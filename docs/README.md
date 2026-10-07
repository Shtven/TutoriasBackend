# Sistema de Tutorías — Documentación del backend

Documentación del prototipo de pruebas. Está alineada con:

- **Esquema de BD:** `TutorConnect.sql` (export de dbdiagram), resumido en [MODELO_DE_DATOS.md](MODELO_DE_DATOS.md).
- **Mockups de escritorio** del 5 oct 2026 (16 pantallas).

> Esta carpeta describe el comportamiento **objetivo** del backend. El código actual todavía corresponde al esquema anterior; la implementación se hará a partir de estos documentos.

## Documentos

| Documento | Contenido |
|---|---|
| [AUTENTICACION.md](AUTENTICACION.md) | Cómo funciona el login con Microsoft y Google, registro en el primer acceso, JWT propio, campo de proveedor y opciones para entrar al panel de administración. |
| [API.md](API.md) | Referencia de todos los endpoints: ruta, roles, body, respuesta y validaciones. |
| [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md) | Estados, cupo, choques de horario, sesiones y pase de lista, bajas, recordatorios y correos. |
| [MODELO_DE_DATOS.md](MODELO_DE_DATOS.md) | Tablas del esquema recibido y cambios recomendados. |

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
| `500 Internal Server Error` | Error inesperado. |

Cuando un recurso existe pero pertenece a otro usuario (por ejemplo, la tutoría de otro tutor), la respuesta es `400` con el mensaje `"No tienes permiso sobre este recurso."`.

---

## Pantallas y endpoints

| # | Pantalla | Rol | Endpoints |
|---|---|---|---|
| 01 | Iniciar sesión | — | `POST /auth/microsoft`, `POST /auth/google` |
| 02 | Completar registro | — | `POST /auth/registro` |
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
| `POST /auth/signup`, `POST /auth/signin` (contraseña) | `POST /auth/microsoft`, `POST /auth/google`, `POST /auth/registro` | Acceso solo con proveedor externo; ya no se guardan contraseñas. |
| `/tutoria` = una sesión con fecha | `/tutorias` = tutoría recurrente con horarios semanales | Una tutoría tiene horarios, y cada horario genera sesiones. |
| `/horario` = disponibilidad del tutor | Los horarios pertenecen a una tutoría (`/tutorias`, `/horarios/{id}/temas`) | Nuevo modelo. |
| `/asistencia` = inscripción + asistencia | `/inscripciones` y `/sesiones/{id}/lista` | Inscribirse y pasar lista son acciones distintas. |
| `/calificaciones` | **Eliminado** | Ya no está en el modelo ni en los mockups. |
| `/materia/{nrc}` (público) | `/materias/{idMateria}` (requiere token) | El NRC se puede editar; el id no cambia. |
| `/comentarios`, `/temas` | `/tutorias/{id}/comentarios`, `/horarios/{id}/temas` | Comentarios por tutoría; temas por horario. |
| Matrícula numérica (8 dígitos) | Matrícula texto con `S` (`S23014587`) | `usuario.matricula` vuelve a `varchar(50)`. |
| Cupo 5 | Cupo 7 | Mockups. |

También se eliminan la entidad `Permisos` y la columna `asistencia.calificacion`.

---

## Supuestos por confirmar

Puntos que las respuestas no cubrieron. La documentación los asume como se indica; las etiquetas `[S#]` aparecen en los demás documentos donde aplican.

| Id | Supuesto |
|---|---|
| **S1** | El pase de lista se abre **solo el mismo día** de la sesión (y a partir de su hora de inicio). Una sesión de un día pasado que nunca se abrió no queda registrada ni cuenta para nada. |
| **S2** | Edificio y aula también se pueden cambiar **solo sin inscritos activos**, igual que los horarios. |
| **S3** | Al desactivar una tutoría: se avisa por correo a los inscritos activos, sus inscripciones conservan el estado `ACTIVA` (la tutoría pasa a la lista de "anteriores" del tutorado) y no se puede reactivar. No se puede desactivar mientras haya un pase de lista abierto. |
| **S4** | Si un tutorado se da de baja voluntariamente y se vuelve a inscribir, su conteo de inasistencias empieza en 0. |
| **S5** | La baja voluntaria no tiene restricción de horario y envía un correo de confirmación. |
| **S6** | Un comentario solo lo pueden borrar su autor o un ADMIN. |
| **S7** | El aviso de "lista sin cerrar" es un correo al tutor que se envía una vez, al llegar la hora de fin de la sesión (ver recomendación en [MODELO_DE_DATOS.md](MODELO_DE_DATOS.md#cambios-recomendados)). |
| **S8** | El catálogo de edificios y aulas se importa a una tabla de esta misma BD (ver [MODELO_DE_DATOS.md](MODELO_DE_DATOS.md#cambios-recomendados)). |
