package cl.ucn.pipefilter.verticles;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FraudCheckFilterVerticleTest extends VerticleTestBase {

    @Test
    public void marcaRevisionPorMontoAltoConTarjetaDeCredito() throws InterruptedException {
        desplegar(new FraudCheckFilterVerticle());

        JsonObject resultado = enviarYEsperar(
                "order.priced",
                "order.persist",
                ordenCalculada(250001L, "TARJETA_CREDITO", 1)
        );

        assertEquals("REVISION", resultado.getString("status"));
    }

    @Test
    public void marcaRevisionPorCantidadExcesivaDeItems() throws InterruptedException {
        desplegar(new FraudCheckFilterVerticle());

        JsonObject resultado = enviarYEsperar(
                "order.priced",
                "order.persist",
                ordenCalculada(10000L, "EFECTIVO", 21)
        );

        assertEquals("REVISION", resultado.getString("status"));
    }

    @Test
    public void conservaEstadoCalculadaCuandoNoHayRiesgo() throws InterruptedException {
        desplegar(new FraudCheckFilterVerticle());

        JsonObject resultado = enviarYEsperar(
                "order.priced",
                "order.persist",
                ordenCalculada(50000L, "EFECTIVO", 1)
        );

        assertEquals("CALCULADA", resultado.getString("status"));
    }

    private JsonObject ordenCalculada(long total, String metodoPago, int cantidadItems) {
        JsonArray items = new JsonArray();

        for (int i = 0; i < cantidadItems; i++) {
            items.add(new JsonObject()
                    .put("productId", "PROD-" + i)
                    .put("quantity", 1)
                    .put("unitPrice", 1000));
        }

        return new JsonObject()
                .put("orderId", "ORD-FRAUDE")
                .put("total", total)
                .put("paymentMethod", metodoPago)
                .put("status", "CALCULADA")
                .put("items", items);
    }
}
