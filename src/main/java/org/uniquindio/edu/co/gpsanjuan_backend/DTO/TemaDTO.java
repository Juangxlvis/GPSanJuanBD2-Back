package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record TemaDTO (
        Long temaId,
        String titulo,

        String descripcion,

        List<ExamenDTO> examenes,

        List<PreguntaDTO> unidades
){
}
