package cl.ucn.pipefilter.model;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderTest {

    @Test
    public void orderDebeGuardarSusCamposPrincipales() {
        Instant timestamp = Instant.parse("2025-11-20T12:34:56Z");

        Order order = new Order();
        order.setOrderId("ORD-TEST-004");
        order.setCustomerId("CUST-004");
        order.setTimestamp(timestamp);
        order.setCurrency("CLP");
        order.setPaymentMethod("TARJETA_CREDITO");
        order.setSubtotal(35000L);
        order.setDiscount(3500L);
        order.setTotal(31500L);
        order.setStatus("CALCULADA");

        assertEquals("ORD-TEST-004", order.getOrderId());
        assertEquals("CUST-004", order.getCustomerId());
        assertEquals(timestamp, order.getTimestamp());
        assertEquals("CLP", order.getCurrency());
        assertEquals("TARJETA_CREDITO", order.getPaymentMethod());
        assertEquals(Long.valueOf(35000), order.getSubtotal());
        assertEquals(Long.valueOf(3500), order.getDiscount());
        assertEquals(Long.valueOf(31500), order.getTotal());
        assertEquals("CALCULADA", order.getStatus());
    }

    @Test
    public void orderNuevaDebeIniciarConListaDeItemsVacia() {
        Order order = new Order();

        assertTrue(order.getItems().isEmpty());
    }
}
