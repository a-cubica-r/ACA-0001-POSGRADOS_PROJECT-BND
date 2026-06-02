package ufps.edu.co.rest.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstudiantesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer   id;
    private String    apellido;
    private String    apellido2;
    private String    cedula;
    private String    codigo;
    private String    email;
    private Boolean   esposgrado;
    private LocalDate fechalngreso;
    private LocalDate fechanacimiento;
    private String    migrado;
    private String    moodleld;
    private String    nombre;
    private String    nombre2;
    private String    telefono;
    private Integer   cohorteId;
    private Integer   estadoEstudianteId;
    private Integer   pensumId;
    private Integer   programaId;
    private Integer   usuarioId;
}
