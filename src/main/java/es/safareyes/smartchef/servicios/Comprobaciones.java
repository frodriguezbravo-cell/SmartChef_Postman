package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;

import java.util.Optional;

final class Comprobaciones {

    private Comprobaciones() {
    }

    static <T> T existe(Optional<T> valor, String recurso, Object identificador) {
        return valor.orElseThrow(() -> new RecursoNoEncontradoException(recurso, identificador));
    }
}
