-- =====================================================================
-- Datos de prueba — reproducen los mockups de escritorio (5 oct 2026)
--
-- Requiere: 001_esquema.sql recién ejecutado (tablas vacías).
-- Uso: SQL Editor de Supabase o psql "<connection string>" -f este archivo.
--
-- Para entrar sin Microsoft/Google en pruebas: backend con
-- AUTH_DEV_HABILITADO=true y POST /auth/dev {"correo": "..."}:
--   Tutorada  Valeria Mendoza Cruz  zs23014587@estudiantes.uv.mx
--   Tutora    Rocío Hernández Aguilar zs21009342@estudiantes.uv.mx
--   Admin     Adriana Salas Ortega  asalas@uv.mx
--
-- Contenido:
--   * Catálogo real de Experiencias Educativas de la Licenciatura en
--     Ingeniería de Software, plan 2023 (54 EE), con NRC aleatorios.
--   * 11 tutorías activas con los cupos de la pantalla 15 (46 inscripciones
--     activas). Las EE de los mockups que no existen en el plan se
--     reasignaron a la más cercana:
--       Cálculo Integral            -> Lógica
--       Contabilidad Financiera     -> Desarrollo de empresas tecnológicas
--       Fundamentos de Programación -> Metodología de la programación
--       Ingeniería de Requisitos    -> Requisitos de software
--       Probabilidad y Estadística  -> Estadística para la calidad del software
--       Programación Orientada a Objetos -> Diseño y programación orientada a objetos
--       Programación Web            -> Desarrollo de sistemas web
--   * Estructuras de datos (Rocío, lun y mié 10–12): 5 inscritos, historial
--     de la pantalla 14 sin la sesión del 5 oct; Mariana López lleva 2
--     inasistencias (en riesgo: la próxima falta la da de baja).
--   * Valeria: inscrita en Bases de datos y Álgebra lineal, baja voluntaria
--     en Metodología de la programación y baja automática en Matemáticas
--     discretas (pantalla 07); historial de Bases de datos (pantalla 08).
--   * Comentarios de las pantallas 04, 08 y 11.
-- =====================================================================

BEGIN;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM tutoria) OR EXISTS (SELECT 1 FROM usuario) THEN
        RAISE EXCEPTION 'La BD ya tiene datos: ejecuta primero 001_esquema.sql.';
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- Funciones auxiliares (solo viven en esta sesión)
-- ---------------------------------------------------------------------
CREATE FUNCTION pg_temp.usuario(p_matricula TEXT, p_nombre TEXT, p_apellidos TEXT, p_correo TEXT, p_rol TEXT)
RETURNS VOID LANGUAGE sql AS $f$
    INSERT INTO usuario (matricula, nombre, apellidos, correo, id_rol)
    VALUES (p_matricula, p_nombre, p_apellidos, lower(p_correo), (SELECT id_rol FROM rol WHERE rol = p_rol));
$f$;

-- Busca la EE por nombre (los NRC son aleatorios).
CREATE FUNCTION pg_temp.tutoria(p_materia TEXT, p_tutor TEXT, p_edificio INT, p_aula INT)
RETURNS INT LANGUAGE plpgsql AS $f$
DECLARE
    v_materia INT;
    v_id INT;
BEGIN
    SELECT id_materia INTO v_materia FROM materia WHERE materia = p_materia;
    IF v_materia IS NULL THEN
        RAISE EXCEPTION 'No existe la experiencia educativa "%".', p_materia;
    END IF;
    INSERT INTO tutoria (id_materia, matricula_tutor, edificio, aula, estado)
    VALUES (v_materia, p_tutor, p_edificio, p_aula, 'ACTIVA')
    RETURNING id_tutoria INTO v_id;
    RETURN v_id;
END $f$;

CREATE FUNCTION pg_temp.horario(p_tutoria INT, p_dia TEXT, p_inicio TIME, p_fin TIME, p_temas TEXT[] DEFAULT '{}')
RETURNS INT LANGUAGE plpgsql AS $f$
DECLARE
    v_id INT;
    v_tema TEXT;
BEGIN
    INSERT INTO horario (id_tutoria, dia, hora_inicio, hora_fin, estado)
    VALUES (p_tutoria, p_dia, p_inicio, p_fin, 'ACTIVO') RETURNING id_horario INTO v_id;
    FOREACH v_tema IN ARRAY p_temas LOOP
        INSERT INTO temas (tema, id_horario) VALUES (v_tema, v_id);
    END LOOP;
    RETURN v_id;
