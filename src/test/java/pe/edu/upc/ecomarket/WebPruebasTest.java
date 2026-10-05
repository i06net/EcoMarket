package pe.edu.upc.ecomarket;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WebPruebasTest extends PruebaBase {

    @Test
    void landingPublicaCargaCategorias() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("categorias"));
    }

    @Test
    void catalogoWebAceptaFiltros() throws Exception {
        mockMvc.perform(get("/catalogo")
                        .param("texto", "quinua")
                        .param("categoria", "Alimentos a granel"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"))
                .andExpect(model().attributeExists("productos"))
                .andExpect(model().attribute("texto", "quinua"));
    }

    @Test
    void loginWebGeneraSesionJwt() throws Exception {
        mockMvc.perform(post("/login")
                        .param("correo", "admin@ecomarket.pe")
                        .param("contrasena", "Admin12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-success"))
                .andExpect(model().attributeExists("auth"));
    }
}
