package cl.ucn.pipefilter.verticles;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PricingFilterVerticleTest extends VerticleTestBase {

    @Test
    public void calculaSubtotalYDescuento10() throws InterruptedException {
        desplegar(new PricingFilterVerticle());

        JsonObject resultado = enviarYEsperar(
                "order.validated",
                "order.priced",
                ordenConCupon("DESCUENTO10", 2, 15000, 1, 5000)
        );

        assertEquals(Long.valueOf(35000), resultado.getLong("subtotal"));
        assertEquals(Long.valueOf(3500), resultado.getLong("discount"));
        assertEquals(Long.valueOf(31500), resultado.getLong("total"));
        assertEquals("CALCULADA", resultado.getString("status"));
    }

    @Test
    public void calculaDescuento20CuandoSubtotalEsSuficiente() throws InterruptedException {
        desplegar(new PricingFilterVerticle());

        JsonObject resultado = enviarYEsperar(
                "order.validated",
                "order.priced",
                ordenConCupon("DESCUENTO20", 2, 25000, 1, 10000)
        );

        assertEquals(Long.valueOf(60000), resultado.getLong("subtotal"));
        assertEquals(Long.valueOf(12000), resultado.getLong("discount"));
        assertEquals(Long.valueOf(48000), resultado.getLong("total"));
    }

    @Test
    public void noAplicaDescuento20CuandoSubtotalEsInsuficiente() throws InterruptedException {
        desplegar(new PricingFilterVerticle());

        JsonObject resultado = enviarYEsperar(
                "order.validated",
                "order.priced",
                ordenConCupon("DESCUENTO20", 1, 20000, 1, 10000)
        );

        assertEquals(Long.valueOf(30000), resultado.getLong("subtotal"));
        assertEquals(Long.valueOf(0), resultado.getLong("discount"));
        assertEquals(Long.valueOf(30000), resultado.getLong("total"));
    }

    private JsonObject ordenConCupon(String cupon, int cantidadA, int precioA, int cantidadB, int precioB) {
        return new JsonObject()
                .put("orderId", "ORD-PRECIO")
                .put("couponCode", cupon)
                .put("items", new JsonArray()
                        .add(new JsonObject()
                                .put("productId", "PROD-A")
                                .put("quantity", cantidadA)
                                .put("unitPrice", precioA))
                        .add(new JsonObject()
                                .put("productId", "PROD-B")
                                .put("quantity", cantidadB)
                                .put("unitPrice", precioB)));
    }
}
