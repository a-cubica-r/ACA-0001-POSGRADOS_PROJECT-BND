package ufps.edu.co.records.output.entity;

import java.time.LocalDate;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record EstudiantesOutput(
        Integer   id,
        String    apellido,
        String    apellido2,
        String    cedula,
        String    codigo,
        String    email,
        Boolean   esposgrado,
        LocalDate fechalngreso,
        LocalDate fechanacimiento,
        String    migrado,
        String    moodleld,
        String    nombre,
        String    nombre2,
        String    telefono,
        Integer   cohorteId,
        Integer   estadoEstudianteId,
        Integer   pensumId,
        Integer   programaId,
        Integer   usuarioId,
        ProgramaOutput programa
) implements OutputResponse {}
