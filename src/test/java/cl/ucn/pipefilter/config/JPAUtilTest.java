package cl.ucn.pipefilter.config;

import cl.ucn.pipefilter.model.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JPAUtilTest {

    @Test
    public void creaElEntityManagerUsadoPorLaImpresionPorDefecto() {
        EntityManagerFactory factory = mock(EntityManagerFactory.class);
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Order> query = mock(TypedQuery.class);

        when(factory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManager.createQuery("SELECT o FROM Order o", Order.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        try (MockedStatic<Persistence> persistence = mockStatic(Persistence.class)) {
            persistence.when(() -> Persistence.createEntityManagerFactory("ordersPU"))
                    .thenReturn(factory);

            assertNotNull(new JPAUtil());
            OrderPrinter.printAllOrders();

            persistence.verify(() -> Persistence.createEntityManagerFactory("ordersPU"));
            verify(factory).createEntityManager();
            verify(entityManager).close();
        }
    }
}
