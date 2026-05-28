# API — Sistema de Tutorías FNT

Documentación de todos los endpoints del backend (Spring Boot 4.x).

- **Base URL local:** `http://localhost:8080`
- **Formato:** JSON (`Content-Type: application/json`)
- **Autenticación:** JWT en header `Authorization: Bearer <token>` (excepto `/auth/**`)
- **Roles disponibles:** `TUTOR`, `TUTORADO`, `ADMIN`

---

## Envoltura de respuesta

Todas las respuestas usan la misma estructura `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Texto descriptivo",
  "data": { /* T, puede ser null */ }
}
```

En errores de negocio (`BusinessException`) responde `400 Bad Request`:
```json
{ "success": false, "message": "Mensaje de error", "data": null }
```

En errores de validación (`@Valid`) responde `400` con `data` = mapa `campo -> mensaje`.

---

## 1. Autenticación — `/auth` (público)

### `POST /auth/signup`
Registra un nuevo usuario.

**Body**
```json
{
  "matricula": "S20001234",
  "nombre": "Ana",
  "apellidoP": "Pérez",
  "apellidoM": "López",
  "correo": "ana@example.com",
  "pwd": "12345678",
  "rol": 1
}
```
Validaciones: matrícula/nombre/apellidos/correo/pwd no vacíos, correo formato válido, pwd ≥ 8 caracteres, `rol` ∈ {TUTOR(1), TUTORADO(2)}; matrícula y correo únicos.

**Response 200**
```json
{ "success": true, "message": "Registro exitoso.", "data": null }
```

---

### `POST /auth/signin`
Login con matrícula y contraseña.

**Body**
```json
{ "matricula": "S20001234", "pwd": "12345678" }
```

**Response 200**
```json
{
  "success": true,
  "message": "Login",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "rol": "TUTOR"
  }
}
```

---

## 2. Tutorías — `/tutoria`

### `POST /tutoria` — `TUTOR`, `ADMIN`
Crea una sesión. Los `temas` son opcionales (RF14).

**Body**
```json
{
  "idHorario": 1,
  "edificio": 2,
  "aula": 304,
  "nrc": 12345,
  "fecha": "2026-06-10",
  "temas": ["Integrales por partes", "Sustitución trigonométrica"]
}
```

**Response 200**
```json
{ "success": true, "message": "Tutoria creada.", "data": null }
```

---

### `PUT /tutoria/{id}` — `TUTOR` (dueño), `ADMIN`
Modifica una sesión existente. No permite cambiar la materia.

**Body** (`ActualizarTutoriaRequest`)
```json
{ "idHorario": 1, "edificio": 2, "aula": 304, "fecha": "2026-06-12" }
```

**Response 200** → `"Tutoria actualizada."`

---

### `DELETE /tutoria/{id}` — `TUTOR` (dueño), `ADMIN`
Cancela la sesión y notifica por correo a inscritos. Bloqueado a menos de 15 min del inicio (RF08).

**Response 200** → `"Tutoria cancelada."`

---

### `GET /tutoria/{id}` — `TUTOR`, `TUTORADO`, `ADMIN`
Detalle de la sesión.

**Response 200**
```json
{
  "success": true,
  "message": "Tutoria",
  "data": {
    "id": 5,
    "fecha": "2026-06-10",
    "nombreTutor": "Ana Pérez López",
    "horaInicio": "10:00:00",
    "horaFin": "11:00:00",
    "materia": "Cálculo II",
    "edificio": 2,
    "aula": 304,
    "estado": "PROGRAMADA",
    "temas": [
      { "idTema": 12, "tema": "Integrales por partes" }
    ]
  }
}
```

---

### `GET /tutoria/mis-tutorias` — `TUTOR`, `ADMIN`
Sesiones `PROGRAMADA` del tutor autenticado (RF03).

**Response 200** → `data: List<TutoriaResponsive>`

---

### `GET /tutoria/disponibles` — `TUTORADO`, `ADMIN`
Sesiones `PROGRAMADA` con `fecha >= today` (RF10).

**Response 200** → `data: List<TutoriaResponsive>`

---

### `PUT /tutoria/completar/{id}` — `TUTOR` (dueño), `ADMIN`
Marca como `COMPLETADA` (RF09). Requiere que ya haya iniciado y no esté COMPLETADA/CANCELADA.

**Response 200** → `"Tutoria completa."`