END $f$;

CREATE FUNCTION pg_temp.inscribir(p_tutoria INT, p_matricula TEXT, p_fecha TIMESTAMPTZ,
                                  p_estado TEXT DEFAULT 'ACTIVA', p_baja TIMESTAMPTZ DEFAULT NULL)
RETURNS VOID LANGUAGE sql AS $f$
    INSERT INTO inscripcion (id_tutoria, matricula_tutorado, fecha_inscripcion, estado, fecha_baja)
    VALUES (p_tutoria, p_matricula, p_fecha, p_estado, p_baja);
$f$;

-- Sesión completada: marca a todos los inscritos de esa fecha como asistió,
-- salvo las matrículas en p_faltan.
CREATE FUNCTION pg_temp.sesion_completada(p_horario INT, p_fecha DATE, p_faltan TEXT[] DEFAULT '{}')
RETURNS VOID LANGUAGE plpgsql AS $f$
DECLARE
    v_sesion INT;
    v_cierre TIMESTAMPTZ := (p_fecha + (SELECT hora_fin FROM horario WHERE id_horario = p_horario))
                            AT TIME ZONE 'America/Mexico_City';
BEGIN
    INSERT INTO sesion (id_horario, fecha, hora_inicio, hora_fin, fecha_lista_abierta, fecha_lista_cerrada)
    SELECT id_horario, p_fecha, hora_inicio, hora_fin, v_cierre - interval '2 hours', v_cierre
    FROM horario WHERE id_horario = p_horario
    RETURNING id_sesion INTO v_sesion;

    INSERT INTO asistencia (id_sesion, id_inscripcion, asistio)
    SELECT v_sesion, i.id_inscripcion, NOT (i.matricula_tutorado = ANY (p_faltan))
    FROM inscripcion i JOIN horario h ON h.id_tutoria = i.id_tutoria
    WHERE h.id_horario = p_horario
      AND i.fecha_inscripcion::date <= p_fecha
      AND (i.fecha_baja IS NULL OR i.fecha_baja::date >= p_fecha);
END $f$;

CREATE FUNCTION pg_temp.sesion_cancelada(p_horario INT, p_fecha DATE)
RETURNS VOID LANGUAGE sql AS $f$
    INSERT INTO sesion (id_horario, fecha, hora_inicio, hora_fin, fecha_cancelada)
    SELECT id_horario, p_fecha, hora_inicio, hora_fin,
           (p_fecha - 1 + time '18:00') AT TIME ZONE 'America/Mexico_City'
    FROM horario WHERE id_horario = p_horario;
$f$;

-- ---------------------------------------------------------------------
-- Experiencias Educativas: Licenciatura en Ingeniería de Software,
-- plan 2023 (UV, catálogo de experiencias educativas, 54 EE).
-- Nombres tal como aparecen en el plan; NRC (identificador) aleatorios.
-- ---------------------------------------------------------------------
INSERT INTO materia (materia, identificador) VALUES
    -- Área de Formación Básica General (5)
    ('Literacidad digital', 21988),
    ('Pensamiento crítico para la solución de problemas', 74302),
    ('Lengua I', 55585),
    ('Lengua II', 58317),
    ('Lectura y escritura de textos académicos', 41912),
    -- Área de Formación Básica de Iniciación a la Disciplina (8)
    ('Metodología de la programación', 39337),
    ('Fundamentos de ingeniería de software', 23164),
    ('Lógica', 93849),
    ('Estructuras de datos', 29454),
    ('Álgebra lineal', 32179),
    ('Matemáticas discretas', 76196),
    ('Habilidades de comunicación', 20134),
    ('Arquitectura de computadoras', 74272),
    -- Área de Formación Disciplinar (25)
    ('Requisitos de software', 52027),
    ('Diseño y programación orientada a objetos', 40971),
    ('Bases de datos', 84926),
    ('Procesos para ingeniería de software', 82751),
    ('Principios de diseño de software', 47974),
    ('Principios de construcción de software', 64334),
    ('Bases de datos para el desarrollo de software', 87790),
    ('Redes de computadoras para el desarrollo de software', 82695),
    ('Programación para aplicaciones de escritorio', 29161),
    ('Diseño de software', 62195),
    ('Tecnologías para la construcción de software', 67792),
    ('Sistemas operativos', 98127),
    ('Fundamentos de prueba de software', 40206),
    ('Taller de prueba de software', 71356),
    ('Desarrollo de sistemas en red', 21944),
    ('Diseño de interfaces de usuario', 27231),
    ('Desarrollo de sistemas web', 88622),
    ('Despliegue de software', 52503),
    ('Desarrollo de empresas tecnológicas', 46014),
    ('Administración de proyectos de software', 94560),
    ('Inteligencia artificial', 64626),
    ('Medición de software', 68429),
    ('Proyecto guiado', 68422),
    ('Aspectos de seguridad para el desarrollo de software', 31846),
    ('Estadística para la calidad del software', 34317),
    -- Área de Formación Terminal (4)
    ('Servicio Social', 56458),
    ('Experiencia Recepcional', 30848),
    ('Prácticas profesionales de ingeniería de software', 92777),
    ('Acreditación del idioma inglés', 51634),
    -- Optativas (12)
    ('Verificación y validación de software', 97572),
    ('Base de datos no convencionales', 95079),
    ('Administración de bases de datos', 94815),
    ('Sistemas operativos móviles', 51797),
    ('Ambientes para la ejecución de servicios', 76783),
    ('Programación segura', 42043),
    ('Pentesting', 89107),
    ('Análisis de algoritmos', 41980),
    ('Programación funcional', 86546),
    ('Desarrollo ágil de software', 28183),
    ('Reingeniería de software', 93933),
    ('Inteligencia artificial aplicada a la ingeniería de software', 99821);

