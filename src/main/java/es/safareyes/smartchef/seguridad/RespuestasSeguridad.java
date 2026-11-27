package es.safareyes.smartchef.seguridad;

import es.safareyes.smartchef.servicios.Fechas;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException ex)
            throws IOException {
        escribir(peticion, respuesta, 401, "No autenticado",
                "Hace falta iniciar sesión y enviar un token válido en la cabecera Authorization");
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException ex)
            throws IOException {
        escribir(peticion, respuesta, 403, "Acceso denegado", "Tu rol no permite hacer esto");
    }

    private static void escribir(HttpServletRequest peticion, HttpServletResponse respuesta, int estado,
                                 String titulo, String detalle) throws IOException {
        respuesta.setStatus(estado);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.setContentType("application/problem+json");
        String json = "{\"type\":\"about:blank\",\"title\":\"" + json(titulo) + "\",\"status\":" + estado
                + ",\"detail\":\"" + json(detalle) + "\",\"instance\":\"" + json(peticion.getRequestURI())
                + "\",\"timestamp\":\"" + LocalDateTime.now(Fechas.MADRID).withNano(0) + "\"}";
        respuesta.getWriter().write(json);
    }

    private static String json(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
