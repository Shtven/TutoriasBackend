-- =====================================================================
-- Seed S001 — Materias del Plan 2019 (Administración FNT)
-- =====================================================================
-- Carga las materias de las áreas BID (Iniciación a la Disciplina) y
-- D (Disciplinar) del mapa curricular Plan 2019.
--
-- Idempotente: usa WHERE NOT EXISTS para que sea seguro re-ejecutar.
-- La condicion de unicidad es el NRC.
--
-- NRCs: actualmente sinteticos (10001-10044) por carecer de los reales.
-- Si se cuentan con los NRCs oficiales, sustituirlos en la columna nrc
-- de cada fila de la clausula VALUES antes de aplicar el seed.
--
-- Como aplicar:
--   psql -h <host> -U <user> -d <db> -f db/seeds/S001__materias_admin_plan_2019.sql
-- =====================================================================

BEGIN;

INSERT INTO materia (materia, nrc)
SELECT v.materia, v.nrc
FROM (VALUES
    -- ----- BID (Iniciación a la Disciplina, 77 créditos) -----
    ('Fundamentos de Derecho',                                   10001),
    ('Fundamentos de Administración',                            10002),
    ('Matemáticas Básicas',                                      10003),
    ('Contabilidad Básica',                                      10004),
    ('Microeconomía',                                            10005),
    ('Ética y Responsabilidad Social',                           10006),
    ('Organizaciones Sustentables',                              10007),
    ('Derecho Laboral',                                          10008),
    ('Contabilidad Financiera',                                  10009),
    ('Metodología de la Investigación',                          10010),
    ('Mercadotecnia',                                            10011),
    ('Estadística',                                              10012),
    ('Soluciones Tecnológicas Aplicables a las Organizaciones',  10013),
    ('Gestión del Capital Humano',                               10014),
    ('Sistemas de Producción',                                   10015),

    -- ----- D (Disciplinar, 174 créditos) -----
    ('Derecho Corporativo',                                      10016),
    ('Macroeconomía',                                            10017),
    ('Organización y Procedimientos',                            10018),
    ('Contabilidad Gerencial',                                   10019),
    ('Investigación de Mercados',                                10020),
    ('Habilidades Directivas',                                   10021),
    ('Gestión de la Inclusión',                                  10022),
    ('Matemáticas Financieras',                                  10023),
    ('Planeación Financiera y Presupuestos',                     10024),
    ('Creatividad e Innovación Empresarial',                     10025),
    ('Administración de Inversiones',                            10026),
    ('Administración Estratégica',                               10027),
    ('Administración de la Mipyme y Empresas Familiares',        10028),
    ('Administración de las Compensaciones',                     10029),
    ('Investigación de Operaciones',                             10030),
    ('Práctica en Sector Privado',                               10031),
    ('Emprendimiento Social y Económico',                        10032),
    ('Estrategias de Financiamiento',                            10033),
    ('Mercadotecnia Digital',                                    10034),
    ('Formación y Desarrollo de Capital Humano',                 10035),
    ('Métodos Cuantitativos',                                    10036),
    ('Práctica en Sector Público',                               10037),
    ('Formulación y Evaluación de Proyectos',                    10038),
    ('Sistemas de Gestión de Calidad',                           10039),
    ('Seminario de Investigación',                               10040),
    ('Administración Pública',                                   10041),
    ('Comportamiento Organizacional',                            10042),
    ('Marco Tributario',                                         10043),
    ('Transformación Digital en las Organizaciones',             10044)
) AS v(materia, nrc)
WHERE NOT EXISTS (
    SELECT 1 FROM materia m WHERE m.nrc = v.nrc
);

COMMIT;

-- Verificacion
SELECT COUNT(*) AS materias_admin_2019
FROM materia
WHERE nrc BETWEEN 10001 AND 10044;