-- ---------------------------------------------------------------------
-- Usuarios
-- ---------------------------------------------------------------------
DO $$
BEGIN
    PERFORM pg_temp.usuario('25480', 'Adriana', 'Salas Ortega', 'asalas@uv.mx', 'ADMIN');

    PERFORM pg_temp.usuario('S21009342', 'Rocío', 'Hernández Aguilar', 'zS21009342@estudiantes.uv.mx', 'TUTOR');
    PERFORM pg_temp.usuario('S21004411', 'Jorge', 'Castillo Rivera', 'zS21004411@estudiantes.uv.mx', 'TUTOR');
    PERFORM pg_temp.usuario('S21002250', 'Ernesto', 'Vázquez Lara', 'zS21002250@estudiantes.uv.mx', 'TUTOR');
    PERFORM pg_temp.usuario('S21007711', 'Daniela', 'Ortiz Méndez', 'zS21007711@estudiantes.uv.mx', 'TUTOR');
    PERFORM pg_temp.usuario('S21003390', 'Alejandra', 'Ruiz Sosa', 'zS21003390@estudiantes.uv.mx', 'TUTOR');

    PERFORM pg_temp.usuario('S23014587', 'Valeria', 'Mendoza Cruz', 'zS23014587@estudiantes.uv.mx', 'TUTORADO');
    PERFORM pg_temp.usuario('S23016620', 'Andrea', 'Morales Pineda', 'zS23016620@estudiantes.uv.mx', 'TUTORADO');
    PERFORM pg_temp.usuario('S23011873', 'Diego', 'Ramírez Soto', 'zS23011873@estudiantes.uv.mx', 'TUTORADO');
    PERFORM pg_temp.usuario('S22019045', 'Fernando', 'Aguilar Ruiz', 'zS22019045@estudiantes.uv.mx', 'TUTORADO');
    PERFORM pg_temp.usuario('S23012298', 'Kevin', 'Domínguez Luna', 'zS23012298@estudiantes.uv.mx', 'TUTORADO');
    PERFORM pg_temp.usuario('S23017731', 'Mariana', 'López Herrera', 'zS23017731@estudiantes.uv.mx', 'TUTORADO');
    PERFORM pg_temp.usuario('S24011236', 'Sofía', 'Ramos Gutiérrez', 'zS24011236@estudiantes.uv.mx', 'TUTORADO');

    -- Tutorados de relleno para los cupos de la pantalla 15 (S24900001…S24900038).
    PERFORM pg_temp.usuario('S249' || lpad(n::text, 5, '0'), 'Tutorado', 'Prueba ' || lpad(n::text, 2, '0'),
                            'zS249' || lpad(n::text, 5, '0') || '@estudiantes.uv.mx', 'TUTORADO')
    FROM generate_series(1, 38) AS n;
END $$;

