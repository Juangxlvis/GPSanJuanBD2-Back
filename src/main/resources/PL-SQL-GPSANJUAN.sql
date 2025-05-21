
CREATE OR REPLACE PROCEDURE login(p_id IN VARCHAR2, rol IN VARCHAR2, res OUT CHAR) AS
    cursor c_alumno is select *
                       from alumno a
                       where a.ID_ALUMNO = p_id;
    cursor c_docente is select *
                        from docente d
                        where d.ID_DOCENTE = p_id;
    v_alumno  alumno%ROWTYPE;
    v_docente docente%ROWTYPE;
BEGIN
    if rol = 'docente' then

        open c_docente;
        fetch c_docente into v_docente;
        if c_docente%FOUND then
            res := '1';
            dbms_output.put_line('docente encontrado');
        else
            res := '0';
        end if;
        close c_docente;

    else

        open c_alumno;
        fetch c_alumno into v_alumno;
        if c_alumno%FOUND then
            res := '1';
            dbms_output.put_line('alumno encontrado');
        else
            res := '0';
        end if;
        close c_alumno;
    end if;
end;

-- Obtener el nombre dado el id del usuario y el rol.
create or replace procedure get_nombre_usuario(p_id_usuario in varchar2, rol in varchar2, res out varchar2) as
BEGIN
    if rol = 'docente' then
        SELECT d.NOMBRE || ' ' || d.APELLIDO
        INTO res
        from docente d
        where d.ID_DOCENTE = p_id_usuario;
    else
        SELECT a.NOMBRE || ' ' || a.APELLIDO
        INTO res
        from alumno a
        where a.ID_ALUMNO = p_id_usuario;
    end if;
end;


-- (HECHO) Procedimiento que retorna los grupos de un usuario dado su id y rol.
-- Retorna un JSON con los grupos del usuario, especficando el id del grupo, el nombre del grupo y el nombre del curso.
CREATE OR REPLACE PROCEDURE get_grupos_por_usuario(
    p_id_usuario IN VARCHAR2,
    rol_in IN VARCHAR2,
    res OUT CLOB
) AS
    v_json_result CLOB;
BEGIN
    IF LOWER(rol_in) = 'docente' THEN
        SELECT COALESCE(
                       JSON_ARRAYAGG(
                               JSON_OBJECT(
                                       'id_grupo'     VALUE id_grupo,
                                       'nombre_grupo' VALUE nombre_grupo,
                                   -- Apply workaround for nombre_curso
                                       'nombre_curso' VALUE '"' || REPLACE(nombre_curso, '"', '\"') || '"'
                                       FORMAT JSON -- Keep FORMAT JSON, it might help with other fields
                               )
                       ),
                       JSON_ARRAY()
               )
        INTO v_json_result
        FROM (
                 SELECT g.ID_GRUPO id_grupo,
                        g.NOMBRE   nombre_grupo,
                        c.NOMBRE   nombre_curso
                 FROM docente d
                          JOIN grupo g ON (d.ID_DOCENTE = g.id_docente)
                          JOIN curso c ON (c.id_curso = g.id_curso)
                 WHERE d.ID_DOCENTE = p_id_usuario
             );
    ELSIF LOWER(rol_in) = 'alumno' THEN
        SELECT COALESCE(
                       JSON_ARRAYAGG(
                               JSON_OBJECT(
                                       'id_grupo'     VALUE id_grupo,
                                       'nombre_grupo' VALUE nombre_grupo,
                                   -- Apply workaround for nombre_curso
                                       'nombre_curso' VALUE '"' || REPLACE(nombre_curso, '"', '\"') || '"'
                                       FORMAT JSON
                               )
                       ),
                       JSON_ARRAY()
               )
        INTO v_json_result
        FROM (
                 SELECT ag.ID_GRUPO id_grupo, g.NOMBRE nombre_grupo, c.NOMBRE nombre_curso
                 FROM alumno_grupo ag
                          JOIN grupo g ON (ag.id_grupo = g.id_grupo)
                          JOIN curso c ON (c.id_curso = g.id_curso)
                 WHERE ag.id_alumno = p_id_usuario
             );
    ELSE
        v_json_result := JSON_ARRAY();
    END IF;

    res := v_json_result;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error in get_grupos_por_usuario: ' || SQLERRM);
        -- Return a valid JSON error object
        res := JSON_OBJECT('error' VALUE ('Error en el procedimiento almacenado: ' || SQLERRM) FORMAT JSON);
