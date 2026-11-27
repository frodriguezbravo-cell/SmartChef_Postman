package es.safareyes.smartchef.excepciones;

public class FicheroNoValidoException extends RuntimeException {

    public FicheroNoValidoException(String mensaje) {
        super(mensaje);
    }
}
