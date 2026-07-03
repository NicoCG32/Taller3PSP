package cl.ucn.pipefilter.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderItemTest {

    @Test
    public void orderItemDebeGuardarProductoCantidadPrecioYOrden() {
        Order order = new Order();
        order.setOrderId("ORD-TEST-005");

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId("PROD-A");
        item.setQuantity(2);
        item.setUnitPrice(15000L);
        item.setOrder(order);

        assertEquals(Long.valueOf(1), item.getId());
        assertEquals("PROD-A", item.getProductId());
        assertEquals(Integer.valueOf(2), item.getQuantity());
        assertEquals(Long.valueOf(15000), item.getUnitPrice());
        assertEquals(order, item.getOrder());
    }
}
