package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record RespuestaDTO (

        Integer respuestaId,
        String descripcion,
        boolean esVerdadera,

        PreguntaDTO pregunta,

        List<PresentacionPreguntaDTO> presentacionesPregunta

){
}
