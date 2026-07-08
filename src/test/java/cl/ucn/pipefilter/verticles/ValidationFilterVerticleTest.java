package cl.ucn.pipefilter.verticles;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ValidationFilterVerticleTest extends VerticleTestBase {

    @Test
    public void enviaOrdenValidaAlCanalValidated() throws InterruptedException {
        desplegar(new ValidationFilterVerticle());

        JsonObject resultado = enviarYEsperar("order.raw", "order.validated", ordenValida());

        assertEquals("ORD-VALIDA", resultado.getString("orderId"));
    }

    @Test
    public void enviaOrdenConTimestampInvalidoAlCanalError() throws InterruptedException {
        desplegar(new ValidationFilterVerticle());

        JsonObject orden = ordenValida().put("timestamp", "fecha-invalida");
        JsonObject resultado = enviarYEsperar("order.raw", "order.error", orden);

        assertEquals("ORD-VALIDA", resultado.getString("orderId"));
    }

    private JsonObject ordenValida() {
        return new JsonObject()
                .put("orderId", "ORD-VALIDA")
                .put("customerId", "CUST-001")
                .put("currency", "CLP")
                .put("paymentMethod", "TARJETA_CREDITO")
                .put("timestamp", "2025-11-20T12:34:56Z")
                .put("items", new JsonArray()
                        .add(new JsonObject()
                                .put("productId", "PROD-A")
                                .put("quantity", 2)
                                .put("unitPrice", 15000)));
    }
}
