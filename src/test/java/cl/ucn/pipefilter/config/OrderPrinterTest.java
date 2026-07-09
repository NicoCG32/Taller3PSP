package cl.ucn.pipefilter.config;

import cl.ucn.pipefilter.model.Order;
import cl.ucn.pipefilter.model.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderPrinterTest {

    @Test
    public void permiteCrearLaUtilidadDeImpresion() {
        assertNotNull(new OrderPrinter());
    }

    @Test
    public void imprimeLasOrdenesObtenidasDesdeElEntityManager() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Order> query = mock(TypedQuery.class);

        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManager.createQuery("SELECT o FROM Order o", Order.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(ordenCompleta()));

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream salidaOriginal = System.out;
        try {
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            OrderPrinter.printAllOrders(entityManager);
        } finally {
            System.setOut(salidaOriginal);
        }

        String texto = salida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("ORD-MOCK"));
        assertTrue(texto.contains("PROD-MOCK"));
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    public void revierteLaTransaccionCuandoLaConsultaFalla() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);
        when(entityManager.createQuery("SELECT o FROM Order o", Order.class))
                .thenThrow(new IllegalStateException("consulta fallida"));

        PrintStream errorOriginal = System.err;
        try {
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            OrderPrinter.printAllOrders(entityManager);
        } finally {
            System.setErr(errorOriginal);
        }

        verify(transaction).rollback();
        verify(entityManager).close();
    }

    private Order ordenCompleta() {
        Order order = new Order();
        order.setOrderId("ORD-MOCK");
        order.setCustomerId("CUST-MOCK");
        order.setTimestamp(Instant.parse("2025-11-20T12:34:56Z"));
        order.setCurrency("CLP");
        order.setPaymentMethod("TARJETA_CREDITO");
        order.setSubtotal(35000L);
        order.setDiscount(3500L);
        order.setTotal(31500L);
        order.setStatus("CALCULADA");

        OrderItem item = new OrderItem();
        item.setProductId("PROD-MOCK");
        item.setQuantity(2);
        item.setUnitPrice(15000L);
        item.setOrder(order);
        order.setItems(List.of(item));
        return order;
    }
}
