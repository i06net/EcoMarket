package pe.edu.upc.ecomarket;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductoPruebasTest extends PruebaBase {

    private Long idEcoEtiqueta(String nombre) throws Exception {
        String cuerpo = mockMvc.perform(get("/api/eco-etiquetas").param("incluirInactivas", "true"))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(cuerpo, "$[?(@.nombre == '" + nombre + "')].id");
        return ids.getFirst().longValue();
    }

    @Test
    void publicarClasificaConPalabrasClaveSinGemini() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);

        mockMvc.perform(json(post("/api/productos"), comerciante, """
                        {"comercioId": %d, "categoriaId": 1, "nombre": "Quinua blanca",
                         "descripcion": "Quinua orgánica de Puno vendida a granel", "precio": 12.50, "stock": 40}
                        """.formatted(comercio)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motorClasificacion").value("keywords"))
                .andExpect(jsonPath("$.ecoEtiquetas[*].nombre", hasItems("Orgánico", "Venta a granel")));

        Long sinEtiquetas = crearProducto(comerciante, comercio, "Fideos", "Paquete de tallarines");
        mockMvc.perform(conToken(post("/api/productos/" + sinEtiquetas + "/clasificar"), comerciante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motor").value("keywords"))
                .andExpect(jsonPath("$.producto.ecoEtiquetas.length()").value(0))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void soloElDuenoPublicaEditaYClasifica() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        String otro = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        Long producto = crearProducto(comerciante, comercio, "Jabón de avena", "Jabón artesanal");

        mockMvc.perform(json(post("/api/productos"), otro, """
                        {"comercioId": %d, "categoriaId": 1, "nombre": "Arroz", "precio": 3, "stock": 3}
                        """.formatted(comercio)))
                .andExpect(status().isForbidden());
        mockMvc.perform(json(put("/api/productos/" + producto), otro, """
                        {"comercioId": %d, "categoriaId": 3, "nombre": "Jabón", "precio": 9, "stock": 5}
                        """.formatted(comercio)))
                .andExpect(status().isForbidden());
        mockMvc.perform(conToken(post("/api/productos/" + producto + "/clasificar"), otro))
                .andExpect(status().isForbidden());
    }

    @Test
    void validaPrecioStockYCategoria() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, false);
        mockMvc.perform(json(post("/api/productos"), comerciante, """
                        {"comercioId": %d, "categoriaId": 1, "nombre": "Arroz", "precio": -1, "stock": -3}
                        """.formatted(comercio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.precio").exists())
                .andExpect(jsonPath("$.errores.stock").exists());
        mockMvc.perform(json(post("/api/productos"), comerciante, """
                        {"comercioId": %d, "categoriaId": 999, "nombre": "Arroz", "precio": 3, "stock": 3}
                        """.formatted(comercio)))
                .andExpect(status().isNotFound());
    }

    @Test
    void busquedaCatalogoYComparacion() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        Long quinua = crearProducto(comerciante, comercio, "Quinua roja", "Quinua orgánica a granel");
        Long jabon = crearProducto(comerciante, comercio, "Jabón natural", "Jabón artesanal hecho a mano");

        mockMvc.perform(get("/api/busqueda/productos").param("nombre", "QUINUA ROJA"))
                .andExpect(jsonPath("$[*].id", hasItem(quinua.intValue())));
        mockMvc.perform(get("/api/busqueda/productos").param("ecoEtiqueta", "Artesanal"))
                .andExpect(jsonPath("$[*].id", hasItem(jabon.intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(quinua.intValue()))));
        mockMvc.perform(get("/api/busqueda/productos").param("nombre", "zzz-no-existe"))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/comercios/" + comercio + "/productos"))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/productos/comparar").param("ids", quinua + "," + jabon)
                        .param("lat", "-12.1020").param("lng", "-77.0420"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanciaKm").exists());
        mockMvc.perform(get("/api/productos/comparar").param("ids", quinua.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void correccionManualYEliminacionLogica() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        Long producto = crearProducto(comerciante, comercio, "Bolsa de tela", "Bolsa reutilizable");
        Long local = idEcoEtiqueta("Producto local");

        mockMvc.perform(json(put("/api/productos/" + producto), comerciante, """
                        {"comercioId": %d, "categoriaId": 4, "nombre": "Bolsa de tela", "precio": 8, "stock": 10,
                         "ecoEtiquetaIds": [%d]}
                        """.formatted(comercio, local)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motorClasificacion").value("manual"));

        mockMvc.perform(conToken(delete("/api/productos/" + producto), comerciante))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/productos/" + producto)).andExpect(status().isNotFound());
    }

    @Test
    void ecoEtiquetaAsignadaSeDesactiva() throws Exception {
        String admin = tokenAdmin();
        Long etiqueta = id(mockMvc.perform(json(post("/api/eco-etiquetas"), admin, """
                        {"nombre": "Comercio justo %s", "palabrasClave": "comercio justo"}
                        """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(json(post("/api/eco-etiquetas"), registrar("COMERCIANTE"), """
                        {"nombre": "No autorizada"}
                        """))
                .andExpect(status().isForbidden());

        String comerciante = registrar("COMERCIANTE");
        Long comercio = crearComercio(comerciante, true);
        Long producto = crearProducto(comerciante, comercio, "Café", "Café de comercio justo");

        mockMvc.perform(conToken(delete("/api/eco-etiquetas/" + etiqueta), admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/eco-etiquetas"))
                .andExpect(jsonPath("$[*].id", not(hasItem(etiqueta.intValue()))));
        mockMvc.perform(get("/api/productos/" + producto))
                .andExpect(jsonPath("$.ecoEtiquetas[*].id", hasItem(etiqueta.intValue())));
    }
}
