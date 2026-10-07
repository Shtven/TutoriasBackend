# API — Sistema de Tutorías

Referencia de endpoints. Las convenciones generales (envoltura `ApiResponse`, formatos, códigos HTTP) están en [README.md](README.md#convenciones-generales). Las reglas que se citan como **RN-x** están en [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md).

En cada endpoint, **Roles** indica quién puede llamarlo. **(dueño)** significa que además debe ser el tutor de esa tutoría; un `ADMIN` siempre pasa la verificación de dueño.

## Índice

0. [Objetos comunes](#0-objetos-comunes)
1. [Autenticación — `/auth`](#1-autenticación--auth-público)
2. [Usuarios — `/usuarios`](#2-usuarios--usuarios)
3. [Experiencias Educativas — `/materias`](#3-experiencias-educativas--materias)
4. [Catálogos — `/catalogos`](#4-catálogos--catalogos)
5. [Tutorías — `/tutorias`](#5-tutorías--tutorias)
6. [Temas — `/horarios/{id}/temas`, `/temas`](#6-temas)
7. [Inscripciones — `/inscripciones`](#7-inscripciones--inscripciones)
8. [Sesiones y pase de lista — `/sesiones`](#8-sesiones-y-pase-de-lista--sesiones)
9. [Comentarios](#9-comentarios)
10. [Administración — `/admin`](#10-administración--admin)
11. [Matriz de permisos](#11-matriz-de-permisos)

---

## 0. Objetos comunes

Estos objetos se repiten en varias respuestas.

### `Materia`
```json
{ "idMateria": 5, "nombre": "Estructuras de Datos", "nrc": 48215 }
```
`nrc` corresponde a la columna `materia.identificador`.

### `UsuarioResumen`
```json
{ "matricula": "S21009342", "nombre": "Rocío", "apellidos": "Hernández Aguilar", "nombreCompleto": "Rocío Hernández Aguilar" }
```

### `Horario`
```json
{
  "idHorario": 31,
  "dia": "LUNES",
  "horaInicio": "10:00",
  "horaFin": "12:00",
  "estado": "ACTIVO",
  "temas": [ { "idTema": 101, "tema": "Listas enlazadas" } ]
}
```
En los listados solo se incluyen horarios `ACTIVO`. `temas` solo viene en las vistas de detalle.

### `Cupo`
```json
{ "ocupados": 4, "maximo": 7, "disponibles": 3 }
```
`ocupados` = tutorados con inscripción `ACTIVA` (RN-3).

### `TutoriaResumen`
```json
{
  "idTutoria": 12,
  "materia": { "idMateria": 5, "nombre": "Estructuras de Datos", "nrc": 48215 },
  "tutor": { "matricula": "S21009342", "nombre": "Rocío", "apellidos": "Hernández Aguilar", "nombreCompleto": "Rocío Hernández Aguilar" },
  "edificio": 2,
  "aula": 7,
  "estado": "ACTIVA",
  "horarios": [
    { "idHorario": 31, "dia": "LUNES", "horaInicio": "10:00", "horaFin": "12:00", "estado": "ACTIVO" },
    { "idHorario": 32, "dia": "MIERCOLES", "horaInicio": "10:00", "horaFin": "12:00", "estado": "ACTIVO" }
  ],
  "cupo": { "ocupados": 4, "maximo": 7, "disponibles": 3 }
}
```

### `Sesion`
Representa una ocurrencia de un horario en una fecha. Puede existir en BD o ser **virtual** (`idSesion: null`), es decir, una sesión futura que todavía no tiene fila (RN-6).

```json
{
  "idSesion": 88,
  "idHorario": 31,
  "idTutoria": 12,
  "materia": "Estructuras de Datos",
  "fecha": "2026-10-05",
  "dia": "LUNES",
  "horaInicio": "10:00",
  "horaFin": "12:00",
  "edificio": 2,
  "aula": 7,
  "estado": "LISTA_ABIERTA",
  "inscritos": 5,
  "marcados": 5,
  "asistieron": 4,
  "recordatorioA": "08:00",
  "terminada": true,
  "acciones": { "abrirLista": false, "cerrarLista": true, "cancelar": false }
}
```

| Campo | Significado |
|---|---|
| `estado` | `PROGRAMADA` · `LISTA_ABIERTA` · `COMPLETADA` · `CANCELADA` (RN-6). |
| `inscritos` | `PROGRAMADA` / `LISTA_ABIERTA`: inscritos activos. `COMPLETADA`: tutorados con marca. |
| `marcados` / `asistieron` | Conteo de marcas de asistencia (`0` si no hay fila). |
| `recordatorioA` | Hora a la que se envía el recordatorio (`horaInicio − 2 h`). |
| `terminada` | `true` si ya pasó `horaFin`. Se usa para el aviso "La sesión terminó. Cierra la lista". |
| `acciones` | Qué botones habilitar en la UI, según RN-7 a RN-9. |

---

## 1. Autenticación — `/auth` (público)

Flujo completo y configuración en [AUTENTICACION.md](AUTENTICACION.md).

### `POST /auth/microsoft`
### `POST /auth/google`

Verifica el ID token del proveedor. Si el usuario ya existe, entrega el JWT propio; si no, inicia el registro.

**Body**
```json
{ "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..." }
```

**Response 200 — usuario registrado**
```json
{
  "success": true,
  "message": "Login",
  "data": {
    "registroPendiente": false,
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "usuario": {
      "matricula": "S23014587",
      "nombre": "Valeria",
      "apellidos": "Mendoza Cruz",
      "correo": "zS23014587@estudiantes.uv.mx",
      "rol": "TUTORADO"
    }
  }
}
```

**Response 200 — primer acceso**
```json
{
  "success": true,
  "message": "Registro pendiente",
  "data": {
    "registroPendiente": true,
    "tokenRegistro": "eyJhbGciOiJIUzI1NiJ9...",
    "perfil": {
      "nombre": "Sofía",
      "apellidos": "Ramos Gutiérrez",
      "correo": "zS24011236@estudiantes.uv.mx",
      "proveedor": "MICROSOFT"
    }
  }
}
```

**Errores (400)**

| Mensaje | Causa |
|---|---|
| `Token del proveedor inválido o expirado.` | Firma, `iss`, `aud` o `exp` no válidos. |
| `Solo se permiten cuentas @estudiantes.uv.mx.` | Microsoft: `tid` o dominio distinto. |
| `Solo se permiten cuentas @gmail.com.` | Google: dominio distinto o `email_verified = false`. |

---

### `POST /auth/registro`

Completa el primer acceso (pantalla 02). Nombre, apellidos, correo y proveedor se toman del `tokenRegistro`, no del body.

**Body**
```json
{ "tokenRegistro": "eyJhbGciOiJIUzI1NiJ9...", "matricula": "S24011236", "rol": "TUTORADO" }
```

**Validaciones**

| Campo | Regla |
|---|---|
| `tokenRegistro` | Firmado por el backend y vigente (15 min). |
| `matricula` | Se aplica `trim`, se quita el prefijo `z` si viene y la `S` se pasa a mayúscula. Debe quedar `S` + 8 dígitos (`S24011236`). Única: `"La matrícula ya está registrada con otra cuenta."` |
| `rol` | `TUTORADO` o `TUTOR`. Cualquier otro valor (incluido `ADMIN`) → `"Rol inválido."` |

**Response 200** → igual que el login de un usuario registrado (`registroPendiente: false`, `token`, `usuario`), con `message: "Registro exitoso."`.

---

## 2. Usuarios — `/usuarios`

### `GET /usuarios/me` — todos

Datos del usuario autenticado (barra lateral).

**Response 200**
```json
{
  "success": true,
  "message": "Usuario",
  "data": {
    "matricula": "S23014587",
    "nombre": "Valeria",
    "apellidos": "Mendoza Cruz",
    "correo": "zS23014587@estudiantes.uv.mx",
    "rol": "TUTORADO",
    "proveedor": "MICROSOFT"
  }
}
```

---

## 3. Experiencias Educativas — `/materias`

En la UI se llaman "Experiencias Educativas" (EE). El body usa los mismos nombres de campo que la respuesta (`nombre`, `nrc`).

### `GET /materias` — `TUTOR`, `ADMIN`

Catálogo ordenado por nombre. El tutor lo usa en el select de "Crear tutoría"; el admin, en la pantalla 16.

**Query params**

| Param | Tipo | Descripción |
|---|---|---|
| `q` | string | Opcional. Busca en el nombre (sin distinguir mayúsculas ni acentos) o por NRC exacto. |

**Response 200**
```json
{
  "success": true,
  "message": "Experiencias educativas",
  "data": [
    { "idMateria": 1, "nombre": "Álgebra Lineal", "nrc": 47702, "tutoriasActivas": 1 },
    { "idMateria": 13, "nombre": "Redes de Computadoras", "nrc": 52310, "tutoriasActivas": 0 }
  ]
}
```

### `GET /materias/{idMateria}` — `TUTOR`, `ADMIN`

**Response 200** → `data`: un objeto con la misma forma que los elementos de la lista.

### `POST /materias` — `ADMIN`

**Body**
```json
{ "nombre": "Arquitectura de Computadoras", "nrc": 52325 }
```

| Campo | Regla |
|---|---|
| `nombre` | Obligatorio; máximo 100 caracteres. |
| `nrc` | Obligatorio; entero positivo; único → `"Este NRC ya está registrado para Redes de Computadoras."` |

**Response 200** → `message: "Experiencia educativa creada."`, `data`: la EE creada.

### `PUT /materias/{idMateria}` — `ADMIN`

Mismo body y mismas validaciones (el NRC debe ser único, sin contar a la propia EE).

**Response 200** → `message: "Experiencia educativa actualizada."`

### `DELETE /materias/{idMateria}` — `ADMIN`

Solo si **ninguna** tutoría (activa o inactiva) la usa: `"No se puede eliminar: la experiencia educativa tiene tutorías registradas."`

**Response 200** → `message: "Experiencia educativa eliminada."`

---

## 4. Catálogos — `/catalogos`

### `GET /catalogos/edificios` — `TUTOR`, `ADMIN`

Edificios con sus aulas, para los selects de la pantalla 10. El origen del catálogo está en el supuesto **[S8]**.

**Response 200**
```json
{
  "success": true,
  "message": "Edificios",
  "data": [
    { "edificio": 1, "aulas": [1, 2, 3, 4, 5, 6, 8, 9, 10, 12] },
    { "edificio": 2, "aulas": [2, 4, 7, 9, 11] }
  ]
}
```

---

## 5. Tutorías — `/tutorias`

### `GET /tutorias` — `TUTORADO`

**Explorar tutorías** (pantalla 03). Devuelve tutorías `ACTIVA` **excluyendo** aquellas en las que el tutorado tiene una inscripción `ACTIVA` o alguna vez tuvo `BAJA_AUTOMATICA` (RN-12). Las tutorías llenas sí se incluyen.

**Query params** (todos opcionales)

| Param | Tipo | Descripción |
|---|---|---|
| `q` | string | Busca en el nombre de la EE o del tutor. |
| `dia` | string | `LUNES`…`VIERNES`: solo tutorías con algún horario ese día. |
| `soloConLugar` | boolean | Solo con `cupo.disponibles > 0`. |
| `ocultarChoques` | boolean | Excluye las que chocan con las inscripciones activas del tutorado (RN-4). |

**Response 200** → `data`: lista de `TutoriaResumen` más el campo `choques`:
```json
{
  "success": true,
  "message": "Tutorías disponibles",
  "data": [
    {
      "idTutoria": 20,
      "materia": { "idMateria": 9, "nombre": "Probabilidad y Estadística", "nrc": 47790 },
      "tutor": { "matricula": "S21007711", "nombre": "Daniela", "apellidos": "Ortiz Méndez", "nombreCompleto": "Daniela Ortiz Méndez" },
      "edificio": 1,
      "aula": 6,
      "estado": "ACTIVA",
      "horarios": [ { "idHorario": 45, "dia": "JUEVES", "horaInicio": "17:00", "horaFin": "19:00", "estado": "ACTIVO" } ],
      "cupo": { "ocupados": 2, "maximo": 7, "disponibles": 5 },
      "choques": [
        {
          "dia": "JUEVES",
          "horaInicio": "17:00",
          "horaFin": "19:00",
          "conTutoria": { "idTutoria": 7, "materia": "Bases de Datos", "horaInicio": "16:00", "horaFin": "18:00" }
        }
      ]
    }
  ]
}
```
`choques` viene vacío si no hay cruce.

---

### `GET /tutorias/{idTutoria}` — todos

Detalle de la tutoría (pantallas 04, 06 y 11). Los horarios incluyen sus `temas`.

**Response 200**
```json
{
  "success": true,
  "message": "Tutoría",
  "data": {
    "idTutoria": 12,
    "materia": { "idMateria": 5, "nombre": "Estructuras de Datos", "nrc": 48215 },
    "tutor": { "matricula": "S21009342", "nombre": "Rocío", "apellidos": "Hernández Aguilar", "nombreCompleto": "Rocío Hernández Aguilar" },
    "edificio": 2,
    "aula": 7,
    "estado": "ACTIVA",
    "horarios": [
      { "idHorario": 31, "dia": "LUNES", "horaInicio": "10:00", "horaFin": "12:00", "estado": "ACTIVO",
        "temas": [ { "idTema": 101, "tema": "Listas enlazadas" }, { "idTema": 102, "tema": "Pilas y colas" } ] },
      { "idHorario": 32, "dia": "MIERCOLES", "horaInicio": "10:00", "horaFin": "12:00", "estado": "ACTIVO",
        "temas": [ { "idTema": 104, "tema": "Árboles binarios de búsqueda" } ] }
    ],
    "cupo": { "ocupados": 4, "maximo": 7, "disponibles": 3 },
    "proximaSesion": { "idHorario": 32, "fecha": "2026-10-07", "dia": "MIERCOLES", "horaInicio": "10:00", "horaFin": "12:00", "recordatorioA": "08:00" },
    "esDueno": false,
    "panelTutorado": {
      "estado": "CON_LUGAR",
      "puedeInscribirse": true,
      "choques": [],
      "inscripcion": null
    }
  }
}
```

| Campo | Descripción |
|---|---|
| `proximaSesion` | Siguiente ocurrencia no cancelada de cualquier horario `ACTIVO`. `null` si la tutoría está `INACTIVA`. |
| `esDueno` | `true` si el usuario autenticado es el tutor. |
| `panelTutorado` | Solo cuando el rol es `TUTORADO`; para los demás roles es `null`. |

**`panelTutorado.estado`** (pantalla 06). Se evalúa en este orden y gana el primero que se cumpla:

| Estado | Condición | `puedeInscribirse` |
|---|---|---|
| `BAJA_AUTOMATICA` | El tutorado tiene una inscripción `BAJA_AUTOMATICA` en esta tutoría. | `false` |
| `NO_ACTIVA` | Tutoría `INACTIVA`. | `false` |
| `INSCRIPCION_ACTIVA` | El tutorado tiene una inscripción `ACTIVA` en esta tutoría. | `false` |
| `LLENA` | `cupo.disponibles = 0`. | `false` |
| `CHOQUE_HORARIO` | Algún horario choca con sus inscripciones activas; `choques` trae el detalle. | `false` |
| `CON_LUGAR` | Ninguna de las anteriores. | `true` |

En `INSCRIPCION_ACTIVA` y `BAJA_AUTOMATICA`, `inscripcion` viene así:
```json
{
  "estado": "ACTIVA",
  "fechaInscripcion": "2026-10-05T09:14:00-06:00",
  "fechaBaja": null,
  "inasistencias": 0,
  "inasistenciasPermitidas": 2
}
```

---

### `GET /tutorias/mis-tutorias` — `TUTOR`

Tutorías del tutor autenticado (pantalla 09, bloque "Tus tutorías", y selector de la pantalla 14).

**Query params**

| Param | Valores | Default |
|---|---|---|
| `estado` | `ACTIVA` · `INACTIVA` · `TODAS` | `ACTIVA` |

**Response 200** → lista de `TutoriaResumen` más:
```json
{
  "proximaSesion": { "fecha": "2026-10-06", "dia": "MARTES", "horaInicio": "12:00", "horaFin": "14:00" },
  "tutoradosEnRiesgo": 1
}
```
`tutoradosEnRiesgo` = inscritos activos con exactamente 2 inasistencias (RN-11). Corresponde a la alerta "1 tutorado ya tiene 2 inasistencias".

---

### `POST /tutorias` — `TUTOR`

Crea una tutoría (pantalla 10). El tutor es el usuario autenticado. Estado inicial: `ACTIVA`; horarios: `ACTIVO`. El cupo es fijo (7) y no se envía.

**Body**
```json
{
  "idMateria": 13,
  "edificio": 1,
  "aula": 10,
  "horarios": [
    { "dia": "MARTES", "horaInicio": "08:00", "horaFin": "10:00",
      "temas": ["Modelo OSI", "Direccionamiento IPv4", "Cálculo de subredes"] },
    { "dia": "VIERNES", "horaInicio": "12:00", "horaFin": "14:00",
      "temas": ["Enrutamiento estático", "VLAN"] }
  ]
}
```

**Validaciones** (RN-1, RN-2, RN-5)

| Regla | Mensaje |
|---|---|
| La EE existe. | `Experiencia educativa no encontrada.` |
| Edificio y aula existen en el catálogo. | `El aula seleccionada no existe.` |
| Al menos 1 horario. | `Agrega al menos un horario.` |
| `dia` entre `LUNES` y `VIERNES`. | `Día inválido: solo de lunes a viernes.` |
| `horaInicio < horaFin`. | `La hora de fin debe ser posterior a la de inicio.` |
| Los horarios de la tutoría no se traslapan entre sí. | `Los horarios 1 y 2 se traslapan.` |
| No choca con otra tutoría `ACTIVA` del mismo tutor. | `El horario del martes 08:00–10:00 choca con tu tutoría de Bases de Datos.` |
| El aula no está ocupada a esa hora. | `El Edificio 1 · Aula 10 está ocupado el martes de 08:00 a 10:00.` |
| Temas: máximo 10 por horario, de 1 a 60 caracteres, sin repetirse en el mismo horario. | `Máximo 10 temas por horario.` / `El tema no puede exceder 60 caracteres.` |

**Response 200** → `message: "Tutoría creada."`, `data`: la tutoría con la forma de `GET /tutorias/{id}`.

---

### `PUT /tutorias/{idTutoria}` — `TUTOR` (dueño)

**Editar tutoría.** Reemplaza el lugar y el conjunto completo de horarios (con sus temas), reutilizando el formulario de la pantalla 10. **Solo se permite sin inscritos activos** (RN-5, [S2]). Para cambiar únicamente temas con inscritos, ver [§6](#6-temas).

**Body**
```json
{
  "edificio": 2,
  "aula": 7,
  "horarios": [
    { "idHorario": 31, "dia": "LUNES", "horaInicio": "10:00", "horaFin": "12:00", "temas": ["Listas enlazadas", "Pilas y colas"] },
    { "dia": "JUEVES", "horaInicio": "10:00", "horaFin": "12:00", "temas": ["Grafos"] }
  ]
}
```

Comportamiento con los horarios:

| Caso | Efecto |
|---|---|
| Viene con `idHorario` y no cambian `dia`/horas | Se conserva; sus temas se reemplazan por la lista enviada. |
| Viene con `idHorario` y cambian `dia`/horas | El horario anterior pasa a `INACTIVO` y se crea uno nuevo. Así se conserva el historial de sesiones. |
| Viene sin `idHorario` | Se crea (`ACTIVO`). |
| Un horario `ACTIVO` no viene en la lista | Pasa a `INACTIVO`. |

**Validaciones:** las mismas de `POST /tutorias`, más:

| Regla | Mensaje |
|---|---|
| Tutoría `ACTIVA`. | `La tutoría no está activa.` |
| 0 inscritos activos. | `No se pueden cambiar horarios ni lugar con tutorados inscritos.` |
| Ningún pase de lista abierto. | `Cierra el pase de lista abierto antes de editar.` |

**Response 200** → `message: "Tutoría actualizada."`, `data`: tutoría actualizada.

---

### `PATCH /tutorias/{idTutoria}/estado` — `TUTOR` (dueño), `ADMIN`

Desactiva la tutoría: el tutor cuando ya no quiere impartirla; el admin al final del periodo (RN-13, [S3]).

**Body**
```json
{ "estado": "INACTIVA" }
```

| Regla | Mensaje |
|---|---|
| Solo se acepta `INACTIVA` (no hay reactivación). | `Estado inválido.` |
| La tutoría está `ACTIVA`. | `La tutoría ya está inactiva.` |
| No hay un pase de lista abierto. | `Cierra el pase de lista abierto antes de desactivar.` |

**Efectos:** deja de aceptar inscripciones, deja de generar sesiones y recordatorios, y se envía un correo a los inscritos activos.

**Response 200** → `message: "Tutoría desactivada."`

---

### `GET /tutorias/{idTutoria}/inscritos` — `TUTOR` (dueño), `ADMIN`

Panel "Inscritos" de la pantalla 11.

**Response 200**
```json
{
  "success": true,
  "message": "Inscritos",
  "data": {
    "cupo": { "ocupados": 5, "maximo": 7, "disponibles": 2 },
    "inscritos": [
      { "matricula": "S23016620", "nombreCompleto": "Andrea Morales Pineda", "correo": "zS23016620@estudiantes.uv.mx",
        "fechaInscripcion": "2026-08-25T11:00:00-06:00", "inasistencias": 0, "enRiesgo": false },
      { "matricula": "S23017731", "nombreCompleto": "Mariana López Herrera", "correo": "zS23017731@estudiantes.uv.mx",
        "fechaInscripcion": "2026-08-26T08:30:00-06:00", "inasistencias": 2, "enRiesgo": true }
    ]
  }
}
```
Ordenados por nombre. `enRiesgo` = `inasistencias == 2`.

---

### `GET /tutorias/{idTutoria}/sesiones` — `TUTOR` (dueño), `ADMIN`

Lista de sesiones de la pantalla 11: todas las sesiones registradas en BD, más las **virtuales** (`PROGRAMADA`) de los próximos días. Orden descendente por fecha.

**Query params**

| Param | Tipo | Default | Descripción |
|---|---|---|---|
| `dias` | int | `7` | Ventana de sesiones futuras virtuales a incluir, contando desde hoy (0–28). |

**Response 200** → `data`: lista de [`Sesion`](#sesion).

---

### `GET /tutorias/{idTutoria}/historial-asistencia` — `TUTOR` (dueño), `ADMIN`

Matriz de la pantalla 14: cada tutorado contra cada sesión registrada.

**Response 200**
```json
{
  "success": true,
  "message": "Historial de asistencia",
  "data": {
    "tutoria": { "idTutoria": 12, "materia": "Estructuras de Datos", "edificio": 2, "aula": 7,
                 "horarios": [ { "dia": "LUNES", "horaInicio": "10:00", "horaFin": "12:00" }, { "dia": "MIERCOLES", "horaInicio": "10:00", "horaFin": "12:00" } ],
                 "cupo": { "ocupados": 4, "maximo": 7, "disponibles": 3 } },
    "resumen": {
      "sesionesCompletadas": 7,
      "sesionesCanceladas": 1,
      "marcasAsistio": 30,
      "marcasTotales": 35,
      "asistenciaPct": 86,
      "bajasAutomaticas": 1
    },
    "sesiones": [
      { "idSesion": 70, "fecha": "2026-09-07", "dia": "LUNES", "estado": "COMPLETADA" },
      { "idSesion": 74, "fecha": "2026-09-23", "dia": "MIERCOLES", "estado": "CANCELADA" }
    ],
    "tutorados": [
      {
        "matricula": "S23017731",
        "nombreCompleto": "Mariana López Herrera",
        "estadoInscripcion": "BAJA_AUTOMATICA",
        "fechaBaja": "2026-10-05T12:10:00-06:00",
        "marcas": { "70": true, "71": true, "72": false, "74": null, "88": false },
        "idSesionBaja": 88,
        "asistio": 4,
        "falto": 3
      }
    ]
  }
}
```

| Campo | Descripción |
|---|---|
| `sesiones` | Sesiones en BD (`COMPLETADA`, `CANCELADA`, `LISTA_ABIERTA`), en orden ascendente. |
| `tutorados` | Todos los tutorados con inscripción en la tutoría, en cualquier estado. |
| `marcas` | Mapa `idSesion → true` (asistió) · `false` (no asistió) · `null` (sin marca, sesión cancelada o anterior a su inscripción). |
| `idSesionBaja` | Sesión cuya inasistencia causó la baja automática. Se marca distinto en la UI. |
| `asistenciaPct` | `round(marcasAsistio / marcasTotales × 100)` sobre sesiones `COMPLETADA`. `null` si no hay marcas. |

---

## 6. Temas

Los temas pertenecen a un horario y **sí se pueden modificar con inscritos** (pantalla 11, edición en línea).

### `POST /horarios/{idHorario}/temas` — `TUTOR` (dueño)

**Body**
```json
{ "tema": "Grafos dirigidos" }
```

| Regla | Mensaje |
|---|---|
| Tutoría `ACTIVA` y horario `ACTIVO`. | `El horario no está activo.` |
| De 1 a 60 caracteres, después de `trim`. | `El tema no puede exceder 60 caracteres.` |
| Máximo 10 por horario. | `Máximo 10 temas por horario.` |
| No se repite en el horario (sin distinguir mayúsculas). | `El tema ya existe en este horario.` |

**Response 200** → `message: "Tema agregado."`, `data`: `{ "idTema": 130, "tema": "Grafos dirigidos" }`

### `DELETE /temas/{idTema}` — `TUTOR` (dueño)

**Response 200** → `message: "Tema eliminado."`

---

## 7. Inscripciones — `/inscripciones`

Para el cliente, la inscripción es **a la tutoría**: el tutorado queda en todos sus horarios activos. Los endpoints se identifican por `idTutoria`, así que el contrato no cambia aunque en BD haya una fila por horario o una por tutoría (ver [MODELO_DE_DATOS.md](MODELO_DE_DATOS.md#cambios-recomendados)).

### `POST /inscripciones` — `TUTORADO`

Inscribe al tutorado autenticado (pantalla 05).

**Body**
```json
{ "idTutoria": 12 }
```

**Validaciones** (RN-3, RN-4, RN-12)

| Regla | Mensaje |
|---|---|
| Tutoría `ACTIVA` con al menos un horario `ACTIVO`. | `Esta tutoría ya no está disponible.` |
| Sin inscripción `ACTIVA` en esta tutoría. | `Ya estás inscrito en esta tutoría.` |
| Sin `BAJA_AUTOMATICA` previa en esta tutoría. | `No puedes volver a inscribirte: tuviste baja automática en esta tutoría.` |
| Cupo disponible (< 7). | `La tutoría ya no tiene lugares disponibles.` |
| Sin choque con sus inscripciones activas. | `El jueves de 17:00 a 19:00 choca con tu inscripción en Bases de Datos (16:00–18:00).` |

**Efectos:** estado `ACTIVA`, `fecha_inscripcion = ahora`, correo de confirmación.

**Response 200**
```json
{
  "success": true,
  "message": "Inscripción confirmada.",
  "data": { "idTutoria": 12, "estado": "ACTIVA", "fechaInscripcion": "2026-10-05T09:14:00-06:00" }
}
```

---

### `GET /inscripciones` — `TUTORADO`

**Mis tutorías** (pantalla 07).

**Response 200**
```json
{
  "success": true,
  "message": "Mis inscripciones",
  "data": {
    "proximaSesion": {
      "idTutoria": 7,
      "materia": "Bases de Datos",
      "fecha": "2026-10-06",
      "dia": "MARTES",
      "horaInicio": "16:00",
      "horaFin": "18:00",
      "edificio": 1,
      "aula": 9,
      "temas": ["Normalización", "Consultas con JOIN", "Índices"],
      "recordatorioA": "14:00"
    },
    "activas": [
      {
        "tutoria": { "...": "TutoriaResumen" },
        "inscripcion": { "estado": "ACTIVA", "fechaInscripcion": "2026-10-05T09:14:00-06:00", "nueva": true },
        "inasistencias": 0,
        "inasistenciasPermitidas": 2,
        "proximaSesion": { "fecha": "2026-10-07", "dia": "MIERCOLES", "horaInicio": "10:00", "horaFin": "12:00", "primera": true }
      }
    ],
    "anteriores": [
      {
        "tutoria": { "...": "TutoriaResumen" },
        "inscripcion": { "estado": "BAJA_AUTOMATICA", "fechaInscripcion": "2026-08-24T10:00:00-06:00", "fechaBaja": "2026-09-28T14:05:00-06:00" },
        "inasistencias": 3
      }
    ]
  }
}
```

| Campo | Descripción |
|---|---|
| `proximaSesion` (raíz) | La sesión más próxima entre todas las inscripciones activas. `null` si no hay. |
| `activas` | Inscripciones `ACTIVA` en tutorías `ACTIVA`. |
| `inscripcion.nueva` | `true` si todavía no hay ninguna sesión `COMPLETADA` desde que se inscribió (etiqueta "NUEVA"). |
| `proximaSesion.primera` | `true` si es la primera sesión desde la inscripción ("Primera sesión: …"). |
| `anteriores` | Inscripciones `BAJA`/`BAJA_AUTOMATICA`, más las `ACTIVA` de tutorías que pasaron a `INACTIVA` [S3]. Si hubo varias inscripciones a la misma tutoría, solo se muestra la más reciente. |

---

### `GET /inscripciones/tutorias/{idTutoria}` — `TUTORADO`

**Mi asistencia** en una tutoría (pantalla 08). Usa la inscripción más reciente del tutorado en esa tutoría.

**Response 200**
```json
{
  "success": true,
  "message": "Mi inscripción",
  "data": {
    "tutoria": { "...": "detalle con horarios y temas, como GET /tutorias/{id}" },
    "inscripcion": { "estado": "ACTIVA", "fechaInscripcion": "2026-08-26T08:30:00-06:00", "fechaBaja": null },
    "resumen": {
      "inasistencias": 1,
      "inasistenciasPermitidas": 2,
      "sesionesConAsistencia": 9,
      "sesionesCanceladas": 1
    },
    "proximaSesion": { "fecha": "2026-10-06", "dia": "MARTES", "horaInicio": "16:00", "horaFin": "18:00", "edificio": 1, "aula": 9, "recordatorioA": "14:00" },
    "historial": [
      { "idSesion": null, "fecha": "2026-10-06", "dia": "MARTES", "horaInicio": "16:00", "horaFin": "18:00", "estadoSesion": "PROGRAMADA", "miAsistencia": "PENDIENTE" },
      { "idSesion": 61, "fecha": "2026-10-01", "dia": "JUEVES", "horaInicio": "16:00", "horaFin": "18:00", "estadoSesion": "COMPLETADA", "miAsistencia": "ASISTIO" },
      { "idSesion": 60, "fecha": "2026-09-29", "dia": "MARTES", "horaInicio": "16:00", "horaFin": "18:00", "estadoSesion": "COMPLETADA", "miAsistencia": "NO_ASISTIO" },
      { "idSesion": 59, "fecha": "2026-09-24", "dia": "JUEVES", "horaInicio": "16:00", "horaFin": "18:00", "estadoSesion": "CANCELADA", "miAsistencia": "NO_CUENTA" }
    ]
  }
}
```

`historial` incluye las sesiones registradas desde `fechaInscripcion` y la próxima sesión virtual, en orden descendente.

| `miAsistencia` | Cuándo |
|---|---|
| `ASISTIO` / `NO_ASISTIO` | Sesión `COMPLETADA` con marca. |
| `PENDIENTE` | Sesión `PROGRAMADA` o `LISTA_ABIERTA` sin cerrar. |
| `NO_CUENTA` | Sesión `CANCELADA`. |
| `SIN_MARCA` | Sesión `COMPLETADA` sin marca del tutorado; solo pasa si se inscribió con la lista ya abierta. |

---

### `DELETE /inscripciones/tutorias/{idTutoria}` — `TUTORADO`

**Darme de baja** (pantallas 06 y 08). Libera el lugar.

| Regla | Mensaje |
|---|---|
| Tiene inscripción `ACTIVA` en la tutoría. | `No tienes una inscripción activa en esta tutoría.` |

**Efectos:** estado `BAJA`, `fecha_baja = ahora`, correo de confirmación [S5]. Después puede volver a inscribirse (RN-12).

**Response 200** → `message: "Te diste de baja de la tutoría."`

---

### `GET /inscripciones/semana` — `TUTORADO`

Horarios semanales de sus inscripciones activas, para el panel "Tu semana" (pantalla 04). El frontend los combina con los horarios de la tutoría que se está viendo; los choques ya vienen calculados en `panelTutorado.choques`.

**Response 200**
```json
{
  "success": true,
  "message": "Mi semana",
  "data": [
    { "dia": "MARTES", "horaInicio": "16:00", "horaFin": "18:00", "idTutoria": 7, "materia": "Bases de Datos" },
    { "dia": "JUEVES", "horaInicio": "16:00", "horaFin": "18:00", "idTutoria": 7, "materia": "Bases de Datos" },
    { "dia": "VIERNES", "horaInicio": "08:00", "horaFin": "10:00", "idTutoria": 3, "materia": "Álgebra Lineal" }
  ]
}
```

---

## 8. Sesiones y pase de lista — `/sesiones`

Una sesión **solo se guarda en BD cuando se abre el pase de lista o cuando se cancela** (RN-6). Antes de eso es virtual y se identifica por `idHorario` + `fecha`.

### `GET /sesiones/hoy` — `TUTOR`

Pantalla 09, bloque "Sesiones de hoy".

**Response 200**
```json
{
  "success": true,
  "message": "Sesiones de hoy",
  "data": {
    "fecha": "2026-10-05",
    "sesiones": [ { "...": "Sesion (LISTA_ABIERTA, Estructuras de Datos 10:00–12:00)" }, { "...": "Sesion (PROGRAMADA, Programación Web 16:00–18:00)" } ],
    "listasSinCerrar": [ ]
  }
}
```

| Campo | Descripción |
|---|---|
| `sesiones` | Sesiones del día de hoy (registradas y virtuales) de sus tutorías `ACTIVA`, ordenadas por hora. |
| `listasSinCerrar` | Sesiones de **días anteriores** con estado `LISTA_ABIERTA`. No se cierran solas (RN-9). |

---

### `POST /sesiones/abrir-lista` — `TUTOR` (dueño)

Abre el pase de lista. **Crea la sesión** con `hora_inicio` y `hora_fin` copiadas del horario y `fecha_lista_abierta = ahora` (RN-7).

**Body**
```json
{ "idHorario": 31, "fecha": "2026-10-05" }
```

| Regla | Mensaje |
|---|---|
| Tutoría `ACTIVA`, horario `ACTIVO`. | `El horario no está activo.` |
| `fecha` corresponde al `dia` del horario. | `La fecha no corresponde al día del horario.` |
| `fecha` es hoy [S1]. | `Solo puedes pasar lista el día de la sesión.` |
| La sesión ya inició (`ahora ≥ fecha + horaInicio`). | `La sesión aún no inicia.` |
| No existe sesión cancelada o completada para ese horario y fecha. | `La sesión fue cancelada.` / `El pase de lista ya se cerró.` |

Si la sesión ya existe con `LISTA_ABIERTA`, responde `200` con la misma sesión (idempotente).

**Response 200** → `message: "Pase de lista abierto."`, `data`: la respuesta de `GET /sesiones/{idSesion}/lista`.

---

### `POST /sesiones/cancelar` — `TUTOR` (dueño)

Cancela una sesión futura. **Crea la sesión** con `fecha_cancelada = ahora` (RN-8).

**Body**
```json
{ "idHorario": 32, "fecha": "2026-10-07" }
```

| Regla | Mensaje |
|---|---|
| Tutoría `ACTIVA`, horario `ACTIVO`. | `El horario no está activo.` |
| `fecha` corresponde al `dia` del horario. | `La fecha no corresponde al día del horario.` |
| Faltan **al menos 15 minutos** para el inicio. | `Ya no puedes cancelar: faltan menos de 15 minutos para la sesión.` |
| No hay pase de lista abierto ni cerrado para esa fecha. | `No se puede cancelar una sesión con pase de lista.` |
| No está cancelada ya. | `La sesión ya está cancelada.` |

**Efectos:** correo de cancelación a los inscritos activos; no se envía el recordatorio; la sesión no cuenta para inasistencias.

**Response 200** → `message: "Sesión cancelada."`, `data`: `Sesion` con estado `CANCELADA`.

---

### `GET /sesiones/{idSesion}/lista` — `TUTOR` (dueño), `ADMIN`

Pase de lista (pantallas 12 y 13). Sirve también para ver la lista de una sesión completada ("Ver lista").

**Response 200**
```json
{
  "success": true,
  "message": "Pase de lista",
  "data": {
    "sesion": { "...": "Sesion" },
    "tutorados": [
      { "matricula": "S23016620", "nombreCompleto": "Andrea Morales Pineda", "inasistenciasPrevias": 0, "asistio": true,  "causaraBaja": false },
      { "matricula": "S23017731", "nombreCompleto": "Mariana López Herrera", "inasistenciasPrevias": 2, "asistio": false, "causaraBaja": true }
    ],
    "resumen": { "inscritos": 5, "marcados": 5, "asistieron": 4, "noAsistieron": 1, "sinMarca": 0 },
    "cierre": {
      "todosMarcados": true,
      "sesionIniciada": true,
      "noCancelada": true,
      "puedeCerrar": true,
      "bajasPrevistas": [ { "matricula": "S23017731", "nombreCompleto": "Mariana López Herrera", "inasistencias": 3 } ]
    }
  }
}
```

| Campo | Descripción |
|---|---|
| `tutorados` | Inscritos activos, más quienes ya tengan marca en esta sesión. Orden por nombre. |
| `asistio` | `true` / `false` / `null` (sin marca). |
| `inasistenciasPrevias` | Inasistencias en sesiones `COMPLETADA` anteriores (RN-10). |
| `causaraBaja` | `asistio == false` y `inasistenciasPrevias + 1 > 2`. |
| `cierre` | Las tres condiciones que muestra la pantalla 12 y las bajas que se aplicarán (modal de la pantalla 13). |

---

### `PUT /sesiones/{idSesion}/lista` — `TUTOR` (dueño)

Guarda marcas. Es una actualización parcial: solo cambian las matrículas enviadas. "Marcar a todos: asistió" envía a todos con `true`.

**Body**
```json
{
  "marcas": [
    { "matricula": "S23016620", "asistio": true },
    { "matricula": "S23017731", "asistio": false }
  ]
}
```

| Regla | Mensaje |
|---|---|
| Sesión con estado `LISTA_ABIERTA`. | `El pase de lista no está abierto.` |
| Cada matrícula tiene inscripción activa en la tutoría o ya tenía marca en esta sesión. | `S23099999 no está inscrito en esta tutoría.` |

**Response 200** → `message: "Asistencia guardada."`, `data`: igual que `GET /sesiones/{idSesion}/lista`.

---

### `POST /sesiones/{idSesion}/cerrar-lista` — `TUTOR` (dueño)

Cierra el pase de lista (pantalla 13). Después de cerrarla ya no se puede modificar (RN-9, RN-10).

| Regla | Mensaje |
|---|---|
| Sesión con estado `LISTA_ABIERTA`. | `El pase de lista no está abierto.` |
| La sesión ya inició. | `La sesión aún no inicia.` |
| Todos los inscritos activos tienen marca. | `Faltan 2 tutorados por marcar.` |

**Efectos:** `fecha_lista_cerrada = ahora` (la sesión pasa a `COMPLETADA`); se calculan las bajas automáticas y se envían sus correos (RN-11).

**Response 200**
```json
{
  "success": true,
  "message": "Pase de lista cerrado.",
  "data": {
    "idSesion": 88,
    "estado": "COMPLETADA",
    "asistieron": 4,
    "noAsistieron": 1,
    "bajasAplicadas": [ { "matricula": "S23017731", "nombreCompleto": "Mariana López Herrera", "inasistencias": 3 } ]
  }
}
```

---

## 9. Comentarios

Comentarios y sugerencias de los tutorados para el tutor, por tutoría.

### `GET /tutorias/{idTutoria}/comentarios` — todos

Cualquier usuario autenticado puede verlos; la pantalla 04 los muestra a tutorados que no están inscritos. Orden: más recientes primero.

**Response 200**
```json
{
  "success": true,
  "message": "Comentarios",
  "data": [
    {
      "idComentario": 15,
      "matricula": "S23014587",
      "nombreCompleto": "Valeria Mendoza Cruz",
      "comentario": "¿Podemos ver un ejemplo de transacciones con ROLLBACK?",
      "fechaComentario": "2026-10-04T19:22:00-06:00",
      "esMio": true
    }
  ]
}
```

### `POST /tutorias/{idTutoria}/comentarios` — `TUTORADO`

**Body**
```json
{ "comentario": "Sugiero repasar índices compuestos antes del examen." }
```

| Regla | Mensaje |
|---|---|
| Tutoría `ACTIVA`. | `La tutoría no está activa.` |
| Tiene inscripción `ACTIVA` en la tutoría. | `Inscríbete para dejar comentarios.` |
| De 1 a 255 caracteres, después de `trim`. | `El comentario no puede exceder 255 caracteres.` |

**Response 200** → `message: "Comentario publicado."`, `data`: el comentario creado.

### `DELETE /comentarios/{idComentario}` — `TUTORADO` (autor), `ADMIN`

[S6]. **Response 200** → `message: "Comentario eliminado."`

---

## 10. Administración — `/admin`

### `GET /admin/resumen` — `ADMIN`

Tarjetas de la pantalla 15.

**Query params**

| Param | Tipo | Descripción |
|---|---|---|
| `desde` | fecha | Opcional. Inicio del conteo de bajas automáticas ("desde agosto"). Sin este parámetro se cuentan todas. |

**Response 200**
```json
{
  "success": true,
  "message": "Resumen",
  "data": {
    "tutoriasActivas": 11,
    "inscripcionesActivas": 46,
    "lugaresDisponibles": 31,
    "lugaresTotales": 77,
    "bajasAutomaticas": 2
  }
}
```

| Campo | Cálculo |
|---|---|
| `lugaresTotales` | `tutoriasActivas × 7`. |
| `lugaresDisponibles` | `lugaresTotales − inscripcionesActivas`. |

### `GET /admin/tutorias` — `ADMIN`

Tabla de la pantalla 15.

**Query params**

| Param | Valores | Default |
|---|---|---|
| `q` | Texto (nombre de EE, NRC o nombre del tutor) | — |
| `matriculaTutor` | Matrícula | — |
| `estado` | `ACTIVA` · `INACTIVA` · `TODAS` | `ACTIVA` |

**Response 200** → lista de `TutoriaResumen` más `asistenciaPct` (RN-14, `null` si no hay marcas). Orden por nombre de la EE.

### `GET /admin/tutores` — `ADMIN`

Usuarios con rol `TUTOR`, para el filtro "Todos los tutores".

**Response 200** → `data`: lista de `UsuarioResumen`, ordenada por nombre.

### `POST /admin/tutorias/cierre-periodo` — `ADMIN`

Desactiva **todas** las tutorías `ACTIVA` al terminar el periodo. Aplica los efectos de `PATCH /tutorias/{id}/estado` a cada una. Si alguna tiene un pase de lista abierto, la operación completa se rechaza y el mensaje lista esas tutorías.

**Body**
```json
{ "confirmar": true }
```

**Response 200** → `message: "Periodo cerrado."`, `data`: `{ "tutoriasDesactivadas": 11 }`

> Para desactivar una sola tutoría se usa `PATCH /tutorias/{id}/estado`. Las EE se administran en [§3](#3-experiencias-educativas--materias).

---

## 11. Matriz de permisos

| Ruta | TUTORADO | TUTOR | ADMIN |
|---|:-:|:-:|:-:|
| `POST /auth/microsoft`, `/auth/google`, `/auth/registro` | público | público | público |
| `GET /usuarios/me` | ✅ | ✅ | ✅ |
| `GET /materias`, `GET /materias/{id}` | — | ✅ | ✅ |
| `POST/PUT/DELETE /materias…` | — | — | ✅ |
| `GET /catalogos/edificios` | — | ✅ | ✅ |
| `GET /tutorias` (explorar) | ✅ | — | — |
| `GET /tutorias/{id}`, `GET /tutorias/{id}/comentarios` | ✅ | ✅ | ✅ |
| `GET /tutorias/mis-tutorias`, `POST /tutorias` | — | ✅ | — |
| `PUT /tutorias/{id}`, `POST /horarios/{id}/temas`, `DELETE /temas/{id}` | — | dueño | — |
| `PATCH /tutorias/{id}/estado` | — | dueño | ✅ |
| `GET /tutorias/{id}/inscritos`, `/sesiones`, `/historial-asistencia` | — | dueño | ✅ |
| `/inscripciones/**` | ✅ | — | — |
| `POST /tutorias/{id}/comentarios` | ✅ | — | — |
| `DELETE /comentarios/{id}` | autor | — | ✅ |
| `GET /sesiones/hoy`, `POST /sesiones/abrir-lista`, `POST /sesiones/cancelar` | — | dueño | — |
| `GET /sesiones/{id}/lista` | — | dueño | ✅ |
| `PUT /sesiones/{id}/lista`, `POST /sesiones/{id}/cerrar-lista` | — | dueño | — |
| `/admin/**` | — | — | ✅ |