---

## 3. Horarios — `/horario` (`TUTOR`, `ADMIN`)

### `POST /horario`
Crea un horario para el tutor autenticado (RF04).

**Body**
```json
{ "dia": "LUNES", "horaInicio": "10:00:00", "horaFin": "11:00:00" }
```

**Response 200** → `"Horario creado."`

---

### `PUT /horario/{id}` — dueño
Modifica el horario. Bloquea solapamientos con otros horarios del mismo tutor.

**Body** igual al POST.

**Response 200** → `"Horario actualizado."`

---

### `DELETE /horario/{id}` — dueño
Elimina el horario. Falla con mensaje claro si hay tutorías que lo referencian.

**Response 200** → `"Horario eliminado."`

---

### `GET /horario/{id}`
Detalle.

**Response 200**
```json
{
  "success": true,
  "message": "Horario",
  "data": { "id": 1, "dia": "LUNES", "horaInicio": "10:00:00", "horaFin": "11:00:00" }
}
```

---

### `GET /horario`
Horarios del tutor autenticado.

**Response 200** → `data: List<HorarioResponsive>`

---

## 4. Materias — `/materia`

> ⚠️ Atención: el body usa el campo `nombre`, no `materia`.

### `POST /materia` — `ADMIN`
**Body**
```json
{ "nombre": "Cálculo II", "nrc": 12345 }
```
**Response 200** → `"Materia creada."`

### `PUT /materia/{nrc}` — `ADMIN`
Mismo body.
**Response 200** → `"Materia actualizada."`

### `DELETE /materia/{nrc}` — `ADMIN`
**Response 200** → `"Materia eliminada."`

### `GET /materia/{nrc}` — `TUTOR`, `ADMIN`
**Response 200**
```json
{ "success": true, "message": "Materia",
  "data": { "idMateria": 1, "materia": "Cálculo II", "nrc": 12345 } }
```

### `GET /materia` — `TUTOR`, `ADMIN`
**Response 200** → `data: List<Materia>`

---

## 5. Asistencias / Inscripciones — `/asistencia`

### `POST /asistencia` — `TUTORADO`, `ADMIN`
Inscribe al tutorado autenticado (RF11). Valida solapamiento, cupo (5) y notifica por correo.

**Body**
```json
{ "idTutoria": 5 }
```
**Response 200** → `"Asistencia marcada."`

---

### `DELETE /asistencia/{idAsistencia}` — `TUTORADO` (dueño), `ADMIN`
Cancela inscripción (RF08). Bloqueado a menos de 15 min del inicio.

**Response 200** → `"Asistencia eliminada."`

---

### `PATCH /asistencia/{idAsistencia}?asistio={true|false}` — `TUTOR`, `ADMIN`
Marca asistencia real del tutorado.

**Response 200** → `"Asistencia actualizada."`

---

### `GET /asistencia/{idAsistencia}` — `TUTOR`, `ADMIN`
Detalle de una inscripción.

**Response 200**
```json
{
  "success": true,
  "message": "Asistencia",
  "data": {
    "idAsistencia": 7,
    "matricula": "S20001234",
    "nombre": "Ana Pérez López",
    "asistio": false,
    "calificacion": null
  }
}
```

---

### `GET /asistencia/tutoria/{idTutoria}` — `TUTOR`, `ADMIN`
Inscritos en una tutoría (RF06).

**Response 200** → `data: List<AsistenciaResponsive>`

---

### `GET /asistencia/mis-inscripciones` — `TUTORADO`, `ADMIN`
Inscripciones del tutorado autenticado (todos los estados).

**Response 200** → `data: List<AsistenciaResponsive>`

---

### `GET /asistencia/historial` — `TUTORADO`, `ADMIN`
Inscripciones del tutorado en sesiones `COMPLETADA` (CU-05 paso 1).

**Response 200** → `data: List<AsistenciaResponsive>`

---

## 6. Comentarios — `/comentarios` (RF15)

### `POST /comentarios` — `TUTORADO`, `ADMIN`
Deja un comentario en una sesión donde el tutorado está inscrito (CU-06). Estado `PROGRAMADA`, máx. 255 caracteres.

**Body**
```json
{ "idTutoria": 5, "comentario": "¿Podemos ver derivadas implícitas?" }
```
**Response 200** → `"Comentario registrado."`

---

