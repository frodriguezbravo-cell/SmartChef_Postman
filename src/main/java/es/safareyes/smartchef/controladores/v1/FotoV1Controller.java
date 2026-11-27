package es.safareyes.smartchef.controladores.v1;

import es.safareyes.smartchef.dtos.v1.FotoDto;
import es.safareyes.smartchef.seguridad.IdentidadService;
import es.safareyes.smartchef.seguridad.UsuarioAutenticado;
import es.safareyes.smartchef.servicios.FotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Tag(name = "Fotos", description = "Fotos de las recetas")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FotoV1Controller {

    private final FotoService fotoService;

    @Operation(summary = "Subir foto de una receta (multipart), con portada=true opcional")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN')")
    @PostMapping(value = "/recetas/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoDto> subir(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("id") Integer id,
            @RequestPart("fichero") MultipartFile fichero,
            @RequestParam(name = "portada", defaultValue = "false") boolean portada) {
        FotoDto foto = fotoService.subir(IdentidadService.id(usuario), id, fichero, portada);
        return ResponseEntity.created(URI.create(foto.url())).body(foto);
    }

    @Operation(summary = "Descargar foto")
    @GetMapping("/fotos/{id}")
    public ResponseEntity<Resource> descargar(@PathVariable("id") Integer id) {
        FotoService.Descarga descarga = fotoService.descargar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(descarga.tipo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(descarga.nombre(), StandardCharsets.UTF_8).build().toString())
                .body(descarga.fichero());
    }

    @Operation(summary = "Borrar foto de una receta (también borra el fichero)")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN')")
    @DeleteMapping("/recetas/{recetaId}/fotos/{fotoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable("recetaId") Integer recetaId,
            @PathVariable("fotoId") Integer fotoId) {
        fotoService.borrar(IdentidadService.id(usuario), recetaId, fotoId);
    }
}
