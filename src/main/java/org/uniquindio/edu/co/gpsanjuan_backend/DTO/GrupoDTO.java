package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record GrupoDTO(
        Integer grupoId,
        List<BloqueHorarioDTO> horarios,
        String jornada,
        String nombre,
        String periodo,

        List<NotaDTO> notas,

        CursoDTO curso,

        List<AlumnoDTO> alumnos,

        DocenteDTO docente

) {

}
