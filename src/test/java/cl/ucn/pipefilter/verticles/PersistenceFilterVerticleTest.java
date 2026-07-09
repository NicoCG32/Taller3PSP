package cl.ucn.pipefilter.verticles;

import cl.ucn.pipefilter.model.Order;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PersistenceFilterVerticleTest extends VerticleTestBase {

    @Test
    public void constructorPorDefectoCreaElFiltro() {
        assertSame(PersistenceFilterVerticle.class, new PersistenceFilterVerticle().getClass());
    }

    @Test
    public void convierteYPersisteUnaOrdenConSusItems() throws InterruptedException {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);
        desplegar(new PersistenceFilterVerticle(() -> entityManager));

        JsonObject resultado = enviarYEsperar(
                "order.persist",
                "order.done",
                ordenCompleta()
        );

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(transaction).begin();
        verify(entityManager).persist(orderCaptor.capture());
        verify(transaction).commit();
        verify(entityManager).close();

        Order guardada = orderCaptor.getValue();
        assertEquals("ORD-PERSIST", resultado.getString("orderId"));
        assertEquals("ORD-PERSIST", guardada.getOrderId());
        assertEquals("CUST-PERSIST", guardada.getCustomerId());
        assertEquals(Long.valueOf(31500), guardada.getTotal());
        assertEquals(2, guardada.getItems().size());
        assertEquals("PROD-A", guardada.getItems().get(0).getProductId());
        assertSame(guardada, guardada.getItems().get(0).getOrder());
    }

    @Test
    public void revierteLaTransaccionCuandoLaPersistenciaFalla() throws InterruptedException {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);
        org.mockito.Mockito.doThrow(new IllegalStateException("persistencia fallida"))
                .when(entityManager).persist(org.mockito.ArgumentMatchers.any(Order.class));
        desplegar(new PersistenceFilterVerticle(() -> entityManager));

        PrintStream errorOriginal = System.err;
        try {
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            vertx.eventBus().send("order.persist", ordenCompleta());

            verify(transaction, timeout(5000)).rollback();
            verify(entityManager, timeout(5000)).close();
            verify(transaction, never()).commit();
        } finally {
            System.setErr(errorOriginal);
        }
    }

    private JsonObject ordenCompleta() {
        return new JsonObject()
                .put("orderId", "ORD-PERSIST")
                .put("customerId", "CUST-PERSIST")
                .put("currency", "CLP")
                .put("paymentMethod", "TARJETA_CREDITO")
                .put("timestamp", "2025-11-20T12:34:56Z")
                .put("subtotal", 35000L)
                .put("discount", 3500L)
                .put("total", 31500L)
                .put("status", "CALCULADA")
                .put("items", new JsonArray()
                        .add(new JsonObject()
                                .put("productId", "PROD-A")
                                .put("quantity", 2)
                                .put("unitPrice", 15000L))
                        .add(new JsonObject()
                                .put("productId", "PROD-B")
                                .put("quantity", 1)
                                .put("unitPrice", 5000L)));
    }
}
