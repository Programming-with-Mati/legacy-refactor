package com.globant.javacodecamp.orders;

public interface OrderRepository {
  Order findOrderById(Long orderId);

  void updateOrderStateAndItemsStock(Long orderId, Order order);
}