-- ---------------------------------------------------------------------
-- Tutorías, horarios, inscripciones e historial
-- ---------------------------------------------------------------------
DO $$
DECLARE
    t_algebra INT; t_bd INT; t_logica INT; t_empresas INT; t_ed INT; t_metodologia INT;
    t_req INT; t_discretas INT; t_estadistica INT; t_dpoo INT; t_web INT;
    h_ed_lun INT; h_ed_mie INT; h_bd_mar INT; h_bd_jue INT; h_md_lun INT; h_md_mie INT;
    relleno INT := 0;
    inicio_periodo TIMESTAMPTZ := '2026-08-24 09:00-06';

BEGIN
    t_algebra   := pg_temp.tutoria('Álgebra lineal', 'S21002250', 2, 4);
    t_bd        := pg_temp.tutoria('Bases de datos', 'S21004411', 1, 9);
    t_logica   := pg_temp.tutoria('Lógica', 'S21002250', 1, 3);
    t_empresas     := pg_temp.tutoria('Desarrollo de empresas tecnológicas', 'S21003390', 1, 12);
    t_ed        := pg_temp.tutoria('Estructuras de datos', 'S21009342', 2, 7);
    t_metodologia      := pg_temp.tutoria('Metodología de la programación', 'S21009342', 1, 5);
    t_req       := pg_temp.tutoria('Requisitos de software', 'S21004411', 2, 9);
    t_discretas := pg_temp.tutoria('Matemáticas discretas', 'S21007711', 1, 8);
    t_estadistica      := pg_temp.tutoria('Estadística para la calidad del software', 'S21007711', 1, 6);
    t_dpoo       := pg_temp.tutoria('Diseño y programación orientada a objetos', 'S21007711', 2, 2);
    t_web       := pg_temp.tutoria('Desarrollo de sistemas web', 'S21009342', 2, 11);

    PERFORM pg_temp.horario(t_algebra, 'VIERNES', '08:00', '10:00');
    h_bd_mar := pg_temp.horario(t_bd, 'MARTES', '16:00', '18:00', ARRAY['Normalización', 'Consultas con JOIN', 'Índices']);
    h_bd_jue := pg_temp.horario(t_bd, 'JUEVES', '16:00', '18:00', ARRAY['Subconsultas', 'Procedimientos almacenados', 'Transacciones']);
    PERFORM pg_temp.horario(t_logica, 'MARTES', '12:00', '14:00');
    PERFORM pg_temp.horario(t_logica, 'JUEVES', '12:00', '14:00');
    PERFORM pg_temp.horario(t_empresas, 'LUNES', '16:00', '18:00');
    h_ed_lun := pg_temp.horario(t_ed, 'LUNES', '10:00', '12:00', ARRAY['Listas enlazadas', 'Pilas y colas', 'Recursividad']);
    h_ed_mie := pg_temp.horario(t_ed, 'MIERCOLES', '10:00', '12:00', ARRAY['Árboles binarios de búsqueda', 'Recorridos de árboles', 'Montículos']);
    PERFORM pg_temp.horario(t_metodologia, 'MARTES', '12:00', '14:00');
    PERFORM pg_temp.horario(t_metodologia, 'JUEVES', '12:00', '14:00');
    PERFORM pg_temp.horario(t_req, 'MIERCOLES', '08:00', '10:00');
    PERFORM pg_temp.horario(t_req, 'VIERNES', '10:00', '12:00');
    h_md_lun := pg_temp.horario(t_discretas, 'LUNES', '12:00', '14:00');
    h_md_mie := pg_temp.horario(t_discretas, 'MIERCOLES', '12:00', '14:00');
    PERFORM pg_temp.horario(t_estadistica, 'JUEVES', '17:00', '19:00');
    PERFORM pg_temp.horario(t_dpoo, 'MARTES', '10:00', '12:00');
    PERFORM pg_temp.horario(t_web, 'LUNES', '16:00', '18:00');
    PERFORM pg_temp.horario(t_web, 'MIERCOLES', '16:00', '18:00');

    -- Valeria (pantalla 07)
    PERFORM pg_temp.inscribir(t_bd, 'S23014587', '2026-08-26 10:00-06');
    PERFORM pg_temp.inscribir(t_algebra, 'S23014587', '2026-08-25 10:00-06');
    PERFORM pg_temp.inscribir(t_metodologia, 'S23014587', '2026-08-25 11:00-06', 'BAJA', '2026-09-02 09:00-06');
    PERFORM pg_temp.inscribir(t_discretas, 'S23014587', '2026-08-25 12:00-06', 'BAJA_AUTOMATICA', '2026-09-28 14:05-06');

    -- Estructuras de datos (pantallas 11 a 14)
    PERFORM pg_temp.inscribir(t_ed, 'S23016620', inicio_periodo);
    PERFORM pg_temp.inscribir(t_ed, 'S23011873', inicio_periodo);
    PERFORM pg_temp.inscribir(t_ed, 'S22019045', inicio_periodo);
    PERFORM pg_temp.inscribir(t_ed, 'S23012298', inicio_periodo);
    PERFORM pg_temp.inscribir(t_ed, 'S23017731', inicio_periodo);

    -- Kevin también en Bases de datos (comentario de la pantalla 08)
    PERFORM pg_temp.inscribir(t_bd, 'S23012298', inicio_periodo);

    -- Relleno: cupos de la pantalla 15
    FOR i IN 1..3 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_algebra,   'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..4 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_bd,        'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..7 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_logica,   'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..3 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_empresas,     'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..4 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_metodologia,      'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..6 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_req,       'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..5 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_discretas, 'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..2 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_estadistica,      'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..1 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_dpoo,       'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;
    FOR i IN 1..3 LOOP relleno := relleno + 1; PERFORM pg_temp.inscribir(t_web,       'S249' || lpad(relleno::text, 5, '0'), inicio_periodo); END LOOP;

    -- Historial de Estructuras de datos (pantalla 14, hasta el 30 sep)
    PERFORM pg_temp.sesion_completada(h_ed_lun, '2026-09-07');
    PERFORM pg_temp.sesion_completada(h_ed_mie, '2026-09-09');
    PERFORM pg_temp.sesion_completada(h_ed_lun, '2026-09-14', ARRAY['S23017731']);
    PERFORM pg_temp.sesion_completada(h_ed_lun, '2026-09-21', ARRAY['S23011873']);
    PERFORM pg_temp.sesion_cancelada(h_ed_mie, '2026-09-23');
    PERFORM pg_temp.sesion_completada(h_ed_lun, '2026-09-28', ARRAY['S23017731']);
    PERFORM pg_temp.sesion_completada(h_ed_mie, '2026-09-30', ARRAY['S23012298']);

    -- Historial de Bases de datos (pantalla 08)
    PERFORM pg_temp.sesion_completada(h_bd_jue, '2026-09-17');
    PERFORM pg_temp.sesion_completada(h_bd_mar, '2026-09-22');
    PERFORM pg_temp.sesion_cancelada(h_bd_jue, '2026-09-24');
    PERFORM pg_temp.sesion_completada(h_bd_mar, '2026-09-29', ARRAY['S23014587']);
    PERFORM pg_temp.sesion_completada(h_bd_jue, '2026-10-01');

    -- Matemáticas discretas: las 3 faltas que dieron de baja a Valeria
    PERFORM pg_temp.sesion_completada(h_md_lun, '2026-09-14', ARRAY['S23014587']);
    PERFORM pg_temp.sesion_completada(h_md_lun, '2026-09-21', ARRAY['S23014587']);
    PERFORM pg_temp.sesion_completada(h_md_lun, '2026-09-28', ARRAY['S23014587']);

    -- Comentarios
    INSERT INTO comentarios (matricula_tutorado, comentario, id_tutoria, fecha_comentario) VALUES
        ('S23016620', 'Me ayudaría ver un ejemplo de árbol AVL implementado en Java.', t_ed, '2026-10-02 18:10-06'),
        ('S23011873', '¿Podríamos repasar la notación Big-O antes del primer parcial?', t_ed, '2026-10-03 09:30-06'),
        ('S23012298', 'Sugiero repasar índices compuestos antes del examen.', t_bd, '2026-10-01 20:00-06'),
        ('S23014587', '¿Podemos ver un ejemplo de transacciones con ROLLBACK?', t_bd, '2026-10-04 19:22-06');
END $$;

COMMIT;

-- Verificación rápida (pantalla 15: 11 tutorías, 46 inscripciones activas)
SELECT
    (SELECT COUNT(*) FROM tutoria WHERE estado = 'ACTIVA')     AS tutorias_activas,
    (SELECT COUNT(*) FROM inscripcion WHERE estado = 'ACTIVA') AS inscripciones_activas,
    (SELECT COUNT(*) FROM usuario)                              AS usuarios;
