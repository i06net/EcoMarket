package pe.edu.upc.ecomarket;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdicionalesPruebasTest extends PruebaBase {

    @Test
    void favoritosYPreferencias() throws Exception {
        String consumidor = registrar("CONSUMIDOR");
        Long comercio = crearComercio(registrar("COMERCIANTE"), true);

        Long favorito = id(mockMvc.perform(json(post("/api/favoritos"), consumidor, "{\"comercioId\": " + comercio + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(json(post("/api/favoritos"), consumidor, "{\"comercioId\": " + comercio + "}"))
                .andExpect(status().isConflict());
        mockMvc.perform(conToken(delete("/api/favoritos/" + favorito), registrar("CONSUMIDOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(conToken(delete("/api/favoritos/" + favorito), consumidor))
                .andExpect(status().isNoContent());

        mockMvc.perform(json(put("/api/preferencias"), consumidor, "{\"ecoEtiquetaIds\": [1, 5]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void consumidorNoVeFavoritosNoAprobados() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        String consumidor = registrar("CONSUMIDOR");
        String admin = tokenAdmin();

        mockMvc.perform(json(post("/api/favoritos"), consumidor,
                        "{\"comercioId\": " + comercio + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(json(patch("/api/comercios/" + comercio + "/validacion"), admin,
                        "{\"estado\": \"REJECTED\", \"motivo\": \"Cerrado\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(conToken(get("/api/favoritos"), consumidor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void estadisticasRegistranVisitasYBusquedas() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        Long producto = crearProducto(comerciante, comercio, "Granola", "Granola artesanal a granel");
        String consumidor = registrar("CONSUMIDOR");

        mockMvc.perform(conToken(get("/api/comercios/" + comercio).param("busqueda", "granola"), consumidor));
        mockMvc.perform(conToken(get("/api/productos/" + producto).param("busqueda", "granola"), consumidor));

        mockMvc.perform(conToken(get("/api/comercios/" + comercio + "/estadisticas").param("periodo", "mes"), comerciante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitasComercio").value(1))
                .andExpect(jsonPath("$.visitasProductos[0].cantidad").value(1))
                .andExpect(jsonPath("$.terminosBusqueda[0].nombre").value("granola"))
                .andExpect(jsonPath("$.premium").value(false))
                .andExpect(jsonPath("$.mensajePremium").exists());
        mockMvc.perform(conToken(get("/api/comercios/" + comercio + "/estadisticas"), registrar("COMERCIANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinVisitasMuestraCerosYMensaje() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        mockMvc.perform(conToken(get("/api/comercios/" + comercio + "/estadisticas"), comerciante))
                .andExpect(jsonPath("$.visitasComercio").value(0))
                .andExpect(jsonPath("$.mensajeVisitas").exists())
                .andExpect(jsonPath("$.mensajeBusquedas").exists());
    }

    @Test
    void premiumPermitePromocionesYDestacados() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        Long producto = crearProducto(comerciante, comercio, "Miel de abeja", "Miel artesanal");
        LocalDate hoy = LocalDate.now();
        String promocion = """
                {"comercioId": %d, "titulo": "2x1", "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(comercio, hoy, hoy.plusDays(7));

        mockMvc.perform(json(post("/api/promociones"), comerciante, promocion)).andExpect(status().isForbidden());

        mockMvc.perform(json(post("/api/suscripciones"), comerciante, "{\"comercioId\": " + comercio + ", \"meses\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activa").value(true));

        mockMvc.perform(json(post("/api/promociones"), comerciante, """
                        {"comercioId": %d, "titulo": "2x1", "fechaInicio": "%s", "fechaFin": "%s"}
                        """.formatted(comercio, hoy.plusDays(5), hoy)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(post("/api/promociones"), comerciante, promocion)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/comercios/" + comercio))
                .andExpect(jsonPath("$.premium").value(true))
                .andExpect(jsonPath("$.promociones[0].titulo").value("2x1"));
        mockMvc.perform(get("/api/productos/" + producto))
                .andExpect(jsonPath("$.destacado").value(true));
        mockMvc.perform(conToken(get("/api/comercios/" + comercio + "/estadisticas"), comerciante))
                .andExpect(jsonPath("$.avanzadas.evolucionMensual.length()").value(6));
    }

    @Test
    void recomendacionesYResumen() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        Long producto = crearProducto(comerciante, comercio, "Lentejas", "Lentejas orgánicas a granel");

        String nuevo = registrar("CONSUMIDOR");
        mockMvc.perform(conToken(get("/api/recomendaciones/productos"), nuevo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criterio").value("mas-consultados"));
        mockMvc.perform(conToken(get("/api/consumidor/resumen"), nuevo))
                .andExpect(jsonPath("$.mensaje").exists());

        String consumidor = registrar("CONSUMIDOR");
        mockMvc.perform(json(put("/api/preferencias"), consumidor, "{\"ecoEtiquetaIds\": [1]}")).andExpect(status().isOk());
        mockMvc.perform(conToken(get("/api/recomendaciones/productos"), consumidor))
                .andExpect(jsonPath("$.criterio").value("preferencias"))
                .andExpect(jsonPath("$.productos[*].id", hasItem(producto.intValue())));
        mockMvc.perform(conToken(get("/api/recomendaciones/comercios").param("lat", "-12.1020").param("lng", "-77.0420"), consumidor))
                .andExpect(jsonPath("$.criterio").value("preferencias"));

        mockMvc.perform(conToken(get("/api/productos/" + producto), consumidor));
        mockMvc.perform(conToken(get("/api/consumidor/resumen"), consumidor))
                .andExpect(jsonPath("$.productosConsultadosPorEcoEtiqueta.length()").isNotEmpty());
        mockMvc.perform(conToken(get("/api/recomendaciones/productos"), comerciante))
                .andExpect(status().isForbidden());
    }
}
