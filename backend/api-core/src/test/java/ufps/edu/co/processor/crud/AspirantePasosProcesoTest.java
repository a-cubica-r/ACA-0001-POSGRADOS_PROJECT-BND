package ufps.edu.co.processor.crud;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.records.output.entity.PasoProcesoOutput;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.services.core.crud.AspiranteCoreService;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SC-05 - El switch de pasos del proceso pasó de comparar textos a usar
 * {@code EstadoAspirante}. Estas pruebas fijan la tabla de resultados que
 * producía la versión con textos para cada estado del aspirante.
 */
@ExtendWith(MockitoExtension.class)
class AspirantePasosProcesoTest {

    private static final String C = "completado";
    private static final String E = "en progreso";
    private static final String P = "pendiente";

    @Mock private AspiranteService aspiranteService;

    @InjectMocks private AspiranteCoreService processor;

    static Stream<Arguments> estados() {
        return Stream.of(
                Arguments.of("NO CONFIRMADO", List.of(E, P, P, P, P, P)),
                Arguments.of("INSCRITO", List.of(C, E, P, P, P, P)),
                Arguments.of("PAZ Y SALVO", List.of(C, C, E, P, P, P)),
                Arguments.of("VALIDADO_POR_CALIFICAR", List.of(C, C, C, P, P, P)),
                Arguments.of("VALIDADO_EN_PROGRESO", List.of(C, C, C, E, P, P)),
                Arguments.of("VALIDADO_CALIFICADO", List.of(C, C, C, C, E, P)),
                Arguments.of("ADMITIDO", List.of(C, C, C, C, E, P)),
                Arguments.of("POR LEGALIZAR", List.of(C, C, C, C, C, E)),
                Arguments.of("LEGALIZADO", List.of(C, C, C, C, C, C)),
                // Antes se hacía toUpperCase() antes del switch: minúsculas daban el mismo resultado.
                Arguments.of("inscrito", List.of(C, E, P, P, P, P)),
                Arguments.of("por legalizar", List.of(C, C, C, C, C, E)),
                // Estados sin caso propio, desconocidos o nulos caían en "default".
                Arguments.of("CANCELADO", List.of(C, P, P, P, P, P)),
                Arguments.of("EN PROCESO", List.of(C, P, P, P, P, P)),
                Arguments.of("APROBADO", List.of(C, P, P, P, P, P)),
                Arguments.of("DESCONOCIDO", List.of(C, P, P, P, P, P)),
                Arguments.of(null, List.of(C, P, P, P, P, P)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("estados")
    void getPasosProceso_mismoResultadoQueAntes(String estadoTipo, List<String> esperado) {
        when(aspiranteService.findEstadoTipoById(1)).thenReturn(estadoTipo);

        List<PasoProcesoOutput> pasos = processor.getPasosProceso(1);

        assertEquals(esperado, pasos.stream().map(PasoProcesoOutput::status).toList());
        assertEquals(List.of("Inscripción", "Pago", "Documentos", "Calificación", "Resultado", "Legalización"),
                pasos.stream().map(PasoProcesoOutput::name).toList());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), pasos.stream().map(PasoProcesoOutput::id).toList());
    }
}
