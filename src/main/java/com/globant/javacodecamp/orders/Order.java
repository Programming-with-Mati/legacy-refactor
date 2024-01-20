package com.globant.javacodecamp.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  private Long id;
  private OrderPaymentState payment;
  private String customerEmail;
  private List<OrderItem> orderItems;
  private OrderState state;
}
