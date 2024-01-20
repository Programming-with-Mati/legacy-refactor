package com.globant.javacodecamp.orders;

import java.sql.*;
import java.util.*;

public class OrderService {

  private final String connectionString;

  public OrderService(String connectionString) {
    this.connectionString = connectionString;
  }

  public Order dispatchOrder(Long orderId) {
    try (Connection con = createConnection()) {
      con.setAutoCommit(false);
      Statement statement = con.createStatement();
      String query = """
              SELECT o.id, o.payment, o.customer_email, oi.id, oi.item_id, oi.quantity, i.stock
              FROM `order` o JOIN order_item oi on o.id = oi.order_id
              JOIN item i on oi.item_id = i.id
              WHERE o.id = %d
              """.formatted(orderId);
      ResultSet resultSet = statement.executeQuery(query);
      Order order = new Order();
      Map<Long,Item> items = new HashMap<>();
      while (resultSet.next()) {
        if (order.getId() == null) {
          order.setId(resultSet.getLong("o.id"));
          order.setCustomerEmail(resultSet.getString("o.customer_email"));
          order.setPayment(OrderPaymentState.valueOf(resultSet.getString("o.payment")));

          if (order.getPayment() != OrderPaymentState.PAID) {
            throw new RuntimeException("Unable to dispatch order. Not yet paid");
          }

          order.setOrderItems(new ArrayList<>());
        }
        var itemId = resultSet.getLong("oi.item_id");
        order.getOrderItems().add(new OrderItem(
                resultSet.getLong("oi.id"),
                resultSet.getLong("o.id"),
                itemId,
                resultSet.getInt("oi.quantity")
                )
        );
        items.put(itemId, new Item(
                itemId,
                        resultSet.getInt("i.stock")
                )
        );
      }

      String updateItemsQueryTemplate = "UPDATE item SET stock = %d WHERE id = %d;";
      var updateItems = con.createStatement();

      for (OrderItem orderItem : order.getOrderItems()) {
        Item item = items.get(orderItem.getOrderItemId());
        if (item.getStock() >= orderItem.getQuantity()) {
          item.setStock(item.getStock() - orderItem.getQuantity());
          updateItems.addBatch(updateItemsQueryTemplate.formatted(item.getStock(), item.getId()));
        } else {
          throw new RuntimeException("Unable to process order. Not enough items %s in stock".formatted(orderItem.getItemId()));
        }
      }
      order.setState(OrderState.DISPATCHED);
      String updateOrderQuery = "UPDATE `order` SET state = 'DISPATCHED' WHERE id = %d".formatted(orderId);

      updateItems.executeBatch();
      var updateOrder = con.createStatement();
      updateOrder.execute(updateOrderQuery);
      con.commit();
      return order;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Connection createConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return DriverManager
            .getConnection(connectionString, "root", "test");
  }
}