### `GET /comentarios/tutoria/{idTutoria}` — `TUTOR`, `TUTORADO`, `ADMIN`
Comentarios de la sesión (vista del tutor).

**Response 200**
```json
{
  "success": true,
  "message": "Comentarios de la tutoría",
  "data": [
    {
      "idComentario": 3,
      "idTutoria": 5,
      "matricula": "S20001234",
      "nombre": "Ana Pérez López",
      "comentario": "¿Podemos ver derivadas implícitas?"
    }
  ]
}
```

---

### `GET /comentarios/mis-comentarios` — `TUTORADO`, `ADMIN`
Historial propio de comentarios.

**Response 200** → `data: List<ComentarioResponsive>`

---

### `DELETE /comentarios/{idComentario}` — `TUTORADO` (dueño), `ADMIN`
**Response 200** → `"Comentario eliminado."`

---

## 7. Temas — `/temas` (RF14)

### `POST /temas` — `TUTOR` (dueño), `ADMIN`
Agrega un tema a una sesión `PROGRAMADA`.

**Body**
```json
{ "idTutoria": 5, "tema": "Integrales por partes" }
```
**Response 200** → `"Tema agregado."`

---

### `GET /temas/tutoria/{idTutoria}` — `TUTOR`, `TUTORADO`, `ADMIN`
Lista los temas de una sesión.

**Response 200**
```json
{
  "success": true,
  "message": "Temas de la tutoría",
  "data": [
    { "idTema": 12, "tema": "Integrales por partes" }
  ]
}
```

---

### `DELETE /temas/{idTema}` — `TUTOR` (dueño), `ADMIN`
**Response 200** → `"Tema eliminado."`

---

## 8. Calificaciones — `/calificaciones` (RF13)

### `POST /calificaciones` — `TUTORADO`, `ADMIN`
Califica una sesión `COMPLETADA` (CU-05). Una sola vez por inscripción, valor 1–5.

**Body**
```json
{ "idAsistencia": 7, "calificacion": 5 }
```
**Response 200** → `"Calificación registrada."`

---

### `GET /calificaciones/promedio/{matricula}` — `TUTOR`, `TUTORADO`, `ADMIN`
Promedio del tutor con esa matrícula.

**Response 200**
```json
{
  "success": true,
  "message": "Promedio del tutor",
  "data": {
    "matricula": "S19000123",
    "nombre": "Carlos Ruiz Vega",
    "promedio": 4.6,
    "totalCalificaciones": 12
  }
}
```
> `promedio` es `null` cuando no hay calificaciones todavía.

---

### `GET /calificaciones/promedio` — `TUTOR`, `ADMIN`
Promedio propio del tutor autenticado.

**Response 200** → mismo shape que el endpoint anterior.

---

## Estados de la tutoría

Definidos en [`EstadosTutoria.java`](../src/main/java/com/codespace/tutorias/Helpers/EstadosTutoria.java):

| Estado | Significado | Operaciones permitidas |
|---|---|---|
| `PROGRAMADA` | Sesión agendada y disponible. Equivale a "ACTIVA" en el CU-06. | Inscribirse, cancelar, modificar, comentar, agregar temas. |
| `COMPLETADA` | Sesión finalizada por el tutor (RF09). | Calificar (RF13). |
| `CANCELADA` | Cancelada por el tutor o el sistema. | Solo lectura. |

---

## Códigos de respuesta

| Código | Caso |
|---|---|
| `200 OK` | Operación exitosa. |
| `400 Bad Request` | `BusinessException` (lógica de negocio) o validación `@Valid` fallida. |
| `401 Unauthorized` | JWT inválido, expirado o ausente. |
| `403 Forbidden` | Rol insuficiente para el endpoint. |
| `500 Internal Server Error` | Error inesperado. |

---

## Notas para el frontend

- Toda petición protegida debe llevar `Authorization: Bearer <token>` con el JWT del `signin`.
- CORS restringido a orígenes exactos vía `CORS_ALLOWED_ORIGINS` (lista separada por coma, p.ej. `http://localhost:5173,https://mi-app.onrender.com`). Con `allowCredentials=true` no se admite `*`.
- Fechas: `LocalDate` (`yyyy-MM-dd`); horas: `LocalTime` (`HH:mm:ss`).
- El campo `idAsistencia` ahora viene en `AsistenciaResponsive` — necesario para llamar a `POST /calificaciones`.