END get_grupos_por_usuario;


-- obtenerExamenesPresentadosAlumnoGrupo()
-- @descripción: Se encarga de obtener la presentacion_examen de un alumno específico en un grupo específico
-- @return: retorna un cursor con todos las presentación_examen que cumplan
-- DTO-in: id-alumno, id_grupo
-- DTO-out: Lista de - > (Presentacion_examen)


CREATE OR REPLACE PROCEDURE tomar_examenes_alumno_grupo(
    v_id_alumno IN alumno.id_alumno%TYPE,
    v_id_grupo IN grupo.id_grupo%TYPE,
    p_examenes OUT SYS_REFCURSOR
)
    IS
BEGIN
    OPEN p_examenes FOR
        SELECT pe.*
        FROM presentacion_examen pe
                 JOIN examen e ON pe.id_examen = e.id_examen
                 JOIN Grupo g ON e.id_grupo = g.id_grupo
        WHERE pe.id_alumno = v_id_alumno
          AND g.id_grupo = v_id_grupo;
END tomar_examenes_alumno_grupo;
/

(HECHO)
CREATE OR REPLACE PROCEDURE GET_PRESENTACION_EXAMEN_ALUMNO_GRUPO(
    p_id_alumno IN NUMBER,
    p_id_grupo  IN NUMBER,
    res         OUT CLOB
)
AS
BEGIN
  SELECT JSON_ARRAYAGG(
           JSON_OBJECT(
             'idExamen'      VALUE pe.id_presentacion_examen,
             'nombreExamen'  VALUE e.nombre,
             'calificacion'  VALUE TO_CHAR(pe.calificacion)
             FORMAT JSON
           )
         )
    INTO res
    FROM presentacion_examen pe
    JOIN examen e   ON pe.id_examen = e.id_examen
    JOIN grupo g    ON e.id_grupo   = g.id_grupo
   WHERE pe.terminado    = '1'
     AND pe.calificacion IS NOT NULL
     AND pe.id_alumno    = p_id_alumno
     AND g.id_grupo      = p_id_grupo;
END GET_PRESENTACION_EXAMEN_ALUMNO_GRUPO;
/


(HECHO)
-- Se compara cuales son los examenes que un alumno tiene pendientes por presentar en un grupo específico.
-- Esto se compara observando cuales son los examenes existentes y restando los examenes que ya ha presentado el alumno.
create or replace procedure get_examenes_grupo_pendientes_por_alumno(p_id_alumno in number, p_id_grupo in number, res out clob) as
    v_json CLOB;
BEGIN
    SELECT JSON_ARRAYAGG(
                   JSON_OBJECT(
                           'id_examen' VALUE id_examen,
                           'tiempo_max' VALUE TIEMPO_MAX,
                           'numero_preguntas' VALUE NUMERO_PREGUNTAS,
                           'porcentaje_aprobatorio' VALUE PORCENTAJE_APROBATORIO,
                           'nombre' VALUE nombre,
                           'porcentaje_curso' VALUE PORCENTAJE_CURSO,
                           'fecha_hora_inicio' VALUE TO_CHAR(fecha_hora_inicio, 'DD/MM/YYYY'),
                           'fecha_hora_fin' VALUE TO_CHAR(fecha_hora_fin, 'HH24:MI'),
                           'tema' VALUE '"' || titulo || '"'
                           FORMAT JSON
                   )
           )
    INTO v_json
    FROM (SELECT e.*, t.titulo as titulo
          FROM examen e
                   join tema t on (e.id_tema = t.id_tema)
                   join GRUPO g on (g.ID_GRUPO = p_id_grupo AND e.id_grupo = g.id_grupo)
          WHERE e.id_examen NOT IN (SELECT pe.id_examen
                                    FROM presentacion_examen pe
                                    WHERE pe.id_alumno = p_id_alumno));
    res := v_json;
END get_examenes_grupo_pendientes_por_alumno;

