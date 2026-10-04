package pe.edu.upc.ecomarket;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthPruebasTest extends PruebaBase {

    @Test
    void datosInicialesCreados() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
        mockMvc.perform(get("/api/eco-etiquetas"))
                .andExpect(jsonPath("$[*].nombre", hasItems("Venta a granel", "Sin envase plástico", "Orgánico")));
        tokenAdmin();
    }

    @Test
    void registroDevuelveTokenSinContrasena() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(correoUnico("lucia"), "CONSUMIDOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("CONSUMIDOR"))
                .andExpect(jsonPath("$.usuario.contrasena").doesNotExist());
    }

    @Test
    void correoRepetidoResponde409() throws Exception {
        String correo = correoUnico("repetido");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registroJson(correo, "CONSUMIDOR")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registroJson(correo, "COMERCIANTE")))
                .andExpect(status().isConflict());
    }

    @Test
    void registroInvalidoResponde400ConCampos() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nombres": "", "apellidos": "Quispe", "correo": "malo", "contrasena": "corta", "rol": "ADMINISTRADOR"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists())
                .andExpect(jsonPath("$.errores.rol").exists());
    }

    @Test
    void loginIncorrectoResponde401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"correo": "admin@ecomarket.pe", "contrasena": "Incorrecta1"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void rolInsuficienteResponde403YSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        mockMvc.perform(conToken(get("/api/usuarios"), registrar("CONSUMIDOR"))).andExpect(status().isForbidden());
        mockMvc.perform(conToken(get("/api/usuarios"), tokenAdmin())).andExpect(status().isOk());
    }

    @Test
    void perfilValidaContrasena() throws Exception {
        String token = registrar("CONSUMIDOR");
        mockMvc.perform(json(put("/api/usuarios/perfil"), token, """
                        {"nombres": "Lucía", "apellidos": "Quispe Rojas", "contrasena": "corta"}
                        """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(json(put("/api/usuarios/perfil"), token, """
                        {"nombres": "Lucía", "apellidos": "Quispe Rojas"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellidos").value("Quispe Rojas"));
    }

    @Test
    void cuentaDesactivadaNoPuedeIniciarSesion() throws Exception {
        String correo = correoUnico("desactivar");
        String cuerpo = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(correo, "CONSUMIDOR")))
                .andReturn().getResponse().getContentAsString();
        Long usuarioId = ((Number) JsonPath.read(cuerpo, "$.usuario.id")).longValue();

        mockMvc.perform(conToken(patch("/api/usuarios/" + usuarioId + "/desactivar"), tokenAdmin()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"correo": "%s", "contrasena": "%s"}
                        """.formatted(correo, CLAVE)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("La cuenta está desactivada"));
    }

    @Test
    void loginSinDatosResponde400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"correo": "", "contrasena": ""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists());
    }

    @Test
    void registroComoAdministradorResponde400() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(correoUnico("falso-admin"), "ADMINISTRADOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.rol").exists());
    }

    @Test
    void desactivarDosVecesResponde400() throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson(correoUnico("doble"), "COMERCIANTE")))
                .andReturn().getResponse().getContentAsString();
        Long usuarioId = ((Number) JsonPath.read(cuerpo, "$.usuario.id")).longValue();
        String tokenAdmin = tokenAdmin();

        mockMvc.perform(conToken(patch("/api/usuarios/" + usuarioId + "/desactivar"), tokenAdmin))
                .andExpect(status().isOk());
                
        mockMvc.perform(conToken(patch("/api/usuarios/" + usuarioId + "/desactivar"), tokenAdmin))
                .andExpect(status().isBadRequest());
    }
}
