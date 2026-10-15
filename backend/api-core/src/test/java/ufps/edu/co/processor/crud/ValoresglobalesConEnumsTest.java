package ufps.edu.co.processor.crud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.enums.PrefijoValorGlobal;
import ufps.edu.co.maps.specific.ValoresglobalesMap;
import ufps.edu.co.rest.dto.ValoresglobalesDTO;
import ufps.edu.co.rest.services.ValoresglobalesService;
import ufps.edu.co.services.core.crud.ValoresglobalesCoreService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SC-05 - Los prefijos y unidades de valores globales pasaron a enums;
 * se verifica que las claves y valores generados no cambiaron.
 */
@ExtendWith(MockitoExtension.class)
class ValoresglobalesConEnumsTest {

    private static final int YEAR = LocalDate.now().getYear();

    @Mock private ValoresglobalesService valoresglobalesService;
    @Spy private ValoresglobalesMap valoresglobalesMap = new ValoresglobalesMap();

    @InjectMocks private ValoresglobalesCoreService processor;

    private static ValoresglobalesDTO valor(String clave, String valor) {
        return ValoresglobalesDTO.builder().clave(clave).valor(valor).build();
    }

    @Test
    void findCurrentByPrefix_tamanoMaximo_aceptaElPrefijoAntiguoComoAlias() {
        when(valoresglobalesService.findAll()).thenReturn(List.of(
                valor("TAMANO_MAXIMO_" + YEAR + "_1", "5MB"),
                valor("SALARIO_MINIMO_" + YEAR + "_1", "1.423.500")));

        ValoresglobalesDTO actual = processor.findCurrentByPrefix(PrefijoValorGlobal.TAMANO_MAXIMO_ARCHIVOS);

        assertEquals("TAMANO_MAXIMO_" + YEAR + "_1", actual.getClave());
    }

    @Test
    void findCurrentByPrefix_conEnumYConTexto_danElMismoResultado() {
        when(valoresglobalesService.findAll()).thenReturn(List.of(
                valor("SALARIO_MINIMO_" + YEAR + "_1", "1.400.000"),
                valor("SALARIO_MINIMO_" + YEAR + "_2", "1.423.500")));

        assertEquals(processor.findCurrentByPrefix("SALARIO_MINIMO").getClave(),
                processor.findCurrentByPrefix(PrefijoValorGlobal.SALARIO_MINIMO).getClave());
        assertEquals(1423500L, processor.getCurrentSalaryMinimumPesos());
    }

    @Test
    void calcularValorInscripcion_usaSalarioMinimoPorMultiplicadorSmmlv() {
        when(valoresglobalesService.findAll()).thenReturn(List.of(
                valor("SALARIO_MINIMO_" + YEAR + "_1", "1.000.000"),
                valor("VALOR_INSCRIPCION_" + YEAR + "_1", "0,5SMMLV")));

        assertEquals(0, new BigDecimal("500000").compareTo(processor.calcularValorInscripcionPesos()));
    }

    @Test
    void createValorInscripcion_agregaSufijoSmmlv_yGeneraClaveConVersion() {
        when(valoresglobalesService.findAll()).thenReturn(List.of(valor("VALOR_INSCRIPCION_" + YEAR + "_1", "1SMMLV")));
        when(valoresglobalesService.create(any())).thenAnswer(inv -> inv.getArgument(0));

        ValoresglobalesDTO creado = processor.createValorInscripcion(valor("VALOR_INSCRIPCION", "2"));

        assertEquals("VALOR_INSCRIPCION_" + YEAR + "_2", creado.getClave());
        assertEquals("2SMMLV", creado.getValor());
    }

    @Test
    void createTamanoMaximoArchivos_agregaSufijoMb() {
        when(valoresglobalesService.findAll()).thenReturn(List.of());
        when(valoresglobalesService.create(any())).thenAnswer(inv -> inv.getArgument(0));

        ValoresglobalesDTO creado = processor.createTamanoMaximoArchivos(valor("TAMANO_MAXIMO_ARCHIVOS", "10"));

        assertEquals("TAMANO_MAXIMO_ARCHIVOS_" + YEAR + "_1", creado.getClave());
        assertEquals("10MB", creado.getValor());
    }

    @Test
    void createSalarioMinimo_noAgregaSufijo() {
        when(valoresglobalesService.findAll()).thenReturn(List.of());
        when(valoresglobalesService.create(any())).thenAnswer(inv -> inv.getArgument(0));

        ValoresglobalesDTO creado = processor.createSalarioMinimo(valor("SALARIO_MINIMO", "1.423.500"));

        assertEquals("SALARIO_MINIMO_" + YEAR + "_1", creado.getClave());
        assertEquals("1.423.500", creado.getValor());
    }

    @Test
    void parseSmmlvMultiplier_sigueAceptandoLaVarianteSmlmv() {
        assertEquals(0, new BigDecimal("1.5").compareTo(valoresglobalesMap.parseSmmlvMultiplier("1,5 SMLMV")));
        assertEquals(0, new BigDecimal("2").compareTo(valoresglobalesMap.parseSmmlvMultiplier("2SMMLV")));
    }
}