-- 2. crear_presentacion_examen
-- Inserta una nueva fila en presentacion_examen con todos los campos que tu servicio espera.
CREATE OR REPLACE PROCEDURE crear_presentacion_examen(
    p_tiempo                  IN NUMBER,
    p_terminado               IN CHAR,
    p_ip                      IN VARCHAR2,
    p_fecha_hora_presentacion IN DATE,
    p_id_examen               IN NUMBER,
    p_id_alumno               IN NUMBER,
    p_mensaje                 OUT VARCHAR2
) AS
BEGIN
    INSERT INTO presentacion_examen (
        tiempo,
        terminado,
        ip_source,
        fecha_hora_presentacion,
        id_examen,
        id_alumno
    ) VALUES (
        p_tiempo,
        p_terminado,
        p_ip,
        p_fecha_hora_presentacion,
        p_id_examen,
        p_id_alumno
    );
    p_mensaje := 'Presentación creada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        p_mensaje := 'Error al crear presentación: ' || SQLERRM;
END crear_presentacion_examen;
/


CREATE OR REPLACE PROCEDURE crear_presentacion_examen(
    v_id_examen IN presentacion_examen.id_examen%TYPE,
    v_id_alumno IN presentacion_examen.id_alumno%TYPE,
    v_mensaje OUT VARCHAR2 -- Mover al final de la lista de parámetros y utilizar OUT
)
    IS

BEGIN
    INSERT INTO presentacion_examen (tiempo, terminado, calificacion, ip_source, fecha_hora_presentacion, id_examen, id_alumno)
    Values (null, '0', 0, '192.168.0.1', sysdate, v_id_examen, v_id_alumno);

    v_mensaje := 'presentación_examen se ha creado exitosamente';

EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear la presentacion_examen:   ' || SQLERRM;

END crear_presentacion_examen;
/


-- este procedimiento se encarga de calificar el examen una vez presentado
CREATE OR REPLACE PROCEDURE calificar_examen (
    v_id_presentacion_examen IN presentacion_examen.id_presentacion_examen%TYPE,
    v_calificacion IN presentacion_examen.calificacion%TYPE,
    v_mensaje OUT VARCHAR2
) IS
BEGIN
    UPDATE presentacion_examen
    SET calificacion = v_calificacion
    WHERE id_presentacion_examen = v_id_presentacion_examen;

    v_mensaje := 'Examen calificado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al calificar el examen: ' || SQLERRM;
END calificar_examen;
/


CREATE OR REPLACE PROCEDURE  calificar_pregunta (
    v_id_presentacion_pregunta IN presentacion_pregunta.id_presentacion_pregunta%TYPE,
    v_respuesta_correcta IN presentacion_pregunta.respuesta_correcta%TYPE,
    v_mensaje out varchar2
) IS
BEGIN
    UPDATE presentacion_pregunta
    SET respuesta_correcta = v_respuesta_correcta
    WHERE id_presentacion_pregunta = v_id_presentacion_pregunta;

    v_mensaje := 'Pregunta calificada exitosamente';

END calificar_pregunta;
/


CREATE OR REPLACE PROCEDURE crear_pregunta (
    v_enunciado       IN pregunta.enunciado%TYPE,
    v_es_publica      IN pregunta.es_publica%TYPE,
    v_tipo_pregunta   IN pregunta.tipo_pregunta%TYPE,
    v_id_tema         IN pregunta.id_tema%TYPE,
    v_id_docente      IN pregunta.id_docente%TYPE,
    v_mensaje         OUT VARCHAR2 -- Mover al final de la lista de parámetros y utilizar OUT
)
IS
BEGIN
    INSERT INTO pregunta (enunciado, es_publica, tipo_pregunta, id_tema, id_docente, estado)
    VALUES (v_enunciado, v_es_publica, v_tipo_pregunta, v_id_tema, v_id_docente, 'creada');
    v_mensaje := 'Pregunta creada exitosamente';

    EXCEPTION
     WHEN OTHERS THEN
            v_mensaje := 'Error al crear la pregunta: ' || SQLERRM;

END crear_pregunta;
/


