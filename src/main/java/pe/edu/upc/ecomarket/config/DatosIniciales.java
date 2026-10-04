package pe.edu.upc.ecomarket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.upc.ecomarket.models.Categoria;
import pe.edu.upc.ecomarket.models.EcoEtiqueta;
import pe.edu.upc.ecomarket.models.Rol;
import pe.edu.upc.ecomarket.models.Usuario;
import pe.edu.upc.ecomarket.repository.CategoriaRepository;
import pe.edu.upc.ecomarket.repository.EcoEtiquetaRepository;
import pe.edu.upc.ecomarket.repository.RolRepository;
import pe.edu.upc.ecomarket.repository.UsuarioRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final CategoriaRepository categoriaRepository;
    private final EcoEtiquetaRepository ecoEtiquetaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.correo}")
    private String correoAdmin;

    @Value("${admin.contrasena}")
    private String contrasenaAdmin;

    @Override
    public void run(String... args) {
        crearRoles();
        crearAdministrador();
        crearCategorias();
        crearEcoEtiquetas();
    }

    private void crearRoles() {
        for (String nombre : List.of(Rol.CONSUMIDOR, Rol.COMERCIANTE, Rol.ADMINISTRADOR)) {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                rolRepository.save(new Rol(null, nombre));
            }
        }
    }

    private void crearAdministrador() {
        if (usuarioRepository.existsByRolNombre(Rol.ADMINISTRADOR)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombres("Administrador");
        admin.setApellidos("EcoMarket");
        admin.setCorreo(correoAdmin.trim().toLowerCase());
        admin.setContrasena(passwordEncoder.encode(contrasenaAdmin));
        admin.setRol(rolRepository.findByNombre(Rol.ADMINISTRADOR).orElseThrow());
        usuarioRepository.save(admin);
    }

    private void crearCategorias() {
        if (categoriaRepository.count() > 0) {
            return;
        }
        categoriaRepository.saveAll(List.of(
                new Categoria(null, "Alimentos a granel", "Granos, menestras, frutos secos y otros alimentos vendidos por peso"),
                new Categoria(null, "Frutas y verduras", "Productos frescos de chacra y ferias orgánicas"),
                new Categoria(null, "Cuidado personal", "Jabones, shampoos sólidos, cepillos y cosmética natural"),
                new Categoria(null, "Limpieza del hogar", "Detergentes, desinfectantes y utensilios de limpieza"),
                new Categoria(null, "Artesanías y textiles", "Objetos y prendas elaborados por artesanos locales"),
                new Categoria(null, "Bebidas", "Cafés, infusiones y jugos de productores locales")));
    }

    private void crearEcoEtiquetas() {
        if (ecoEtiquetaRepository.count() > 0) {
            return;
        }
        ecoEtiquetaRepository.saveAll(List.of(
                new EcoEtiqueta("Venta a granel", "Se vende por peso, sin empaque individual",
                        "El cliente lleva su envase o compra la cantidad exacta que necesita",
                        "granel, por kilo, por peso, rellenable, recarga, trae tu envase"),
                new EcoEtiqueta("Sin envase plástico", "No usa plástico de un solo uso en su empaque",
                        "Empaque de vidrio, papel, cartón, tela o sin empaque",
                        "sin plastico, libre de plastico, sin empaque, vidrio, papel kraft, carton, cero residuos"),
                new EcoEtiqueta("Producto local", "Producido en Perú por productores cercanos",
                        "Elaborado o cultivado por productores nacionales, idealmente de la región",
                        "local, peruano, hecho en peru, productor, chacra, comunidad, andino"),
                new EcoEtiqueta("Artesanal", "Elaborado a mano o en pequeña escala",
                        "Producción manual o en lotes pequeños, sin procesos industriales",
                        "artesanal, hecho a mano, tejido a mano, casero, elaboracion propia"),
                new EcoEtiqueta("Orgánico", "Cultivado sin pesticidas ni fertilizantes sintéticos",
                        "Producción orgánica o agroecológica",
                        "organico, organica, sin pesticidas, agroecologico, sin quimicos"),
                new EcoEtiqueta("Reutilizable", "Pensado para usarse muchas veces",
                        "Reemplaza a un producto desechable",
                        "reutilizable, lavable, retornable, durable, recargable"),
                new EcoEtiqueta("Biodegradable", "Se descompone de forma natural",
                        "Material compostable o de origen vegetal",
                        "biodegradable, compostable, bambu, fibra natural")));
    }
}
