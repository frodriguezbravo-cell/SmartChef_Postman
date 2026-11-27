package es.safareyes.smartchef.planificacion;

import es.safareyes.smartchef.servicios.MiDespensaService;
import es.safareyes.smartchef.servicios.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TareasProgramadas {

    private final NotificacionService notificacionService;
    private final MiDespensaService despensaService;

    @Scheduled(cron = "${app.tareas.caducidad.cron}", zone = "Europe/Madrid")
    public void revisarDespensas() {
        int avisos = notificacionService.avisarCaducidades();
        int eliminados = despensaService.limpiarCaducados();
        log.info("Tarea de caducidad: {} avisos nuevos y {} productos caducados eliminados", avisos, eliminados);
    }

    @Scheduled(cron = "${app.tareas.resumen.cron}", zone = "Europe/Madrid")
    public void resumenSemanal() {
        int resumenes = notificacionService.resumenSemanal();
        log.info("Tarea de resumen semanal: {} resúmenes enviados", resumenes);
    }
}
