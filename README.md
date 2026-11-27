# SmartChef · backend

API REST de recetas, despensa, lista de la compra, colecciones e historial de comidas
(Spring Boot, Spring Data JPA, PostgreSQL).

## Puesta en marcha

1. Copiar `.env.example` a `.env` y rellenar `DB_PASSWORD` y `JWT_SECRET` (al menos 32 caracteres).
2. Arrancar PostgreSQL 16 con `docker compose up -d`. La primera vez se ejecutan solos
   `script_smartchefdb.sql` y `datos_prueba_usuario.sql`. Para empezar de cero: `docker compose down -v`.
3. Arrancar la API con `./mvnw spring-boot:run` (o `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
   para que las tareas programadas se ejecuten cada minuto).
4. Swagger UI en `http://localhost:8080/swagger-ui.html` (botón *Authorize* con el token).

Sin Docker: crear la base de datos `smartchefdb` y ejecutar a mano los dos scripts de `src/main/resources/utils`.

### Variables de entorno

| Variable | Uso |
|----------|-----|
| `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_SCHEMA` | Conexión a PostgreSQL |
| `JWT_SECRET` | Clave de firma de los tokens (mínimo 32 caracteres) |
| `STORAGE_DIR` | Carpeta de las fotos (por defecto `uploads`, excluida de Git) |

### Usuarios de prueba

| Email | Contraseña | Rol |
|-------|-----------|-----|
| `demo@smartchef.es` | `1234` | USUARIO |
| `chef@smartchef.es` | `1234` | CHEF |
| `admin@smartchef.es` | `1234` | ADMIN |

## Seguridad

- `POST /api/v1/auth/registro` da de alta con el rol USUARIO y crea su colección «Favoritas».
- `POST /api/v1/auth/login` devuelve un JWT que caduca a los 60 minutos. Se envía como `Authorization: Bearer <token>`.
- Contraseñas con `BCryptPasswordEncoder`. Sin token o con token caducado: 401; rol insuficiente o recurso ajeno: 403.
- Reglas por ruta en `SecurityConfig` y `@PreAuthorize` en alta, modificación y retirada de recetas, fotos e informes.
- Propiedad del recurso: despensa, listas de la compra, colecciones, registros y notificaciones son siempre los del usuario
  del token; en las rutas `/api/usuarios/{idUsuario}/...` el id tiene que ser el del token (o ser ADMIN).
- Públicos: catálogo y detalle de recetas, ingredientes, descarga de fotos, `/api/v1/auth/**` y Swagger.

## Fotos

- `POST /api/v1/recetas/{id}/fotos` (multipart, campo `fichero`, `portada=true` opcional): solo el chef autor.
  Una portada y hasta 4 fotos más. Solo JPEG, PNG y WEBP (415) y como máximo 5 MB (413).
- `GET /api/v1/fotos/{id}` (público) devuelve la imagen con su `Content-Type`.
- `DELETE /api/v1/recetas/{recetaId}/fotos/{fotoId}` borra la foto y su fichero.
- En disco el nombre es un UUID; en la tabla `foto_receta` se guardan nombre original, clave, tipo, tamaño, orden y fecha.
- Una receta solo aparece en el catálogo (endpoint 1) si tiene foto de portada.

## Notificaciones y tareas programadas

`GET /api/v1/notificaciones?leida=&pagina=&tamano=` y `PATCH /api/v1/notificaciones/{id}/leida` (solo las propias).

| Tarea | Cron (`application.properties`) | Qué hace |
|-------|------|----------|
| `revisarDespensas` | `0 0 9 * * *` | Avisa de lo que caduca en 2 días o menos sugiriendo una receta y borra lo caducado hace más de 7 días |
| `resumenSemanal` | `0 0 9 * * MON` | Resumen de la semana anterior (comidas y calorías) |

Son idempotentes: antes de avisar se comprueba en `notificacion` si ya existe el aviso de ese producto o de esa semana.
Además se avisa al retirar una receta que alguien tiene en una colección y al actualizar la lista de la compra.
Más detalle en `docs/tareas.md`.

## Endpoints (16) y errores

| # | Ruta | Roles |
|---|------|-------|
| 1 | `GET /api/v1/recetas` | público |
| 2 | `GET /api/v1/recetas/{id}` | público |
| 3 | `POST /api/v1/recetas` | CHEF, ADMIN |
| 4 | `PUT /api/v1/recetas/{id}` | CHEF autor, ADMIN |
| 5 | `DELETE /api/v1/recetas/{id}` | CHEF autor, ADMIN |
| 6 | `GET /api/v1/ingredientes?texto=` | público |
| 7 | `POST /api/v1/recetas/buscar-por-ingredientes` | identificado |
| 8 | `GET /api/v1/despensa` | identificado |
| 9 | `PUT /api/v1/despensa/{ingredienteId}` | identificado |
| 10 | `POST /api/v1/listas-compra/desde-receta` | identificado |
| 11 | `PATCH /api/v1/listas-compra/{id}/cerrar` | dueño de la lista |
| 12 | `GET /api/v1/colecciones` | identificado |
| 13 | `PUT /api/v1/colecciones/{nombre}/recetas/{recetaId}` | identificado |
| 14 | `POST /api/v1/registros` | identificado |
| 15 | `GET /api/v1/registros/semana?fecha=` | identificado |
| 16 | `GET /api/v1/informes/recetas-populares?desde=&hasta=` | ADMIN, CHEF |

Además: `GET/PUT /api/usuarios/{id}/preferencias` (RN-03). Errores en formato `ProblemDetail`:
400 datos mal formados (con mapa `errores`), 401 sin identificar, 403 sin permiso, 404 no existe,
409 conflicto (registro duplicado, lista ya cerrada, receta retirada), 413 fichero de más de 5 MB, 415 tipo de fichero no permitido, 422 regla de negocio incumplida
(fecha futura, referencia a un código inexistente, comida sin receta ni descripción).

## Mapeo con MapStruct

Las conversiones entidad ↔ DTO están en el paquete `mapeadores` (`@Mapper(componentModel = "spring")`):

| Mapper | Qué convierte |
|--------|---------------|
| `RecetaMapper` | tarjetas, detalle completo, líneas y pasos; `RecetaPeticion` → `Receta` |
| `ColeccionMapper` | colecciones con sus recetas (`uses = RecetaMapper.class`) |
| `RegistroMapper` | registros de comida (`uses = RecetaMapper.class`) |
| `DespensaMapper` | líneas de la despensa (`@Context` con la fecha de hoy) y productos |
| `ListaCompraMapper` | listas de la compra y sus líneas |
| `IngredienteMapper` | catálogo de ingredientes |
| `InformeMapper` | recetas más guardadas |
| `UsuarioMapper` | alergias y actualización parcial de preferencias |
| `PlanComidaMapper`, `CatalogoMapper` | plan de comidas y opciones de los catálogos |

- `@Mapping(target, source)` con nombres distintos o anidados: `source = "dificultad.nombre"`, `source = "caloriasKcal"`.
- `@Mapping(ignore = true)` en `aLinea` y `aPaso`: el servicio rellena receta, ingrediente, unidad, gramos y orden.
- Actualización parcial con `@MappingTarget` y `NullValuePropertyMappingStrategy.IGNORE` en `UsuarioMapper.actualizarPreferencias`.
- Listas de entidades a listas de DTOs (`tarjetas`, `colecciones`, `lineasCompra`...) y uso de otro mapper con `uses`.
- `injectionStrategy = InjectionStrategy.CONSTRUCTOR` en los mappers que usan otro, para inyectar por constructor.

## Pruebas con Postman

Colección en `docs/postman/SmartChef.postman_collection.json`: login, los 16 endpoints de negocio, fotos, notificaciones
y sus casos de error. Los tokens y los ids (`recetaId`, `listaId`, `fotoId`...) se guardan solos en variables.
Las carpetas están ordenadas para ejecutarse con el *Collection Runner* sobre una base de datos recién creada.
Para las subidas, el *working directory* de Postman debe ser la carpeta `docs/postman` (allí están las imágenes).

## Dónde se usa cada formato de consulta

| Formato | Dónde |
|---------|-------|
| Q1 derivadas | `existsByUsuarioIdAndFechaAndMomento`, `findByUsuarioIdAndFechaBetweenOrderByFechaAsc`, `findByUsuarioIdAndNombreIgnoreCase`, `findFirstByUsuarioIdAndCompletadaFalseOrderByIdAsc` (lista abierta), `findByNombreContainingIgnoreCaseAndPublicadaTrue` |
| Q2 JPQL | `IRecetaRepository.idsVegetarianas/idsSinGluten` (`NOT EXISTS`), `idsEconomicas` (`SUM` con `JOIN`), `IRegistroComidaRepository.masCocinadas` (`COUNT` + `GROUP BY`) |
| Q3 nativas | `coincidenciasConIngredientes` (`GROUP BY` + `HAVING`, RN-04), `caloriasPorDia` (`DATE_TRUNC('week', …)`), `masGuardadas` |
| Q4 modificación | `IDespensaItemRepository.eliminarCaducadosAntesDe` (`MiDespensaService.limpiarCaducados`, caducados hace más de 7 días; la llama la tarea `revisarDespensas`) |
| Q5 proyecciones | `RecetaTarjetaView` (catálogo), `CoincidenciaView`, `CaloriasDiaView`, `SELECT new …RecetaPopularDto(…)` |
| Q6 paginación | endpoints 1 y 6 (`Paginas`: página, tamaño 1-50 y orden con lista blanca) |
| Q7 Specification | `RecetaSpecs` (filtros combinables del endpoint 1) |
| Q8 N+1 | `@EntityGraph` en `findConIngredientesById` y `findConPasosById` (endpoint 2) + `default_batch_fetch_size=50` |

### MultipleBagFetchException (decisión)

`Receta` tiene dos `List` (ingredientes y pasos). Cargar las dos con `JOIN FETCH`/`@EntityGraph` en una sola consulta
lanza `MultipleBagFetchException`. Se ha resuelto **en dos consultas**: la primera trae la receta con sus ingredientes
(y el ingrediente y la unidad de cada línea), la segunda trae los pasos sobre la misma instancia ya cargada en la sesión.
Se prefiere a cambiar a `Set` porque las listas conservan el orden (`@OrderBy`) y el producto cartesiano
ingredientes × pasos no se produce.

## Decisiones sobre lo que el enunciado deja abierto

- **Precio**: `ingrediente.precio_kg` es €/kg (o €/litro). Coste de una línea = gramos × precio / 1000; ml se tratan como gramos.
  El coste por ración suma las líneas obligatorias. Si alguna no tiene precio o gramos, el coste es incompleto
  (`costeCompleto = false`) y la receta **no** se considera económica.
- **Preferencias de receta (RN-02)**: se calculan al consultar (`CalculadoraPreferencias`), nunca se guardan.
  Vegetariana: todos los ingredientes (también los opcionales) son aptos. Sin gluten: ninguno tiene el alérgeno GLUTEN.
- **Preferencias del perfil (RN-03)**: vegetariana = dieta VEGETARIANA o VEGANA; sin gluten = dieta SIN_GLUTEN;
  rápida y económica son dos marcas del perfil. Un parámetro de la petición manda sobre el perfil; `false` significa «no filtrar».
- **Búsqueda por ingredientes (RN-04)**: porcentaje = ingredientes distintos que se tienen / ingredientes distintos de la receta
  (opcionales incluidos); solo se devuelven las de más del 50 %. Lo caducado de la despensa no cuenta.
- **Lista de la compra (RN-05/06)**: una sola lista abierta por persona (se crea si no hay). Lo caducado no se resta.
  Al cerrar, lo comprado se suma a la despensa (mismo ingrediente y unidad) y la lista queda `CERRADA` con todas sus líneas.
  La API anterior (`/api/usuarios/{id}/listas-compra`) respeta lo mismo: no deja crear una segunda lista abierta ni
  añadir recetas o trasladar lo comprado de una lista cerrada (409), para no sumar dos veces a la despensa.
- **Despensa (PUT)**: sustituye la cantidad del ingrediente (si había varias entradas se unifican); cantidad 0 lo elimina.
- **Historial (RN-07)**: las calorías se calculan al registrar (por ración × raciones) y se guardan en el registro.
  El descuento de la despensa gasta primero lo que caduca antes y convierte kg↔g y l↔ml.
- **Retirada (RN-09)**: `receta.retirada` (la receta sigue existiendo). No sale en el catálogo ni en las búsquedas,
  pero sí en colecciones e historial, marcada como retirada. No se puede modificar, guardar ni registrar una retirada (409).
- **Colecciones (RN-08)**: «Favoritas» se crea al registrarse (y al primer uso para los usuarios anteriores) y la API no permite borrar colecciones.
