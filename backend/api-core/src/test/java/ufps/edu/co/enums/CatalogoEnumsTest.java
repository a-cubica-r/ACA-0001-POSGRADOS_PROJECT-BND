package ufps.edu.co.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SC-05 - Verifica que cada enum conserve exactamente el texto que antes estaba
 * hardcodeado y que existe en la base de datos (copia de la BD tomada el 2026-10-12).
 */
class CatalogoEnumsTest {

    /** Pares entidad|tipo de la tabla {@code estado}. */
    private static final Set<String> ESTADOS_BD = Set.of(
            "administrativo|ACTIVO", "administrativo|INACTIVO",
            "aspirante|NO CONFIRMADO", "aspirante|APROBADO", "aspirante|MATRICULADO", "aspirante|EN PROCESO",
            "aspirante|SUSPENDIDO", "aspirante|ARCHIVADO", "aspirante|CANCELADO", "aspirante|INSCRITO",
            "aspirante|VALIDADO_POR_CALIFICAR", "aspirante|VALIDADO_EN_PROGRESO", "aspirante|VALIDADO_CALIFICADO",
            "aspirante|ADMITIDO", "aspirante|PAZ Y SALVO", "aspirante|POR LEGALIZAR", "aspirante|LEGALIZADO",
            "cohorte|CERRADA", "cohorte|ABIERTA",
            "documento|APROBADO", "documento|RECHAZADO", "documento|PENDIENTE",
            "entrevista|CONFIRMADA", "entrevista|PENDIENTE DE CONFIRMACION", "entrevista|SOLICITUD DE CAMBIO",
            "entrevista|COMPLETADA", "entrevista|CANCELADA",
            "global|EN ESPERA", "global|EN PROGRESO", "global|FINALIZADO",
            "pago|REALIZADO", "pago|PENDIENTE",
            "pagoinscripcion|RECHAZADO", "pagoinscripcion|EN CURSO", "pagoinscripcion|VENCIDO",
            "pagoinscripcion|COMPLETADO",
            "pagomatricula|RECHAZADO", "pagomatricula|EN CURSO", "pagomatricula|VENCIDO", "pagomatricula|COMPLETADO",
            "prueba|PENDIENTE DE CONFIRMACION", "prueba|SOLICITUD DE CAMBIO", "prueba|COMPLETADA",
            "prueba|CANCELADA", "prueba|CONFIRMADA",
            "semestre|PROGRAMADO", "semestre|EN CURSO", "semestre|FINALIZADO");

    private static List<String> valores(ValorCatalogo[] items) {
        return Arrays.stream(items).map(ValorCatalogo::getValor).toList();
    }

    // ─── Estados (tabla estado) ────────────────────────────────────────────────

    @Test
    void estadosCatalogo_todosExistenEnLaTablaEstado() {
        List<EstadoCatalogo> estados = Stream.of(
                EstadoAdministrativo.values(), EstadoAspirante.values(), EstadoCohorte.values(),
                EstadoSemestre.values(), EstadoEntrevista.values(), EstadoPrueba.values(), EstadoPago.values(),
                EstadoPagoInscripcion.values(), EstadoPagoMatricula.values())
                .flatMap(Arrays::stream)
                .map(EstadoCatalogo.class::cast)
                .toList();

        for (EstadoCatalogo estado : estados) {
            String par = estado.getEntidad().getValor() + "|" + estado.getValor();
            assertTrue(ESTADOS_BD.contains(par), "No existe en la tabla estado: " + par);
        }
    }

    @Test
    void estadoEstudiante_noExisteEnBd_seDocumentaComoAusente() {
        // EstudiantesCoreService ya buscaba ("ACTIVO", "estudiante") y aceptaba null.
        assertEquals("estudiante", EstadoEstudiante.ACTIVO.getEntidad().getValor());
        assertFalse(ESTADOS_BD.contains("estudiante|ACTIVO"));
    }

    @Test
    void estadosConEspacios_conservanElTextoOriginal() {
        assertEquals("NO CONFIRMADO", EstadoAspirante.NO_CONFIRMADO.getValor());
        assertEquals("PAZ Y SALVO", EstadoAspirante.PAZ_Y_SALVO.getValor());
        assertEquals("POR LEGALIZAR", EstadoAspirante.POR_LEGALIZAR.getValor());
        assertEquals("EN PROCESO", EstadoAspirante.EN_PROCESO.getValor());
        assertEquals("EN CURSO", EstadoPagoInscripcion.EN_CURSO.getValor());
        assertEquals("EN CURSO", EstadoPagoMatricula.EN_CURSO.getValor());
        assertEquals("EN CURSO", EstadoSemestre.EN_CURSO.getValor());
        assertEquals("PENDIENTE DE CONFIRMACION", EstadoEntrevista.PENDIENTE_DE_CONFIRMACION.getValor());
        assertEquals("SOLICITUD DE CAMBIO", EstadoPrueba.SOLICITUD_DE_CAMBIO.getValor());
    }

    @Test
    void entidadEstado_coincideConColumnaEntidad() {
        Set<String> entidadesBd = ESTADOS_BD.stream().map(par -> par.split("\\|")[0]).collect(Collectors.toSet());
        for (EntidadEstado entidad : EntidadEstado.values()) {
            if (entidad == EntidadEstado.ESTUDIANTE) {
                continue;
            }
            assertTrue(entidadesBd.contains(entidad.getValor()), "Entidad no existe en BD: " + entidad.getValor());
        }
    }

    // ─── Otros catálogos ───────────────────────────────────────────────────────

