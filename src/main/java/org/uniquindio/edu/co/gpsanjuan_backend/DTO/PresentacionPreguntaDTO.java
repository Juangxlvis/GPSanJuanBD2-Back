package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record PresentacionPreguntaDTO (

        Integer presentacionPreguntaId,

        boolean respuestaCorrecta,


        PresentacionExamenDTO presentacionExamen,

        PreguntaDTO pregunta,

        RespuestaDTO respuesta
){
}
