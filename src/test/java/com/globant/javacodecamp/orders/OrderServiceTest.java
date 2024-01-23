package com.globant.javacodecamp.orders;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Testcontainers
class OrderServiceTest {

  @Container
  private MySQLContainer<?> mysql = getMySQLContainer();
  private OrderService orderService;

  @NotNull
  private static MySQLContainer<?> getMySQLContainer() {
    return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.32"))
            .withDatabaseName("shop")
            .withCopyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/docker-entrypoint-initdb.d/init.sql")
            .withUsername("root");
  }

  @Test
  void testDispatchOrder() {
    orderService = new OrderService(new JdbcOrderRepository(mysql.getJdbcUrl()));

    var order = orderService.dispatchOrder(1L);
    assertEquals(OrderState.DISPATCHED, order.getState());

    try(var connection = createConnection()) {
      var resultSet = connection.createStatement()
              .executeQuery("SELECT * FROM item WHERE id = %d".formatted(1L));
      resultSet.next();
      var stock = resultSet.getInt("stock");
      assertEquals(98, stock);
    } catch (SQLException e) {
      fail();
    }
  }

  @Test
  void testDispatchOrderWhenNotPaid() {
    orderService = new OrderService(new JdbcOrderRepository(mysql.getJdbcUrl()));
    var exception = assertThrows(RuntimeException.class, () -> orderService.dispatchOrder(4L));
    assertTrue(exception.getMessage().contains("Not yet paid"));
  }

  @Test
  void testDispatchOrderWhenNotFound() {
    orderService = new OrderService(new JdbcOrderRepository(mysql.getJdbcUrl()));
    var exception = assertThrows(RuntimeException.class, () -> orderService.dispatchOrder(5L));
    assertTrue(exception.getMessage().contains("not found"));
  }

  private Connection createConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return DriverManager
            .getConnection(mysql.getJdbcUrl(), "root", "test");
  }
}
