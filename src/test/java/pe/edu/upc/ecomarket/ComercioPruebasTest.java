package pe.edu.upc.ecomarket;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ComercioPruebasTest extends PruebaBase {

    private static final String COMERCIO_EDITADO = """
            {"nombre": "Granel Verde", "horario": "Lun-Dom 8:00-20:00", "direccion": "Av. Larco 345",
             "distrito": "Miraflores", "ciudad": "Lima", "latitud": -12.1211, "longitud": -77.0297}
            """;

    @Test
    void comercioNacePendienteYSoloLoVeElDueno() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long id = crearComercio(comerciante, false);

        mockMvc.perform(conToken(get("/api/comercios/" + id), comerciante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDING"));
        mockMvc.perform(get("/api/comercios/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/comercios")).andExpect(jsonPath("$[*].id", not(hasItem(id.intValue()))));
    }

    @Test
    void soloComerciantesRegistranComercios() throws Exception {
        mockMvc.perform(json(post("/api/comercios"), registrar("CONSUMIDOR"), COMERCIO_EDITADO))
                .andExpect(status().isForbidden());
    }

    @Test
    void direccionNoUbicableResponde400() throws Exception {
        mockMvc.perform(json(post("/api/comercios"), registrar("COMERCIANTE"), """
                        {"nombre": "Sin mapa", "direccion": "Calle 1", "distrito": "Lince", "ciudad": "Lima"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se pudo ubicar la dirección. Indica la latitud y la longitud del comercio en el mapa"));
    }

    @Test
    void otroComercianteNoPuedeEditar() throws Exception {
        Long id = crearComercio(registrar("COMERCIANTE"), false);
        mockMvc.perform(json(put("/api/comercios/" + id), registrar("COMERCIANTE"), COMERCIO_EDITADO))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorValidaYRechazarExigeMotivo() throws Exception {
        String comerciante = registrar("COMERCIANTE");
        Long id = crearComercio(comerciante, false);
        String admin = tokenAdmin();

        mockMvc.perform(conToken(get("/api/comercios/pendientes"), admin))
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())));
        mockMvc.perform(json(patch("/api/comercios/" + id + "/validacion"), admin, """
                        {"estado": "REJECTED"}
                        """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(patch("/api/comercios/" + id + "/validacion"), admin, """
                        {"estado": "REJECTED", "motivo": "Falta el horario"}
                        """))
                .andExpect(jsonPath("$.estado").value("REJECTED"));

        mockMvc.perform(json(put("/api/comercios/" + id), comerciante, COMERCIO_EDITADO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDING"));

        mockMvc.perform(json(patch("/api/comercios/" + id + "/validacion"), admin, """
                        {"estado": "APPROVED"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APPROVED"));
        mockMvc.perform(get("/api/comercios/" + id)).andExpect(status().isOk());
    }

    @Test
    void busquedaPorCercania() throws Exception {
                String comerciante = registrar("COMERCIANTE");
                Long id = crearComercio(comerciante, true);
                crearProducto(comerciante, id, "Quinua roja", "Quinua orgánica");

        mockMvc.perform(get("/api/busqueda/cercanos").param("lat", "-12.1020").param("lng", "-77.0420").param("radio", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].comercio.id", hasItem(id.intValue())));
        mockMvc.perform(get("/api/busqueda/cercanos").param("lat", "-12.1020").param("lng", "-77.0420").param("radio", "2"))
                .andExpect(jsonPath("$[*].comercio.id", not(hasItem(id.intValue()))));
        mockMvc.perform(get("/api/busqueda/cercanos").param("lat", "-12.1").param("lng", "-77.0").param("radio", "60"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/busqueda/comercios").param("texto", "Miraflores"))
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())));
        mockMvc.perform(get("/api/comercios/" + id + "/productos")
                        .param("nombre", "quinua"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/comercios/" + id + "/productos")
                        .param("categoria", "alimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
