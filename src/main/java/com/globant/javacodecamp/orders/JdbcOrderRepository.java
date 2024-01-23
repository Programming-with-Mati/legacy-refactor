package com.globant.javacodecamp.orders;

import java.sql.*;
import java.util.ArrayList;

public class JdbcOrderRepository {

  public static final String SELECT_ORDER_WITH_ITEMS = """
          SELECT o.id, o.payment, o.customer_email, oi.id, oi.item_id, oi.quantity, i.stock
          FROM `order` o JOIN order_item oi on o.id = oi.order_id
          JOIN item i on oi.item_id = i.id
          WHERE o.id = %d
          """;

  public static final String UPDATE_STOCK = "UPDATE item SET stock = %d WHERE id = %d;";
  public static final String UPDATE_ORDER_STATE = "UPDATE `order` SET state = 'DISPATCHED' WHERE id = %d";

  private final String jdbcUrl;

  public JdbcOrderRepository(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public Order findOrderById(Long orderId) {
    try (var con = createConnection();
         var statement = con.createStatement();
         ResultSet resultSet = statement.executeQuery(SELECT_ORDER_WITH_ITEMS.formatted(orderId))
    ) {
      Order order;

      if (!resultSet.next()) throw new RuntimeException("Order not found");

      order = mapOrder(resultSet);

      order.setOrderItems(new ArrayList<>());
      var orderItem = mapOrderItem(resultSet);
      order.getOrderItems().add(orderItem);

      return order;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static Order mapOrder(ResultSet resultSet) throws SQLException {
    Order order;
    order = new Order();
    order.setId(resultSet.getLong("o.id"));
    order.setCustomerEmail(resultSet.getString("o.customer_email"));
    order.setPayment(OrderPaymentState.valueOf(resultSet.getString("o.payment")));
    return order;
  }

  private static OrderItem mapOrderItem(ResultSet resultSet) throws SQLException {
    return new OrderItem(
            resultSet.getLong("oi.id"),
            resultSet.getLong("o.id"),
            new Item(resultSet.getLong("oi.item_id"), resultSet.getInt("i.stock")),
            resultSet.getInt("oi.quantity")
    );
  }

  public void updateOrderStateAndItemsStock(Long orderId, Order order) {
    try (var con = createConnection();
        var updateItems = con.createStatement();
        var updateOrder = con.createStatement()
    ) {
      con.setAutoCommit(false);

      order.getOrderItems()
              .stream()
              .map(JdbcOrderRepository::toUpdateStatement)
              .forEach(update -> addUpdateToBatch(updateItems, update));

      String updateOrderQuery = UPDATE_ORDER_STATE.formatted(orderId);

      updateItems.executeBatch();
      updateOrder.execute(updateOrderQuery);
      con.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static void addUpdateToBatch(Statement updateItems, String update) {
    try {
      updateItems.addBatch(update);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static String toUpdateStatement(OrderItem item) {
    return UPDATE_STOCK.formatted(item.getItem().getStock(), item.getItem().getId());
  }

  private Connection createConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return DriverManager
            .getConnection(jdbcUrl, "root", "test");
  }
}
