# Sistema de Tutorías — Documentación del backend

Documentación del prototipo de pruebas. Está alineada con:

- **Esquema de BD:** [`db/supabase/001_esquema.sql`](../db/supabase/001_esquema.sql), que parte de `TutorConnect.sql` (dbdiagram) con los cambios descritos en [MODELO_DE_DATOS.md](MODELO_DE_DATOS.md). Diagrama actualizado en [`db/TutorConnect.dbml`](../db/TutorConnect.dbml).
- **Mockups de escritorio** del 5 oct 2026 (16 pantallas).

> El backend implementa todo lo descrito aquí. Las pruebas de integración (`src/test`) ejecutan el mismo script de Supabase y verifican los flujos de los mockups.

## Documentos

| Documento | Contenido |
|---|---|
| [AUTENTICACION.md](AUTENTICACION.md) | Login con Microsoft y Google, registro en el primer acceso, registro y login con contraseña, JWT propio, campo de proveedor y opciones para entrar al panel de administración. |
| [API.md](API.md) | Referencia de todos los endpoints: ruta, roles, body, respuesta y validaciones. |
| [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md) | Estados, cupo, choques de horario, sesiones y pase de lista, bajas, recordatorios y correos. |
| [MODELO_DE_DATOS.md](MODELO_DE_DATOS.md) | Tablas, cambios aplicados al esquema recibido y cómo crear la BD en Supabase. |

---

## Cómo correr el backend

1. **Base de datos (Supabase):** en el SQL Editor ejecuta, en orden:
   1. `db/supabase/001_esquema.sql`: crea el esquema. **Es destructivo**: borra las tablas anteriores.
   2. `db/supabase/002_datos_prueba.sql` (opcional): las 54 EE reales de Ingeniería de Software (plan 2023) y datos de los mockups.
   3. `db/supabase/003_materias_plan_2019.sql` (opcional): catálogo de EE del plan 2019.

   Si la BD ya existía antes del registro con contraseña, ejecuta solo `db/supabase/004_registro_con_contrasena.sql` (no borra datos) **antes de desplegar** el backend.