-- Obtener preguntas por examen
create or replace procedure get_preguntas_por_examen (p_id_examen number, res clob) is
begin
    select JSON_ARRAYAGG(
        JSON_OBJECT(
            'id_pregunta' VALUE id_pregunta,
            'enunciado' VALUE '"' || enunciado || '"',
            'tipo_pregunta' VALUE tipo_pregunta,
            'id_tema' VALUE id_tema,
            'id_docente' VALUE id_docente
            FORMAT JSON
        )
    )
    into res
    from pregunta p
    join PREGUNTA_EXAMEN pe on p.id_pregunta = pe.id_pregunta
    where pe.id_examen = p_id_examen;
end get_preguntas_por_examen;
/


create or replace procedure get_temas_por_curso (p_id_grupo IN number, res out clob) IS
BEGIN
    SELECT JSON_ARRAYAGG(
                   JSON_OBJECT(
                           'id_tema' VALUE id_tema,
                           'titulo' VALUE '"' || titulo || '"'
                           FORMAT JSON
                   )
           )
    INTO res
    FROM (select t.ID_TEMA id_tema, t.TITULO titulo from tema t join unidad u on t.UNIDAD_ID_UNIDAD = u.ID_UNIDAD
    join curso c on  u.ID_CURSO = c.ID_CURSO
    join grupo g on c.ID_CURSO = g.ID_CURSO
    where g.ID_GRUPO = p_id_grupo);

END get_temas_por_curso;
/


-- 1. Obtener banco de preguntas por tema (públicas y privadas)
CREATE OR REPLACE PROCEDURE get_banco_preguntas(
    p_id_tema  IN NUMBER,
    res        OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'id_pregunta' VALUE p.id_pregunta,
                   'enunciado'    VALUE p.enunciado,
                   'es_publica'   VALUE p.es_publica,
                   'tipo'         VALUE p.tipo_pregunta
                   FORMAT JSON
               )
           )
    INTO res
    FROM pregunta p
    WHERE p.id_tema = p_id_tema
      AND p.estado = 'Activa';
END get_banco_preguntas;
/


-- 2. CRUD de opciones de respuesta
CREATE OR REPLACE PROCEDURE crear_respuesta(
    v_descripcion   IN RESPUESTA.DESCRIPCION%TYPE,
    v_es_verdadera  IN RESPUESTA.ES_VERDADERA%TYPE,
    v_id_pregunta   IN RESPUESTA.ID_PREGUNTA%TYPE,
    v_mensaje       OUT VARCHAR2
) AS
BEGIN
    INSERT INTO RESPUESTA (
        ID_RESPUESTA,
        DESCRIPCION,
        ES_VERDADERA,
        ID_PREGUNTA
    )
    VALUES (
        RESPUESTA_SEQ.NEXTVAL,   -- genera el nuevo ID
        v_descripcion,
        v_es_verdadera,
        v_id_pregunta
    );
    v_mensaje := 'Respuesta creada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear respuesta: ' || SQLERRM;
END crear_respuesta;
/
CREATE OR REPLACE PROCEDURE actualizar_respuesta(
    v_id_respuesta  IN RESPUESTA.ID_RESPUESTA%TYPE,
    v_descripcion   IN RESPUESTA.DESCRIPCION%TYPE,
    v_es_verdadera  IN RESPUESTA.ES_VERDADERA%TYPE,
    v_mensaje       OUT VARCHAR2
) AS
BEGIN
    UPDATE RESPUESTA
      SET DESCRIPCION  = v_descripcion,
          ES_VERDADERA = v_es_verdadera
    WHERE ID_RESPUESTA = v_id_respuesta;
    v_mensaje := 'Respuesta actualizada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al actualizar respuesta: ' || SQLERRM;
END actualizar_respuesta;
/

CREATE OR REPLACE PROCEDURE borrar_respuesta(
    v_id_respuesta  IN RESPUESTA.ID_RESPUESTA%TYPE,
    v_mensaje       OUT VARCHAR2
) AS
BEGIN
    DELETE FROM RESPUESTA
    WHERE ID_RESPUESTA = v_id_respuesta;
    v_mensaje := 'Respuesta eliminada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al eliminar respuesta: ' || SQLERRM;
END borrar_respuesta;
/

