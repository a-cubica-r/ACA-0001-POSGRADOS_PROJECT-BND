package ufps.edu.co.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReciboInscripcionInputDTO {

    private String fechaGeneracion;
    private String codigoRecibo;
    private String periodo;
    private String version;
    private String nit;
    private String programa;
    private String nombre;
    private String tipoPersona;
    private String documento;
    private String correo;
    private String tipoPago;
    private String fechaLimite;
    private String concepto;
    private String valor;
    private String total;
    private String convenioBancolombia;
    private String convenioBbva;
    private String barcodeDato;

    public ReciboDTO toReciboDTO() {
        return ReciboDTO.builder()
                .fechaGeneracion(fechaGeneracion)
                .codigoRecibo(codigoRecibo)
                .periodo(periodo)
                .version(version)
                .nit(nit)
                .programa(programa)
                .nombre(nombre)
                .tipoPersona(tipoPersona)
                .documento(documento)
                .correo(correo)
                .tipoPago(tipoPago)
                .fechaLimite(fechaLimite)
                .concepto(concepto)
                .valor(valor)
                .total(total)
                .convenioBancolombia(convenioBancolombia)
                .convenioBbva(convenioBbva)
                .barcodeDato(barcodeDato)
                .build();
    }
}