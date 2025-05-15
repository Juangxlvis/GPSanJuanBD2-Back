package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record PreguntaExamenDTO(

        Integer preguntaExamenId,
        Double porcentajeExamen,

        Integer tiempoPregunta,

        boolean tieneTiempoMaximo,

        PreguntaDTO pregunta,

        ExamenDTO examen
) {
}
