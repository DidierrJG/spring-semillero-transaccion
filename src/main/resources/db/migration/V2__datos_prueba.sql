-- ==============================================================================
-- Migración V2: Inserción de datos de prueba para semilleros de investigación
-- ==============================================================================

-- 1. Directores de prueba
INSERT INTO directores (nombre, correo) VALUES
('Ing. Carlos René Angarita', 'carlosrene@ufps.edu.co'),
('Dra. Laura Marcela Martínez', 'laura.martinez@universidad.edu')
ON CONFLICT (correo) DO NOTHING;

-- 2. Líneas de investigación
INSERT INTO lineas (nombre) VALUES
('Desarrollo Web y Computación en la Nube'),
('Arquitecturas Empresariales y Microservicios'),
('Inteligencia Artificial y Aprendizaje Automático'),
('Ciberseguridad y Sistemas Distribuidos')
ON CONFLICT (nombre) DO NOTHING;

-- 3. Semilleros de investigación
INSERT INTO semilleros (nombre, sigla, grupo_investigacion, mision, vision, objetivo_general, director_id)
SELECT 
    'Semillero de Investigación en Aplicaciones Web',
    'SIAWEB',
    'GrupCIS - Grupo de Investigación en Ciencias de la Computación e Ingeniería de Software',
    'Fomentar la formación en investigación aplicada para el desarrollo de soluciones web innovadoras y escalables.',
    'Ser reconocidos institucionalmente por la calidad de sus proyectos tecnológicos y producción académica.',
    'Fortalecer competencias investigativas y técnicas en arquitectura de software y tecnologías web modernas.',
    id
FROM directores WHERE correo = 'carlosrene@ufps.edu.co'
ON CONFLICT (sigla) DO NOTHING;

INSERT INTO semilleros (nombre, sigla, grupo_investigacion, mision, vision, objetivo_general, director_id)
SELECT 
    'Semillero de Inteligencia Artificial y Big Data',
    'SIABD',
    'Grupo de Investigación en Computación Inteligente',
    'Investigar y aplicar modelos de aprendizaje computacional en problemas del entorno regional.',
    'Consolidar un grupo estudiantil referente en ciencia de datos aplicada a la educación y la salud.',
    'Desarrollar capacidades en análisis avanzado de datos y procesamiento de lenguaje natural.',
    id
FROM directores WHERE correo = 'laura.martinez@universidad.edu'
ON CONFLICT (sigla) DO NOTHING;

-- 4. Asociación Semillero - Líneas de investigación
INSERT INTO semillero_lineas (semillero_id, linea_id)
SELECT s.id, l.id
FROM semilleros s, lineas l
WHERE s.sigla = 'SIAWEB' 
  AND l.nombre IN ('Desarrollo Web y Computación en la Nube', 'Arquitecturas Empresariales y Microservicios')
ON CONFLICT DO NOTHING;

INSERT INTO semillero_lineas (semillero_id, linea_id)
SELECT s.id, l.id
FROM semilleros s, lineas l
WHERE s.sigla = 'SIABD' 
  AND l.nombre IN ('Inteligencia Artificial y Aprendizaje Automático', 'Ciberseguridad y Sistemas Distribuidos')
ON CONFLICT DO NOTHING;

-- 5. Integrantes (Cumpliendo regla: exactamente un líder por semillero)

-- Integrantes para SIAWEB (1 líder y 2 integrantes)
INSERT INTO integrantes (semillero_id, codigo, nombre, programa, es_lider)
SELECT s.id, '1152001', 'Valentina Ruiz Peña', 'Ingeniería de Sistemas', true
FROM semilleros s WHERE s.sigla = 'SIAWEB'
ON CONFLICT (semillero_id, codigo) DO NOTHING;

INSERT INTO integrantes (semillero_id, codigo, nombre, programa, es_lider)
SELECT s.id, '1152002', 'Camilo Andrés Torres', 'Ingeniería de Sistemas', false
FROM semilleros s WHERE s.sigla = 'SIAWEB'
ON CONFLICT (semillero_id, codigo) DO NOTHING;

INSERT INTO integrantes (semillero_id, codigo, nombre, programa, es_lider)
SELECT s.id, '1152003', 'Daniela Ospina Castro', 'Ingeniería de Sistemas', false
FROM semilleros s WHERE s.sigla = 'SIAWEB'
ON CONFLICT (semillero_id, codigo) DO NOTHING;

-- Integrantes para SIABD (1 líder y 1 integrante)
INSERT INTO integrantes (semillero_id, codigo, nombre, programa, es_lider)
SELECT s.id, '1153001', 'Mateo Ramírez Gómez', 'Ingeniería de Sistemas', true
FROM semilleros s WHERE s.sigla = 'SIABD'
ON CONFLICT (semillero_id, codigo) DO NOTHING;

INSERT INTO integrantes (semillero_id, codigo, nombre, programa, es_lider)
SELECT s.id, '1153002', 'Sofía Morales Vega', 'Ingeniería Electrónica', false
FROM semilleros s WHERE s.sigla = 'SIABD'
ON CONFLICT (semillero_id, codigo) DO NOTHING;