-- 3. Obtener unidades y temas por curso
CREATE OR REPLACE PROCEDURE get_unidades_por_curso(
    p_id_curso IN NUMBER,
    res        OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'id_unidad'   VALUE u.id_unidad,
                   'titulo'      VALUE u.titulo,
                   'descripcion' VALUE u.descripcion,
                   'temas'       VALUE (
                       SELECT JSON_ARRAYAGG(
                                  JSON_OBJECT(
                                    'id_tema' VALUE t.id_tema,
                                    'nombre'  VALUE t.titulo
                                  ) FORMAT JSON
                              )
                       FROM tema t
                       WHERE t.unidad_id_unidad = u.id_unidad
                   ) FORMAT JSON
               ) FORMAT JSON
           )
      INTO res
      FROM unidad u
     WHERE u.id_curso = p_id_curso;
END get_unidades_por_curso;
/

--4 obtener el horario de un grupo:
CREATE OR REPLACE PROCEDURE get_horario_grupo(
    p_id_grupo IN NUMBER,
    res        OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'dia'         VALUE bh.dia,
                   'hora_inicio' VALUE TO_CHAR(bh.hora_inicio, 'HH24:MI'),
                   'hora_fin'    VALUE TO_CHAR(bh.hora_fin, 'HH24:MI'),
                   'lugar'       VALUE bh.lugar
                   FORMAT JSON
               )
           )
    INTO res
    FROM horario h
    JOIN bloque_horario bh ON h.id_bloque_horario = bh.id_bloque_horario
    WHERE h.id_grupo = p_id_grupo;
END get_horario_grupo;
/

CREATE OR REPLACE PROCEDURE crear_examen(
    v_tiempo_max             IN examen.tiempo_max%TYPE,
    v_numero_preguntas       IN examen.numero_preguntas%TYPE,
    v_porcentaje_curso       IN examen.porcentaje_curso%TYPE,
    v_nombre                 IN examen.nombre%TYPE,
    v_descripcion            IN examen.descripcion%TYPE,
    v_porcentaje_aprobatorio IN examen.porcentaje_aprobatorio%TYPE,
    v_fecha_inicio           IN examen.fecha_hora_inicio%TYPE,
    v_fecha_fin              IN examen.fecha_hora_fin%TYPE,
    v_num_preguntas_aleatorias IN examen.num_preguntas_aleatorias%TYPE,
    v_id_tema                IN examen.id_tema%TYPE,
    v_id_docente             IN examen.id_docente%TYPE,  
    v_id_grupo               IN examen.id_grupo%TYPE,
    v_mensaje                OUT VARCHAR2
) AS
BEGIN
    INSERT INTO examen(
        tiempo_max, numero_preguntas, porcentaje_curso, nombre, descripcion,
        porcentaje_aprobatorio, fecha_hora_inicio, fecha_hora_fin, num_preguntas_aleatorias,
        id_tema, id_docente, id_grupo, estado
    ) VALUES (
        v_tiempo_max, v_numero_preguntas, v_porcentaje_curso, v_nombre, v_descripcion,
        v_porcentaje_aprobatorio, v_fecha_inicio, v_fecha_fin, v_num_preguntas_aleatorias,
        v_id_tema, v_id_docente, v_id_grupo, 'Activa'
    );

    v_mensaje := 'Examen creado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear examen: ' || SQLERRM;
END crear_examen;
/



-- 5. Inscribir alumno a grupo
CREATE OR REPLACE PROCEDURE inscribir_alumno_grupo(
    p_id_alumno IN alumno.id_alumno%TYPE,
    p_id_grupo  IN grupo.id_grupo%TYPE,
    v_mensaje   OUT VARCHAR2
) AS
BEGIN
    INSERT INTO alumno_grupo(id_alumno, id_grupo)
    VALUES (p_id_alumno, p_id_grupo);
    v_mensaje := 'Alumno inscrito correctamente';
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        v_mensaje := 'El alumno ya está inscrito en este grupo';
    WHEN OTHERS THEN
        v_mensaje := 'Error al inscribir alumno: ' || SQLERRM;
END inscribir_alumno_grupo;
/

