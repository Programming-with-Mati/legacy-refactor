package com.globant.javacodecamp.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  private Long orderItemId;
  private Long orderId;
  private Long itemId;
  private Integer quantity;

}
