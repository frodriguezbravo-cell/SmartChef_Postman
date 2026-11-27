package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.excepciones.AccesoDenegadoException;
import es.safareyes.smartchef.excepciones.ConflictoException;
import es.safareyes.smartchef.excepciones.FicheroNoValidoException;
import es.safareyes.smartchef.excepciones.NoAutenticadoException;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaIncumplidaException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.servicios.Fechas;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ProblemDetail noAutenticado(NoAutenticadoException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail accesoDenegado(AccesoDenegadoException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail sinPermiso(AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso denegado", "Tu rol no permite hacer esto");
    }

    @ExceptionHandler(FicheroNoValidoException.class)
    public ProblemDetail ficheroNoValido(FicheroNoValidoException ex) {
        return problema(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de fichero no permitido", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail ficheroGrande(MaxUploadSizeExceededException ex) {
        return problema(HttpStatus.valueOf(413), "Fichero demasiado grande", "El fichero supera el tamaño máximo de 5 MB");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ProblemDetail faltaFichero(MissingServletRequestPartException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", "Falta el fichero \"" + ex.getRequestPartName() + "\"");
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail conflicto(ConflictoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Conflicto", "La operación choca con datos que ya existen");
    }

    @ExceptionHandler(ReglaIncumplidaException.class)
    public ProblemDetail reglaIncumplida(ReglaIncumplidaException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaDeNegocio(ReglaNegocioException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosNoValidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Datos no válidos", "Hay campos con valores no válidos");
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoIlegible(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", "El cuerpo de la petición no es un JSON válido o tiene valores del tipo incorrecto");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", "El valor del parámetro \"" + ex.getName() + "\" no es válido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail faltaParametro(MissingServletRequestParameterException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", "Falta el parámetro \"" + ex.getParameterName() + "\"");
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setProperty("timestamp", LocalDateTime.now(Fechas.MADRID).withNano(0).toString());
        return problema;
    }
}