-- 6. Obtener estadísticas de examen
CREATE OR REPLACE PROCEDURE get_estadisticas_examen(
    p_id_examen IN NUMBER,
    res         OUT CLOB
) AS
BEGIN
    SELECT JSON_OBJECT(
               'total_presentaciones' VALUE COUNT(pe.id_presentacion_examen),
               'promedio_nota'        VALUE TO_CHAR(AVG(pe.calificacion), 'FM9990.00'),
               'nota_maxima'          VALUE TO_CHAR(MAX(pe.calificacion)),
               'nota_minima'          VALUE TO_CHAR(MIN(pe.calificacion))
               FORMAT JSON
           )
    INTO res
    FROM presentacion_examen pe
    WHERE pe.id_examen = p_id_examen;
END get_estadisticas_examen;
/



-- 4. Admin: Crear usuario
CREATE OR REPLACE PROCEDURE crear_usuario(
p_id_usuario IN VARCHAR2,
p_rol        IN VARCHAR2,
v_mensaje    OUT VARCHAR2
) AS
BEGIN
INSERT INTO usuario(id_usuario, rol)
VALUES(p_id_usuario, p_rol);
v_mensaje := 'Usuario creado correctamente';
EXCEPTION
WHEN OTHERS THEN
v_mensaje := 'Error al crear usuario: ' || SQLERRM;
END crear_usuario;
/

-- 5. Admin: Actualizar usuario
CREATE OR REPLACE PROCEDURE actualizar_usuario(
p_id_usuario IN VARCHAR2,
p_rol        IN VARCHAR2,
v_mensaje    OUT VARCHAR2
) AS
BEGIN
UPDATE usuario
SET rol = p_rol
WHERE id_usuario = p_id_usuario;
v_mensaje := 'Usuario actualizado correctamente';
EXCEPTION
WHEN OTHERS THEN
v_mensaje := 'Error al actualizar usuario: ' || SQLERRM;
END actualizar_usuario;
/

-- 6. Admin: Eliminar usuario
CREATE OR REPLACE PROCEDURE eliminar_usuario(
p_id_usuario IN VARCHAR2,
v_mensaje    OUT VARCHAR2
) AS
BEGIN
DELETE FROM usuario
WHERE id_usuario = p_id_usuario;
v_mensaje := 'Usuario eliminado correctamente';
EXCEPTION
WHEN OTHERS THEN
v_mensaje := 'Error al eliminar usuario: ' || SQLERRM;
END eliminar_usuario;
/


CREATE OR REPLACE PROCEDURE get_calificaciones_alumno(
    p_id_alumno IN NUMBER,
    res         OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'examen'       VALUE e.nombre,
            'calificacion' VALUE TO_CHAR(pe.calificacion),
            'fecha'        VALUE TO_CHAR(pe.fecha_hora_presentacion, 'YYYY-MM-DD HH24:MI')
            FORMAT JSON
        )
    )
    INTO res
    FROM presentacion_examen pe
    JOIN examen e ON pe.id_examen = e.id_examen
    WHERE pe.id_alumno = p_id_alumno
      AND pe.calificacion IS NOT NULL;
END get_calificaciones_alumno;
/
--------------------------------------------------------------------------------
-- CRUD de TEMA
--------------------------------------------------------------------------------

-- 1. Crear tema
CREATE OR REPLACE PROCEDURE crear_tema(
    p_id_tema      IN TEMA.ID_TEMA%TYPE,
    p_titulo       IN TEMA.TITULO%TYPE,
    p_descripcion  IN TEMA.DESCRIPCION%TYPE,
    p_unidad       IN TEMA.UNIDAD_ID_UNIDAD%TYPE,
    v_mensaje      OUT VARCHAR2
) AS
BEGIN
    INSERT INTO TEMA(id_tema, titulo, descripcion, unidad_id_unidad)
    VALUES(p_id_tema, p_titulo, p_descripcion, p_unidad);

    v_mensaje := 'Tema creado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear tema: ' || SQLERRM;
