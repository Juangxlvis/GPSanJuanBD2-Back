package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record CrearExamenDTO(
        Integer tiempo_maximo,
        Integer numero_preguntas,
        Integer porcentaje_curso,
        String nombre,
        String descripcion,
        Integer porcentaje_aprobatorio,
        String fecha_hora_inicio,
        String fecha_hora_limite,
        Integer numero_preguntas_aleatorias,
        Integer tema_id,
        Integer docente_id,
        Integer grupo_id
) {
}