2. **Variables de entorno:** copia `.env.example` a `.env` y llénalo. `DB_URL` es la cadena JDBC de Supabase.
3. **Arrancar:** `./mvnw spring-boot:run`. Swagger UI queda en `/swagger-ui.html`.
4. **Probar sin Microsoft ni Google:** con `AUTH_DEV_HABILITADO=true`, `POST /auth/dev` con el correo de un usuario de los datos de prueba (ver [AUTENTICACION.md § 8](AUTENTICACION.md#8-login-de-desarrollo)). **Nunca lo actives en un despliegue público.**
5. **Pruebas:** `./mvnw test` necesita un PostgreSQL. Por defecto usa `localhost:5433/tutorias_test`; se cambia con `TEST_DB_URL`, `TEST_DB_USERNAME` y `TEST_DB_PASSWORD`. Las pruebas recrean el esquema con el script de Supabase.

---

## Convenciones generales

- **Base URL local:** `http://localhost:8080`
- **Formato:** JSON (`Content-Type: application/json`).
- **Rutas en plural:** `/tutorias`, `/inscripciones`, `/sesiones`, `/materias`…
- **Autenticación:** `Authorization: Bearer <token>` en todo excepto `/auth/**`. El token es el JWT propio del backend (ver [AUTENTICACION.md](AUTENTICACION.md)).
- **Roles:** `TUTORADO`, `TUTOR`, `ADMIN`. Un usuario tiene un solo rol.
- **Zona horaria de negocio:** `America/Mexico_City`. Todas las reglas de tiempo (inicio de sesión, 15 minutos, recordatorio 2 horas antes) se evalúan en esa zona, sin importar la zona del servidor.

### Formatos

| Tipo | Formato | Ejemplo |
|---|---|---|
| Fecha | `yyyy-MM-dd` | `"2026-10-05"` |
| Hora | `HH:mm` (24 h) | `"16:00"` |
| Marca de tiempo | ISO-8601 con zona | `"2026-10-05T10:02:13-06:00"` |
| Día de la semana | `LUNES` · `MARTES` · `MIERCOLES` · `JUEVES` · `VIERNES` | `"MIERCOLES"` |
| Matrícula | Con prefijo `S` | `"S23014587"` |

El backend acepta los días con acentos o en minúsculas (`miércoles`) y los normaliza a mayúsculas sin acento. Sábado y domingo no se permiten.

### Envoltura de respuesta

Se conserva la misma estructura `ApiResponse<T>` de la versión anterior:

```json
{
  "success": true,
  "message": "Texto descriptivo",
  "data": { }
}
```

Error de negocio (`BusinessException`) → `400 Bad Request`:

```json
{ "success": false, "message": "La tutoría ya no tiene lugares disponibles.", "data": null }
```

Error de validación (`@Valid`) → `400` con `data` = mapa `campo → mensaje`:

```json
{ "success": false, "message": "Error de validación", "data": { "comentario": "No puede exceder 255 caracteres." } }
```

### Códigos HTTP

| Código | Caso |
|---|---|
| `200 OK` | Operación exitosa (también en altas). |
| `400 Bad Request` | Regla de negocio incumplida, recurso inexistente o validación fallida. |
| `401 Unauthorized` | JWT ausente, inválido o expirado. |
| `403 Forbidden` | El rol no tiene acceso al endpoint. |
| `404 Not Found` | La ruta no existe. |
| `405 Method Not Allowed` | Método HTTP no soportado en la ruta. |
| `500 Internal Server Error` | Error inesperado. |

Todos los errores, incluidos 401, 403 y 404, usan la envoltura `ApiResponse`.

Cuando un recurso existe pero pertenece a otro usuario (por ejemplo, la tutoría de otro tutor), la respuesta es `400` con el mensaje `"No tienes permiso sobre este recurso."`.

Si el body es inválido, la validación (`400`) ocurre antes de revisar el rol: un rol sin permiso con un body inválido recibe `400`, no `403`. En ningún caso se ejecuta la acción.

---

## Pantallas y endpoints

| # | Pantalla | Rol | Endpoints |
|---|---|---|---|
| 01 | Iniciar sesión | — | `POST /auth/microsoft`, `POST /auth/google`, `POST /auth/signin` (pruebas: `POST /auth/dev`) |
| 02 | Completar registro | — | Con proveedor: `POST /auth/registro`. A mano: `POST /auth/signup` |
| — | Barra lateral (nombre, matrícula, rol) | Todos | `GET /usuarios/me` |
| 03 | Explorar tutorías | TUTORADO | `GET /tutorias` |
| 04 | Detalle de tutoría | TUTORADO | `GET /tutorias/{id}`, `GET /tutorias/{id}/comentarios`, `GET /inscripciones/semana` |
| 05 | Confirmar inscripción | TUTORADO | `POST /inscripciones` |
| 06 | Panel de inscripción: estados | TUTORADO | `GET /tutorias/{id}` (campo `panelTutorado`) |
| 07 | Mis tutorías | TUTORADO | `GET /inscripciones` |
| 08 | Mi asistencia | TUTORADO | `GET /inscripciones/tutorias/{idTutoria}`, `DELETE /inscripciones/tutorias/{idTutoria}`, `POST /tutorias/{id}/comentarios`, `DELETE /comentarios/{id}` |
| 09 | Mis tutorías y sesiones de hoy | TUTOR | `GET /sesiones/hoy`, `GET /tutorias/mis-tutorias`, `POST /sesiones/abrir-lista`, `POST /sesiones/cancelar` |
| 10 | Crear tutoría | TUTOR | `GET /materias`, `GET /catalogos/edificios`, `POST /tutorias` |
| 11 | Detalle de tutoría | TUTOR | `GET /tutorias/{id}`, `GET /tutorias/{id}/sesiones`, `GET /tutorias/{id}/inscritos`, `GET /tutorias/{id}/comentarios`, `POST /horarios/{id}/temas`, `DELETE /temas/{id}`, `POST /sesiones/cancelar`, `PUT /tutorias/{id}`, `PATCH /tutorias/{id}/estado` |
| 12 | Pase de lista | TUTOR | `GET /sesiones/{id}/lista`, `PUT /sesiones/{id}/lista` |
| 13 | Cerrar pase de lista | TUTOR | `GET /sesiones/{id}/lista` (bloque `cierre`), `POST /sesiones/{id}/cerrar-lista` |
| 14 | Historial de asistencia | TUTOR | `GET /tutorias/mis-tutorias`, `GET /tutorias/{id}/historial-asistencia` |
| 15 | Tutorías de la facultad | ADMIN | `GET /admin/resumen`, `GET /admin/tutorias`, `GET /admin/tutores`, `PATCH /tutorias/{id}/estado`, `POST /admin/tutorias/cierre-periodo` |
| 16 | Experiencias Educativas | ADMIN | `GET /materias`, `POST /materias`, `PUT /materias/{id}`, `DELETE /materias/{id}` |

---

## Cambios respecto a la API anterior

| Antes | Ahora | Motivo |
|---|---|---|
| `POST /auth/signup`, `POST /auth/signin` (contraseña) | Se conservan y se suman `POST /auth/microsoft`, `POST /auth/google` y `POST /auth/registro` | Se puede entrar con proveedor externo o con contraseña. `signup` usa `apellidos` y `rol` por nombre, entra directo y acepta los campos viejos `pwd`, `apellidoP` y `apellidoM`; `signin` acepta matrícula o correo y responde `{ token, rol, usuario }`. |
| `/tutoria` = una sesión con fecha | `/tutorias` = tutoría recurrente con horarios semanales | Una tutoría tiene horarios, y cada horario genera sesiones. |
| `/horario` = disponibilidad del tutor | Los horarios pertenecen a una tutoría (`/tutorias`, `/horarios/{id}/temas`) | Nuevo modelo. |
| `/asistencia` = inscripción + asistencia | `/inscripciones` y `/sesiones/{id}/lista` | Inscribirse y pasar lista son acciones distintas. |
| `/calificaciones` | **Eliminado** | Ya no está en el modelo ni en los mockups. |
| `/materia/{nrc}` (público) | `/materias/{idMateria}` (requiere token) | El NRC se puede editar; el id no cambia. |
| `@PreAuthorize` sin efecto | Seguridad por método activada | Antes no existía `@EnableMethodSecurity`: los roles por endpoint no se aplicaban. |
| `/comentarios`, `/temas` | `/tutorias/{id}/comentarios`, `/horarios/{id}/temas` | Comentarios por tutoría; temas por horario. |
| Matrícula numérica (8 dígitos) | Matrícula texto con `S` (`S23014587`) | `usuario.matricula` vuelve a `varchar(50)`. |
| Cupo 5 | Cupo 7 | Mockups. |

También se eliminan la entidad `Permisos` y la columna `asistencia.calificacion`.

---

## Supuestos por confirmar

Puntos que las respuestas no cubrieron. **Ya están implementados así**; si alguno cambia, se ajusta el código. Las etiquetas `[S#]` aparecen en los demás documentos donde aplican.

| Id | Supuesto |
|---|---|
| **S1** | El pase de lista se abre **solo el mismo día** de la sesión (y a partir de su hora de inicio). Una sesión de un día pasado que nunca se abrió no queda registrada ni cuenta para nada. |
| **S2** | Edificio y aula también se pueden cambiar **solo sin inscritos activos**, igual que los horarios. |
| **S3** | Al desactivar una tutoría: se avisa por correo a los inscritos activos, sus inscripciones conservan el estado `ACTIVA` (la tutoría pasa a la lista de "anteriores" del tutorado) y no se puede reactivar. No se puede desactivar mientras haya un pase de lista abierto. |
| **S4** | Si un tutorado se da de baja voluntariamente y se vuelve a inscribir, su conteo de inasistencias empieza en 0. |
| **S5** | La baja voluntaria no tiene restricción de horario y envía un correo de confirmación. |
| **S6** | Un comentario solo lo pueden borrar su autor o un ADMIN. |
| **S7** | El aviso de "lista sin cerrar" es un correo al tutor que se envía una vez, al llegar la hora de fin de la sesión (`sesion.fecha_aviso_cierre`). |
| **S8** | El catálogo de edificios y aulas vive en la tabla `aula` de esta BD. **El script trae edificios 1–2 con aulas 1–12 como provisional**: hay que sustituirlo por la lista real. |