END crear_tema;
/
-- 2. Actualizar tema
CREATE OR REPLACE PROCEDURE actualizar_tema(
    p_id_tema      IN TEMA.ID_TEMA%TYPE,
    p_titulo       IN TEMA.TITULO%TYPE,
    p_descripcion  IN TEMA.DESCRIPCION%TYPE,
    p_unidad       IN TEMA.UNIDAD_ID_UNIDAD%TYPE,
    v_mensaje      OUT VARCHAR2
) AS
BEGIN
    UPDATE TEMA
       SET titulo            = p_titulo,
           descripcion       = p_descripcion,
           unidad_id_unidad  = p_unidad
     WHERE id_tema = p_id_tema;

    v_mensaje := 'Tema actualizado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al actualizar tema: ' || SQLERRM;
END actualizar_tema;
/
-- 3. Eliminar tema
CREATE OR REPLACE PROCEDURE eliminar_tema(
    p_id_tema  IN TEMA.ID_TEMA%TYPE,
    v_mensaje  OUT VARCHAR2
) AS
BEGIN
    DELETE FROM TEMA
     WHERE id_tema = p_id_tema;

    v_mensaje := 'Tema eliminado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al eliminar tema: ' || SQLERRM;
END eliminar_tema;
/
--------------------------------------------------------------------------------
-- CRUD de GRUPO
--------------------------------------------------------------------------------

-- 1. Crear grupo
CREATE OR REPLACE PROCEDURE crear_grupo(
    p_id_grupo   IN GRUPO.ID_GRUPO%TYPE,
    p_jornada    IN GRUPO.JORNADA%TYPE,
    p_nombre     IN GRUPO.NOMBRE%TYPE,
    p_periodo    IN GRUPO.PERIODO%TYPE,
    p_id_doc     IN GRUPO.ID_DOCENTE%TYPE,
    p_id_curso   IN GRUPO.ID_CURSO%TYPE,
    v_mensaje    OUT VARCHAR2
) AS
BEGIN
    INSERT INTO GRUPO(id_grupo, jornada, nombre, periodo, id_docente, id_curso)
    VALUES(p_id_grupo, p_jornada, p_nombre, p_periodo, p_id_doc, p_id_curso);

    v_mensaje := 'Grupo creado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear grupo: ' || SQLERRM;
END crear_grupo;
/
-- 2. Actualizar grupo
CREATE OR REPLACE PROCEDURE actualizar_grupo(
    p_id_grupo   IN GRUPO.ID_GRUPO%TYPE,
    p_jornada    IN GRUPO.JORNADA%TYPE,
    p_nombre     IN GRUPO.NOMBRE%TYPE,
    p_periodo    IN GRUPO.PERIODO%TYPE,
    p_id_doc     IN GRUPO.ID_DOCENTE%TYPE,
    p_id_curso   IN GRUPO.ID_CURSO%TYPE,
    v_mensaje    OUT VARCHAR2
) AS
BEGIN
    UPDATE GRUPO
       SET jornada    = p_jornada,
           nombre     = p_nombre,
           periodo    = p_periodo,
           id_docente = p_id_doc,
           id_curso   = p_id_curso
     WHERE id_grupo = p_id_grupo;

    v_mensaje := 'Grupo actualizado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al actualizar grupo: ' || SQLERRM;
END actualizar_grupo;
/
-- 3. Eliminar grupo
CREATE OR REPLACE PROCEDURE eliminar_grupo(
    p_id_grupo IN GRUPO.ID_GRUPO%TYPE,
    v_mensaje  OUT VARCHAR2
) AS
BEGIN
    DELETE FROM GRUPO
     WHERE id_grupo = p_id_grupo;

    v_mensaje := 'Grupo eliminado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al eliminar grupo: ' || SQLERRM;
END eliminar_grupo;
/

