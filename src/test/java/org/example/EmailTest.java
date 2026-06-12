package org.example;

import org.example.config.EmailConfig;
import org.example.model.Client;
import org.example.model.CustomerOrder;
import org.example.model.OrderItem;
import org.example.utils.EmailUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class EmailTest {
    @Test
    void shouldGenerateHtmlRowsForOrderItems() throws Exception {

        CustomerOrder order = new CustomerOrder();

        order.setOrderItems(List.of(
                new OrderItem("P001", "Laptop", 2, 1500),
                new OrderItem("P002", "Mouse", 1, 50)
        ));

        Method method =
                EmailUtils.class.getDeclaredMethod(
                        "orderItemListToHtmlList",
                        CustomerOrder.class
                );

        method.setAccessible(true);

        String result =
                (String) method.invoke(null, order);

        assertTrue(result.contains("P001"));
        assertTrue(result.contains("Laptop"));
        assertTrue(result.contains("P002"));
        assertTrue(result.contains("Mouse"));
    }

}
