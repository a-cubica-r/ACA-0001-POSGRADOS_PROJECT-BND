package ufps.edu.co.rest.dto;

import java.io.Serializable;

public record AspiranteCheckoutDTO(
        Integer id,
        Integer idPersona,
        Integer idCohorte,
        String nombres,
        String apellidos,
        String correo,
        String celular,
        String telefono,
        String numerodocumento,
        String tipoDocumento) implements Serializable {
}