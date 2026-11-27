package es.safareyes.smartchef.almacenamiento;

import es.safareyes.smartchef.excepciones.FicheroNoValidoException;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class StorageService {

    private static final Map<String, Set<String>> EXTENSIONES = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp"));

    private final Path carpeta;

    public StorageService(@Value("${app.storage.dir}") String directorio) {
        this.carpeta = Paths.get(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new UncheckedIOException("No se puede crear la carpeta de ficheros " + carpeta, e);
        }
    }

    public String validar(MultipartFile fichero) {
        if (fichero == null || fichero.isEmpty()) {
            throw new ReglaNegocioException("El fichero está vacío");
        }
        String tipo = fichero.getContentType() == null ? "" : fichero.getContentType().toLowerCase(Locale.ROOT);
        Set<String> permitidas = EXTENSIONES.get(tipo);
        String extension = extension(fichero.getOriginalFilename());
        if (permitidas == null || !permitidas.contains(extension)) {
            throw new FicheroNoValidoException("Solo se admiten imágenes JPEG, PNG o WEBP con su extensión correcta");
        }
        return tipo;
    }

    public String guardar(MultipartFile fichero) {
        String clave = UUID.randomUUID() + "." + extension(fichero.getOriginalFilename());
        Path destino = ruta(clave);
        try (InputStream entrada = fichero.getInputStream()) {
            Files.copy(entrada, destino);
        } catch (IOException e) {
            throw new UncheckedIOException("No se ha podido guardar el fichero", e);
        }
        log.info("Fichero guardado: {} ({} bytes)", clave, fichero.getSize());
        return clave;
    }

    public Resource cargar(String clave) {
        Path ruta = ruta(clave);
        if (!Files.exists(ruta)) {
            throw new RecursoNoEncontradoException("Fichero", clave);
        }
        return new FileSystemResource(ruta);
    }

    public void borrar(String clave) {
        try {
            Files.deleteIfExists(ruta(clave));
            log.info("Fichero borrado: {}", clave);
        } catch (IOException e) {
            log.warn("No se ha podido borrar el fichero {}", clave, e);
        }
    }

    private Path ruta(String clave) {
        Path ruta = carpeta.resolve(clave).normalize();
        if (!ruta.startsWith(carpeta)) {
            throw new FicheroNoValidoException("Nombre de fichero no válido");
        }
        return ruta;
    }

    private static String extension(String nombre) {
        if (nombre == null || !nombre.contains(".")) {
            return "";
        }
        return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
