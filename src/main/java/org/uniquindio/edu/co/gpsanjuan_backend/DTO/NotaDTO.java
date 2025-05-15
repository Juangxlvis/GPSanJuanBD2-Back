package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record NotaDTO(
        Integer notaId,
        Float valor,

        GrupoDTO grupo,

        AlumnoDTO alumnoDTO
) {

}
