package es.safareyes.smartchef.excepciones;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super(recurso + " no encontrado/a: " + identificador);
    }
}
