package ufps.edu.co.records.input.entity;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ufps.edu.co.records.contracts.CreateType;
import ufps.edu.co.records.contracts.DeleteType;
import ufps.edu.co.records.contracts.FindType;
import ufps.edu.co.records.contracts.UpdateType;

public enum EstudiantesInput {
    ;

    public record ESTUDIANTE_CREATE(
            @NotBlank String    apellido,
                      String    apellido2,
            @NotBlank String    cedula,
                      String    codigo,
                      String    email,
                      Boolean   esposgrado,
                      LocalDate fechalngreso,
                      LocalDate fechanacimiento,
                      String    migrado,
                      String    moodleld,
            @NotBlank String    nombre,
                      String    nombre2,
                      String    telefono,
                      Integer   cohorteId,
                      Integer   estadoEstudianteId,
                      Integer   pensumId,
            @NotNull  Integer   programaId,
                      Integer   usuarioId
    ) implements CreateType {}

    public record ESTUDIANTE_UPDATE(
            @NotNull  Integer   id,
            @NotBlank String    apellido,
                      String    apellido2,
            @NotBlank String    cedula,
                      String    codigo,
                      String    email,
                      Boolean   esposgrado,
                      LocalDate fechalngreso,
                      LocalDate fechanacimiento,
                      String    migrado,
                      String    moodleld,
            @NotBlank String    nombre,
                      String    nombre2,
                      String    telefono,
                      Integer   cohorteId,
                      Integer   estadoEstudianteId,
                      Integer   pensumId,
            @NotNull  Integer   programaId,
                      Integer   usuarioId
    ) implements UpdateType {}

    public record ESTUDIANTE_FIND(
            @NotNull Integer id
    ) implements FindType {}

    public record ESTUDIANTE_DELETE(
            @NotNull Integer id
    ) implements DeleteType {}
}
