package ufps.edu.co.rest.dto;

import java.io.Serializable;

public record PagoCheckoutPreviewDataDTO(
        Integer idAspirante,
        Integer idPersona,
        String programa,
        String periodo,
        String nombres,
        String apellidos,
        String numerodocumento,
        String facultad,
        String tipo) implements Serializable {
}