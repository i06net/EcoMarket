package pe.edu.upc.ecomarket;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class PruebaBase {

    protected static final String CLAVE = "Clave2026";

    @Autowired
    protected MockMvc mockMvc;

    protected static String correoUnico(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8) + "@correo.com";
    }

    protected static String registroJson(String correo, String rol) {
        return """
                {"nombres": "Lucía", "apellidos": "Quispe", "correo": "%s", "contrasena": "%s", "rol": "%s"}
                """.formatted(correo, CLAVE, rol);
    }

    protected String registrar(String rol) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(correoUnico(rol.toLowerCase()), rol)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.token");
    }

    protected String tokenAdmin() throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "admin@ecomarket.pe", "contrasena": "Admin12345"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.token");
    }

    protected static MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder peticion, String token) {
        return peticion.header("Authorization", "Bearer " + token);
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String token, String cuerpo) {
        peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        return token == null ? peticion : conToken(peticion, token);
    }

    protected static Long id(String cuerpo) {
        return ((Number) JsonPath.read(cuerpo, "$.id")).longValue();
    }

    protected Long crearComercio(String tokenComerciante, boolean aprobar) throws Exception {
        Long comercioId = id(mockMvc.perform(json(post("/api/comercios"), tokenComerciante, """
                        {"nombre": "Granel Verde", "descripcion": "Tienda a granel", "horario": "Lun-Sáb 9:00-19:00",
                         "direccion": "Av. Larco 345", "distrito": "Miraflores", "ciudad": "Lima",
                         "latitud": -12.1211, "longitud": -77.0297}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        if (aprobar) {
            mockMvc.perform(json(patch("/api/comercios/" + comercioId + "/validacion"), tokenAdmin(), """
                            {"estado": "APPROVED"}
                            """))
                    .andExpect(status().isOk());
        }
        return comercioId;
    }

    protected Long crearProducto(String tokenComerciante, Long comercioId, String nombre, String descripcion) throws Exception {
        return id(mockMvc.perform(json(post("/api/productos"), tokenComerciante, """
                        {"comercioId": %d, "categoriaId": 1, "nombre": "%s", "descripcion": "%s", "precio": 12.50, "stock": 40}
                        """.formatted(comercioId, nombre, descripcion)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }
}