    @Test
    void estadoDocumento_coincideConTablaEstadodocumento() {
        assertEquals(List.of("PENDIENTE", "RADICADO", "APROBADO", "RECHAZADO", "EN REVISION", "SUBSANADO"),
                valores(EstadoDocumento.values()));
    }

    @Test
    void conceptoPago_coincideConTablaPagoconcepto() {
        assertEquals(List.of("INSCRIPCION", "MATRICULA", "OTRO"), valores(ConceptoPago.values()));
    }

    @Test
    void tipoRegistro_coincideConTablaTiporegistro_incluyendoIds() {
        assertEquals("UNICO", TipoRegistro.UNICO.getValor());
        assertEquals(1, TipoRegistro.UNICO.getId());
        assertEquals("ESTANDAR", TipoRegistro.ESTANDAR.getValor());
        assertEquals(2, TipoRegistro.ESTANDAR.getId());
    }

    @Test
    void modalidad_coincideConTablaModalidad() {
        assertEquals(List.of("PRESENCIAL", "VIRTUAL", "HIBRIDA"), valores(Modalidad.values()));
    }

    @Test
    void tipoPlazo_coincideConTablaTipoplazo() {
        assertEquals(List.of("CORRECCION", "ENTREVISTA", "DOCUMENTACION", "INSCRIPCION", "MATRICULA", "ADMISION",
                "PAGO"), valores(TipoPlazo.values()));
    }

    @Test
    void rol_valorCoincideConTablaRoles() {
        assertEquals(List.of("Super Administrador", "Posgrados", "Decano de Facultad", "Director de Programa",
                "Aspirante"), valores(Rol.values()));
        assertEquals("SUPER_ADMINISTRADOR", Rol.SUPER_ADMINISTRADOR.getCodigo());
        assertEquals("DIRECTOR_DE_PROGRAMA", Rol.DIRECTOR_DE_PROGRAMA.getCodigo());
    }

    @Test
    void valoresDeSalida_conservanElTextoQueRecibeElFrontend() {
        assertEquals(List.of("completado", "en progreso", "pendiente"), valores(EstadoPaso.values()));
        assertEquals(List.of("validados", "en progreso", "pendiente"), valores(EstadoGeneralDocumentos.values()));
        assertEquals(List.of("ASPIRANTE", "DIRECTOR"), valores(ActorCancelacion.values()));
        assertEquals(1, ZonaResidencia.URBANA.getId());
        assertEquals("Urbana", ZonaResidencia.URBANA.getValor());
        assertEquals(2, ZonaResidencia.RURAL.getId());
        assertEquals("Rural", ZonaResidencia.RURAL.getValor());
    }

    @Test
    void prefijosValorGlobal_coincidenConClavesDeValoresglobales() {
        assertEquals("TAMANO_MAXIMO_ARCHIVOS", PrefijoValorGlobal.TAMANO_MAXIMO_ARCHIVOS.getValor());
        assertEquals("SALARIO_MINIMO", PrefijoValorGlobal.SALARIO_MINIMO.getValor());
        assertEquals("VALOR_INSCRIPCION", PrefijoValorGlobal.VALOR_INSCRIPCION.getValor());
        // El orden importa: antes se devolvía List.of("TAMANO_MAXIMO", "TAMANO_MAXIMO_ARCHIVOS").
        assertEquals(List.of("TAMANO_MAXIMO", "TAMANO_MAXIMO_ARCHIVOS"), PrefijoValorGlobal.aliasesTamanoMaximo());
        assertEquals(List.of("MB", "SMMLV"), valores(UnidadValorGlobal.values()));
    }

    // ─── Comportamiento de comparación ─────────────────────────────────────────

    @Test
    void matches_ignoraMayusculasIgualQueEqualsIgnoreCase() {
        assertTrue(EstadoCohorte.ABIERTA.matches("ABIERTA"));
        assertTrue(EstadoCohorte.ABIERTA.matches("abierta"));
        assertTrue(EstadoAspirante.PAZ_Y_SALVO.matches("Paz y Salvo"));
        assertFalse(EstadoCohorte.ABIERTA.matches("CERRADA"));
        assertFalse(EstadoCohorte.ABIERTA.matches(null));
        // equalsIgnoreCase no recorta espacios; matches tampoco.
        assertFalse(EstadoCohorte.ABIERTA.matches(" ABIERTA"));
    }

    @Test
    void fromValor_encuentraIgnorandoMayusculas_yVacioSiNoExiste() {
        assertEquals(EstadoAspirante.POR_LEGALIZAR,
                ValorCatalogo.fromValor(EstadoAspirante.class, "por legalizar").orElseThrow());
        assertTrue(ValorCatalogo.fromValor(EstadoAspirante.class, "INEXISTENTE").isEmpty());
        assertTrue(ValorCatalogo.fromValor(EstadoAspirante.class, null).isEmpty());
    }

    @Test
    void valores_devuelveTextosEnElMismoOrden() {
        assertEquals(List.of("POR LEGALIZAR", "LEGALIZADO", "ADMITIDO"), ValorCatalogo.valores(
                EstadoAspirante.POR_LEGALIZAR, EstadoAspirante.LEGALIZADO, EstadoAspirante.ADMITIDO));
    }

    @Test
    void estadoAprobacionWompi_mismoResultadoQueLaComparacionAnterior() {
        for (String aprobado : List.of("APPROVED", "approved", " Approval ", "APPROVAL_SUCCESS", "SUCCESS", "PAID",
                "payed")) {
            assertTrue(EstadoAprobacionWompi.esAprobado(aprobado), aprobado);
        }
        for (String noAprobado : List.of("DECLINED", "ERROR", "VOIDED", "PENDING", "")) {
            assertFalse(EstadoAprobacionWompi.esAprobado(noAprobado), noAprobado);
        }
    }
}