CREATE OR REPLACE PROCEDURE obtener_banco_preguntas(
    v_id_tema     IN  NUMBER,
    p_preguntas   OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_preguntas FOR
        SELECT 
            id_pregunta,
            enunciado,
            es_publica,
            tipo_pregunta,
            id_tema,
            id_docente
        FROM pregunta
        WHERE id_tema = v_id_tema
          AND estado = 'Activa';
END obtener_banco_preguntas;
/

CREATE OR REPLACE PROCEDURE obtener_preguntas_docente(
    v_id_docente   IN  NUMBER,
    p_preguntas    OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_preguntas FOR
        SELECT
            id_pregunta,
            enunciado,
            es_publica,
            tipo_pregunta,
            id_tema,
            id_docente
        FROM pregunta
        WHERE id_docente = v_id_docente
          AND estado     = 'Activa';
END obtener_preguntas_docente;
/

CREATE OR REPLACE PROCEDURE obtener_examenes_docente(
    v_id_docente   IN  NUMBER,
    p_examenes     OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_examenes FOR
        SELECT
            id_examen,
            tiempo_max,
            numero_preguntas,
            porcentaje_curso,
            nombre,
            descripcion,
            porcentaje_aprobatorio,
            fecha_hora_inicio,
            fecha_hora_fin,
            num_preguntas_aleatorias,
            id_tema,
            id_docente,
            id_grupo,
            estado
        FROM examen
        WHERE id_docente = v_id_docente
        AND estado     = 'Activa'   -- opcional: solo exámenes activos
        ORDER BY fecha_hora_inicio;
END obtener_examenes_docente;
/

CREATE OR REPLACE PROCEDURE obtener_nota(
    v_id_presentacion_examen IN NUMBER,
    v_nota                   OUT NUMBER
) AS
BEGIN
    -- Intenta leer la calificación de la presentación
    SELECT calificacion
      INTO v_nota
      FROM presentacion_examen
     WHERE id_presentacion_examen = v_id_presentacion_examen;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Si no existe la presentación o no tiene calificación, retornar 0
        v_nota := 0;
    WHEN OTHERS THEN
        -- En caso de cualquier otro error, también devolvemos 0
        v_nota := 0;
END obtener_nota;
/



CREATE OR REPLACE PROCEDURE responder_pregunta(
    p_id_presentacion_examen IN NUMBER,
    p_id_pregunta            IN NUMBER,
    p_id_respuesta           IN NUMBER,
    p_mensaje                OUT VARCHAR2
) AS
BEGIN
    INSERT INTO presentacion_pregunta (
        id_presentacion_pregunta,
        respuesta_correcta,
        id_pregunta,
        id_respuesta,
        id_presentacion_examen
    ) VALUES (
        presentacion_pregunta_seq.NEXTVAL,  -- secuencia para PK
        NULL,                               -- se calificará más tarde
        p_id_pregunta,
        p_id_respuesta,
        p_id_presentacion_examen
    );

    p_mensaje := 'Respuesta registrada correctamente';
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        p_mensaje := 'Ya existe respuesta para esta pregunta en la presentación';
    WHEN OTHERS THEN
        p_mensaje := 'Error al registrar respuesta: ' || SQLERRM;
END responder_pregunta;
/
CREATE OR REPLACE PROCEDURE finalizar_presentacion_examen(
    p_id_presentacion_examen IN NUMBER,
    p_mensaje                OUT VARCHAR2
) AS
    v_total_preguntas    NUMBER;
    v_correctas          NUMBER;
    v_nota_final         NUMBER;
BEGIN
    -- Contamos cuántas preguntas tuvo la presentación
    SELECT COUNT(*) 
      INTO v_total_preguntas
      FROM presentacion_pregunta
     WHERE id_presentacion_examen = p_id_presentacion_examen;

    IF v_total_preguntas = 0 THEN
        p_mensaje := 'No hay preguntas asociadas a esta presentación';
        RETURN;
    END IF;

    -- Contamos cuántas respuestas resultaron correctas
    SELECT COUNT(*) 
      INTO v_correctas
      FROM presentacion_pregunta
     WHERE id_presentacion_examen = p_id_presentacion_examen
       AND respuesta_correcta = 'S';

    -- Calculamos la nota como proporción de correctas * 100
    v_nota_final := ROUND((v_correctas / v_total_preguntas) * 100, 2);

    -- Actualizamos la presentación
    UPDATE presentacion_examen
       SET terminado    = '1',
           calificacion = v_nota_final
     WHERE id_presentacion_examen = p_id_presentacion_examen;

    p_mensaje := 'Presentación finalizada. Nota final: ' || v_nota_final;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_mensaje := 'Presentación no encontrada';
    WHEN OTHERS THEN
        p_mensaje := 'Error al finalizar presentación: ' || SQLERRM;
END finalizar_presentacion_examen;
/
