package com.globant.javacodecamp.orders;

public class OrderService {

  private final JdbcOrderRepository orderRepository;

  public OrderService(JdbcOrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public Order dispatchOrder(Long orderId) {
    Order order = orderRepository.findOrderById(orderId);
    validateOrderIsPaid(order);

    for (OrderItem orderItem : order.getOrderItems()) {
      Item item = orderItem.getItem();
      if (item.getStock() >= orderItem.getQuantity()) {
        item.setStock(item.getStock() - orderItem.getQuantity());
      } else {
        throw new RuntimeException("Unable to process order. Not enough items %s in stock".formatted(item.getId()));
      }
    }
    order.setState(OrderState.DISPATCHED);

    orderRepository.updateOrderStateAndItemsStock(orderId, order);
    return order;
  }

  private static void validateOrderIsPaid(Order order) {
    if (order.getPayment() != OrderPaymentState.PAID) {
      throw new RuntimeException("Unable to dispatch order. Not yet paid");
    }
  }

}
