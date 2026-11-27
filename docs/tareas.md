# Tareas programadas

Se activan con `@EnableScheduling` (`PlanificacionConfig`) y están en `planificacion/TareasProgramadas`.
No tienen lógica: llaman a `NotificacionService` y `MiDespensaService`.

| Tarea | Propiedad | Producción | Perfil `dev` |
|-------|-----------|------------|--------------|
| `revisarDespensas` | `app.tareas.caducidad.cron` | `0 0 9 * * *` (cada día a las 09:00) | `0 * * * * *` (cada minuto) |
| `resumenSemanal` | `app.tareas.resumen.cron` | `0 0 9 * * MON` (los lunes a las 09:00) | `30 * * * * *` (cada minuto, segundo 30) |

## Cómo probarlas

1. Arrancar con el perfil `dev`: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.
2. Con el token de `demo@smartchef.es`, añadir a la despensa algo que caduque pronto:
   `PUT /api/v1/despensa/1` con `{"cantidad": 300, "unidad": "G", "fechaCaducidad": "<mañana>"}`.
3. Esperar al minuto siguiente: en el log aparece `Tarea de caducidad: 1 avisos nuevos...`.
4. `GET /api/v1/notificaciones` muestra el aviso `CADUCIDAD_PROXIMA` con una receta que usa ese ingrediente.
5. En las ejecuciones siguientes el log dice `0 avisos nuevos`: el aviso de ese producto ya existe (idempotencia).
6. Para el resumen semanal hace falta algún registro de la semana anterior
   (`POST /api/v1/registros` con una `fecha` de la semana pasada). Se envía una sola vez por semana y usuario.
