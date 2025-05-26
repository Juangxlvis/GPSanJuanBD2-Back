package org.uniquindio.edu.co.gpsanjuan_backend.services.implementations;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import oracle.jdbc.internal.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.repositories.ExamenRepository;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.DocenteService;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DocenteServiceImp implements DocenteService {

    private final EntityManager entityManager;
    @Autowired
    private final ExamenRepository examenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public List<PreguntaBancoDTO> obtenerBancoPreguntas(Integer id_tema) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("obtener_banco_preguntas")
                .declareParameters(
                        new SqlParameter("v_id_tema", OracleTypes.NUMBER),
                        new SqlOutParameter("p_preguntas", OracleTypes.CURSOR)
                )
                .returningResultSet("p_preguntas", this::mapRowToPreguntaBancoDTO);

        Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource("v_id_tema", id_tema));

        System.out.println("llega hasta antes del return dentro del servicio docente");

        return (List<PreguntaBancoDTO>) result.get("p_preguntas");
    }
    @Transactional
    @Override
    public String crearRespuesta(String descripcion, Character esVerdadera, Integer id_pregunta) {

        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_respuesta");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_descripcion", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_es_verdadera", Character.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_pregunta", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_descripcion", descripcion);
        storedProcedure.setParameter("v_es_verdadera", esVerdadera);
        storedProcedure.setParameter("v_id_pregunta", id_pregunta);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        // Obtener el valor del parámetro de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        // Retornar el mensaje
        return mensaje;
    }

    @Override
    @Transactional
    public String crearExamen(CrearExamenDTO examenDTO) throws ParseException {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_examen");

        storedProcedure.registerStoredProcedureParameter("v_tiempo_max", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_numero_preguntas", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_porcentaje_curso", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_nombre", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_descripcion", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_porcentaje_aprobatorio", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_fecha_hora_inicio", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_fecha_hora_fin", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_num_preguntas_aleatorias", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_tema", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_docente", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_grupo", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_pct_facil",       Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_pct_media",       Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_pct_dificil",     Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_modo",            String.class,  ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);
        storedProcedure.registerStoredProcedureParameter("v_error", String.class, ParameterMode.OUT);


        storedProcedure.setParameter("v_tiempo_max", examenDTO.tiempo_maximo());
        storedProcedure.setParameter("v_numero_preguntas", examenDTO.numero_preguntas());
        storedProcedure.setParameter("v_porcentaje_curso", examenDTO.porcentaje_curso());
        storedProcedure.setParameter("v_nombre", examenDTO.nombre());
        storedProcedure.setParameter("v_descripcion", examenDTO.descripcion());
        storedProcedure.setParameter("v_porcentaje_aprobatorio", examenDTO.porcentaje_aprobatorio());

        storedProcedure.setParameter("v_fecha_hora_inicio", examenDTO.fecha_hora_inicio());
        storedProcedure.setParameter("v_fecha_hora_fin", examenDTO.fecha_hora_limite());

        storedProcedure.setParameter("v_num_preguntas_aleatorias", examenDTO.numero_preguntas_aleatorias());
        storedProcedure.setParameter("v_id_tema", examenDTO.tema_id());
        storedProcedure.setParameter("v_id_docente", examenDTO.docente_id());
        storedProcedure.setParameter("v_id_grupo", examenDTO.grupo_id());
        storedProcedure.setParameter("v_pct_facil", examenDTO.pct_facil());
        storedProcedure.setParameter("v_pct_media", examenDTO.pct_media());
        storedProcedure.setParameter("v_pct_dificil", examenDTO.pct_dificil());
        storedProcedure.setParameter("v_modo", examenDTO.modoAsignacion());

        storedProcedure.execute();

        // Leer mensaje de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");
        String error   = (String) storedProcedure.getOutputParameterValue("v_error");
        if (error != null && !error.isBlank()) {
            throw new RuntimeException("Error al crear examen: " + error);
        }
        return mensaje;

    }

    @Override
    @Transactional
    public String crearPregunta(String enunciado, Character esPublica, String tipoPregunta, Integer idTema, Integer idDocente) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_pregunta");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_enunciado", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_es_publica", Character.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_tipo_pregunta", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_tema", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_docente", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_enunciado", enunciado);
        storedProcedure.setParameter("v_es_publica", esPublica);
        storedProcedure.setParameter("v_tipo_pregunta", tipoPregunta);
        storedProcedure.setParameter("v_id_tema", idTema);
        storedProcedure.setParameter("v_id_docente", idDocente);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        // Obtener el valor del parámetro de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        // Retornar el mensaje
        return mensaje;
    }

    @Override
    @Transactional
    public String calificarExamen(Integer idPresentacionExamen) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("calificar_examen");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_id_presentacion_examen", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_id_presentacion_examen", idPresentacionExamen);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        // Obtener el valor del parámetro de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        // Retornar el mensaje
        return mensaje;
    }

    @Override
    @Transactional
    public List<PreguntaBancoDTO> obtenerPreguntasDocente(Integer idDocente) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("obtener_preguntas_docente")
                .declareParameters(
                        new SqlParameter("v_id_docente", OracleTypes.NUMBER),
                        new SqlOutParameter("p_preguntas", OracleTypes.CURSOR)
                )
                .returningResultSet("p_preguntas", this::mapRowToPreguntaBancoDTO);

        Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource("v_id_docente", idDocente));

        return (List<PreguntaBancoDTO>) result.get("p_preguntas");
    }

    @Override
    @Transactional
    public List<ExamenDTO> obtenerExamenesDocente(Integer id_docente) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("obtener_examenes_docente")
                .declareParameters(
                        new SqlParameter("v_id_docente", OracleTypes.NUMBER),
                        new SqlOutParameter("p_examenes", OracleTypes.CURSOR)
                )
                .returningResultSet("p_examenes", this::mapRowToExamenDTO);

        Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource("v_id_docente", id_docente));

        return (List<ExamenDTO>) result.get("p_examenes");
    }

    private ExamenDTO mapRowToExamenDTO(ResultSet rs, int rowNum) throws SQLException {
        return new ExamenDTO(
                rs.getInt("id_examen"),
                rs.getInt("tiempo_max"),
                rs.getInt("numero_preguntas"),
                rs.getFloat("porcentaje_curso"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getInt("porcentaje_aprobatorio"),
                rs.getDate("fecha_hora_inicio"),
                rs.getDate("fecha_hora_fin"),
                rs.getInt("num_preguntas_aleatorias"),
                rs.getInt("id_tema"),
                rs.getInt("id_docente"),
                rs.getInt("id_grupo"),
                rs.getString("estado")
        );
    }

    @Override
    public String obtenerNombre(String id, String rol) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_nombre_usuario");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_usuario", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("rol", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_usuario", id);
        storedProcedure.setParameter("rol", "alumno");

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String nombre = (String) storedProcedure.getOutputParameterValue("res");

        return nombre;

    }

    @Override
    public List<CursoDTO> obtenerCursos(String id, String rol) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_grupos_por_usuario");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_usuario", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("rol", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_usuario", id);
        storedProcedure.setParameter("rol", "alumno");

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String json1 = (String) storedProcedure.getOutputParameterValue("res");

        Gson gson = new Gson();
        Type personListType = new TypeToken<List<CursoDTO>>() {}.getType();

        return gson.fromJson(json1, personListType);
    }


    @Override
    public List<TemasCursoDTO> obtenerTemasCurso(Integer id_grupo) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_temas_por_curso");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_grupo", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_grupo", id_grupo);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String json1 = (String) storedProcedure.getOutputParameterValue("res");
        Gson gson = new Gson();
        Type personListType = new TypeToken<List<TemasCursoDTO>>() {}.getType();

        return gson.fromJson(json1, personListType);
    }


    @Override
    public List<TemasCursoDTO> obtenerTemasDocente() {
        List<Object[]> resultados = examenRepository.obtenerCursos();
        return resultados.stream()
                .map(fila -> new TemasCursoDTO(""+fila[0], (String) fila[1]))
                .collect(Collectors.toList());
    }


    //Esta vaina es solo para permitir consultar las preguntas por tema, es como un plugin XD
    private PreguntaBancoDTO mapRowToPreguntaBancoDTO(ResultSet rs, int rowNum) throws SQLException {
        return new PreguntaBancoDTO(
                rs.getInt("id_pregunta"),
                rs.getString("enunciado"),
                rs.getString("es_publica").charAt(0),
                rs.getString("tipo_pregunta"),
                rs.getInt("id_tema"),
                rs.getInt("id_docente")
        );
    }

    @Override
    public List<GrupoDTO> obtenerGruposPorCurso(Integer idCurso) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_grupos_por_curso");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_curso", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT); // CLOB se mapea a String

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_curso", idCurso);

        try {
            // Ejecutar el procedimiento almacenado
            storedProcedure.execute();

            // Obtener el resultado (JSON como String)
            String jsonResult = (String) storedProcedure.getOutputParameterValue("res");

            // Verificar si el resultado es nulo o indicativo de error desde PL/SQL
            if (jsonResult == null || jsonResult.trim().isEmpty() || jsonResult.toLowerCase().contains("error")) {
                // Puedes lanzar una excepción personalizada o loggear el error
                System.err.println("Error desde PL/SQL o resultado vacío: " + jsonResult);
                return Collections.emptyList(); // Devolver lista vacía o manejar el error como prefieras
            }

            // Deserializar el JSON a una lista de GrupoDTO
            Gson gson = new Gson();
            Type grupoListType = new TypeToken<List<GrupoDTO>>() {}.getType();

            return gson.fromJson(jsonResult, grupoListType);

        } catch (Exception e) {
            // Manejo de excepciones (e.g., loggear el error, lanzar una excepción personalizada)
            e.printStackTrace(); // Considera un logging más robusto
            return Collections.emptyList(); // O lanzar una excepción
        }
    }
}
