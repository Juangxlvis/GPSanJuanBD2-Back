package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.sql.Timestamp;
import java.util.List;

public record BloqueHorario(
        Integer bloqueId,
        String lugar,
        Timestamp horaInicio,

        Timestamp horaFin,

        List<GrupoDTO> horarios
) {
}
