package ufps.edu.co.records.output.entity;

import java.math.BigDecimal;

import lombok.Builder;
import ufps.edu.co.records.OutputResponse;

@Builder
public record AspiranteDatosOutput(
        String nombres,
        String apellidos,
        String celular,
        String correo,
        String documento,
        String tipodocumento,
        Boolean egresadoufps,
        String empresa,
        String experiencialaboral,
        BigDecimal promediopregrado,
        String titulopregrado,
        String titulosposgrados,
        String ubicaciontrabajo
) implements OutputResponse {}
