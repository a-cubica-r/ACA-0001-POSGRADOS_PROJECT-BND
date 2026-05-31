package ufps.edu.co.DTO;

import lombok.Builder;
import lombok.Data;

/**
 * DTO con todos los datos necesarios para generar el recibo.
 * El controlador construye este objeto y lo pasa al servicio.
 */
@Data
@Builder
public class ReciboDTO {

    // ── Header ─────────────────────────────────────────────
    private String fechaGeneracion;    // "Sábado 30 de Mayo de 2026"
    private String codigoRecibo;       // "821111"
    private String periodo;            // "20261"
    private String version;            // "V. 1.0"
    private String nit;                // "Nit. 890500622-6"

    // ── Datos del aspirante ────────────────────────────────
    private String programa;           // "MAESTRÍA EN CIENCIAS COMPUTACIONALES"
    private String nombre;             // "ARENAS RAMOS ADOLFO ALEJANDRO"
    private String tipoPersona;        // "Aspirante"
    private String documento;          // "1152370421"
    private String correo;             // "adolfo.arenas@ufps.edu.co"

    // ── Datos del pago ─────────────────────────────────────
    private String tipoPago;           // "Inscripción a Posgrados"
    private String fechaLimite;        // "Domingo 31 de Mayo de 2026"
    private String concepto;           // "Derechos de inscripción posgrado"
    private String valor;              // "$150,000"
    private String total;              // "$150,000"

    // ── Bancos ─────────────────────────────────────────────
    private String convenioBancolombia; // "Convenio 61003"
    private String convenioBbva;        // "Convenio 4668"

    // ── Código de barras ───────────────────────────────────
    private String barcodeDato;        // "(415)7709998005938(8020)..."
}