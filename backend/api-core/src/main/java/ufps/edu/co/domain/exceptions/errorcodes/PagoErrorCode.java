package ufps.edu.co.domain.exceptions.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ufps.edu.co.domain.exceptions.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum PagoErrorCode implements BaseErrorCode {
    PAGO_NOT_FOUND("PAGO_NOT_FOUND", "Pago no encontrado"),
    PAGO_ESTADO_NOT_FOUND("PAGO_ESTADO_NOT_FOUND", "Estado de pago no encontrado"),
    PAGO_CONCEPTO_NOT_FOUND("PAGO_CONCEPTO_NOT_FOUND", "Concepto de pago no encontrado"),
    PAGO_CONCEPTO_INVALIDO("PAGO_CONCEPTO_INVALIDO", "El pago no corresponde al concepto solicitado"),
    PAGO_NO_PERTENECE_AL_ASPIRANTE_FORBIDDEN("PAGO_NO_PERTENECE_AL_ASPIRANTE_FORBIDDEN", "El pago no pertenece al aspirante indicado"),
    ASPIRANTE_AUTENTICADO_NO_COINCIDE_FORBIDDEN("ASPIRANTE_AUTENTICADO_NO_COINCIDE_FORBIDDEN", "El usuario autenticado no corresponde al aspirante solicitado"),
    PAGO_YA_REALIZADO_CONFLICT("PAGO_YA_REALIZADO_CONFLICT", "El pago ya fue realizado"),
    VALOR_GLOBAL_NO_CONFIGURADO_NOT_FOUND("VALOR_GLOBAL_NO_CONFIGURADO_NOT_FOUND", "No existe configuración global para calcular el valor del pago"),
    VALOR_GLOBAL_FORMATO_INVALIDO("VALOR_GLOBAL_FORMATO_INVALIDO", "La configuración global del valor del pago tiene un formato inválido"),
    MATRICULA_MONTO_ELEGIDO_INVALIDO("MATRICULA_MONTO_ELEGIDO_INVALIDO", "El monto elegido para matrícula no cumple el mínimo permitido"),
    WOMPI_PAGO_NO_APROBADO_CONFLICT("WOMPI_PAGO_NO_APROBADO_CONFLICT", "El pago no fue aprobado por Wompi"),
    FACTURA_YA_EXISTE_CONFLICT("FACTURA_YA_EXISTE_CONFLICT", "Ya existe una factura para este recibo");

    private final String code;
    private final String defaultMessage;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }
}