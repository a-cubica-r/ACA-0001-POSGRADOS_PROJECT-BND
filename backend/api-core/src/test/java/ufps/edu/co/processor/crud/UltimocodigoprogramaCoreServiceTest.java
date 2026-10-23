package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.UltimocodigoprogramaErrorCode;
import ufps.edu.co.records.output.entity.UltimocodigoprogramaOutput;
import ufps.edu.co.rest.dto.UltimocodigoprogramaDTO;
import ufps.edu.co.rest.services.EstudiantesService;
import ufps.edu.co.rest.services.UltimocodigoprogramaService;
import ufps.edu.co.services.core.crud.UltimocodigoprogramaCoreService;

/**
 * D-00 — Consecutivo del código de estudiante: reserva, edición manual y
 * consulta derivada.
 */
@ExtendWith(MockitoExtension.class)
class UltimocodigoprogramaCoreServiceTest {

    private static final Integer ID_PROGRAMA = 2;
    private static final Integer ID_FILA = 9;

    @Mock
    private UltimocodigoprogramaService service;

    @Mock
    private EstudiantesService estudiantesService;

    @InjectMocks
    private UltimocodigoprogramaCoreService processor;

    private static UltimocodigoprogramaDTO fila(Integer codigo) {
        return UltimocodigoprogramaDTO.builder()
                .id(ID_FILA)
                .idPrograma(ID_PROGRAMA)
                .codigo(codigo)
                .nombrePrograma("Maestría B")
                .build();
    }

    // ─── reservarSiguiente ───────────────────────────────────────────────────

    @Test
    @DisplayName("Contador al día: devuelve el guardado y avanza en 1")
    void reservarSiguiente_contadorAlDia() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(5));
        when(estudiantesService.findUltimoCodigoPorPrograma(ID_PROGRAMA)).thenReturn("21020004");

        int siguiente = processor.reservarSiguiente(ID_PROGRAMA);

        assertEquals(5, siguiente);
        verify(service).update(eq(ID_FILA), guardadoCon(6));
    }

    @Test
    @DisplayName("Contador atrasado respecto al último estudiante: gana el estudiante")
    void reservarSiguiente_contadorAtrasado() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(3));
        when(estudiantesService.findUltimoCodigoPorPrograma(ID_PROGRAMA)).thenReturn("21020012");

        int siguiente = processor.reservarSiguiente(ID_PROGRAMA);

        assertEquals(13, siguiente);
        verify(service).update(eq(ID_FILA), guardadoCon(14));
    }

    @Test
    @DisplayName("Contador por delante del último estudiante: se respeta el contador")
    void reservarSiguiente_contadorPorDelante() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(500));
        when(estudiantesService.findUltimoCodigoPorPrograma(ID_PROGRAMA)).thenReturn("21020002");

        int siguiente = processor.reservarSiguiente(ID_PROGRAMA);

        assertEquals(500, siguiente);
        verify(service).update(eq(ID_FILA), guardadoCon(501));
    }

    @Test
    @DisplayName("Programa sin fila de consecutivo: la crea y arranca en el mínimo")
    void reservarSiguiente_sinFila_creaElConsecutivo() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(null);
        when(estudiantesService.findUltimoCodigoPorPrograma(ID_PROGRAMA)).thenReturn("21020012");

        int siguiente = processor.reservarSiguiente(ID_PROGRAMA);

        assertEquals(13, siguiente);
        ArgumentCaptor<UltimocodigoprogramaDTO> captor = ArgumentCaptor.forClass(UltimocodigoprogramaDTO.class);
        verify(service).create(captor.capture());
        assertEquals(ID_PROGRAMA, captor.getValue().getIdPrograma());
        assertEquals(14, captor.getValue().getCodigo());
        verify(service, never()).update(any(), any());
    }

    @Test
    @DisplayName("Consecutivo agotado (10000): falla y no guarda nada")
    void reservarSiguiente_agotado() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(10000));
        when(estudiantesService.findUltimoCodigoPorPrograma(ID_PROGRAMA)).thenReturn("21029999");

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.reservarSiguiente(ID_PROGRAMA));

        assertEquals(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_AGOTADO_CONFLICT, ex.getCode());
        verify(service, never()).update(any(), any());
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("Si el último estudiante llegó a 9999, el programa queda agotado aunque el contador esté atrás")
    void reservarSiguiente_ultimoEstudianteEnElLimite() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(5));
        when(estudiantesService.findUltimoCodigoPorPrograma(ID_PROGRAMA)).thenReturn("21029999");

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.reservarSiguiente(ID_PROGRAMA));

        assertEquals(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_AGOTADO_CONFLICT, ex.getCode());
        verify(service, never()).update(any(), any());
    }

    @Test
    @DisplayName("Se puede asignar el último secuencial válido (9999)")
    void reservarSiguiente_ultimoCodigoValido() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(9999));
        when(estudiantesService.findUltimoCodigoPorPrograma(ID_PROGRAMA)).thenReturn(null);

        int siguiente = processor.reservarSiguiente(ID_PROGRAMA);

        assertEquals(9999, siguiente);
        verify(service).update(eq(ID_FILA), guardadoCon(10000));
    }

    // ─── updateCodigoPorPrograma ─────────────────────────────────────────────

    @Test
    @DisplayName("Código nulo, negativo o mayor de 9999: CODIGO_INVALIDO y no guarda")
    void updateCodigo_invalido() {
        for (Integer invalido : new Integer[] { null, -1, 10000 }) {
            DomainException ex = assertThrows(DomainException.class,
                    () -> processor.updateCodigoPorPrograma(ID_PROGRAMA, invalido));
            assertEquals(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_CODIGO_INVALIDO, ex.getCode());
        }
        verify(service, never()).update(any(), any());
    }

    @Test
    @DisplayName("Programa sin fila de consecutivo: NOT_FOUND")
    void updateCodigo_programaSinFila() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.updateCodigoPorPrograma(ID_PROGRAMA, 20));

        assertEquals(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_NOT_FOUND, ex.getCode());
        verify(service, never()).update(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Código menor que el siguiente al último estudiante: conflicto con el mínimo")
    void updateCodigo_menorQueLosEstudiantes() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(5));
        when(estudiantesService.findUltimoCodigoPorProgramaSinBloqueo(ID_PROGRAMA)).thenReturn("21020012");

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.updateCodigoPorPrograma(ID_PROGRAMA, 3));

        assertEquals(UltimocodigoprogramaErrorCode.ULTIMOCODIGOPROGRAMA_CODIGO_MENOR_QUE_ESTUDIANTES_CONFLICT,
                ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals(ID_PROGRAMA, param.get("idPrograma"));
        assertEquals(13, param.get("codigoMinimo"));
        verify(service, never()).update(any(), any());
    }

    @Test
    @DisplayName("Código válido y por encima del mínimo: guarda y devuelve el registro")
    void updateCodigo_valido() {
        when(service.findByIdProgramaForUpdate(ID_PROGRAMA)).thenReturn(fila(5));
        when(estudiantesService.findUltimoCodigoPorProgramaSinBloqueo(ID_PROGRAMA)).thenReturn("21020012");
        when(service.update(eq(ID_FILA), any())).thenAnswer(invocation -> invocation.getArgument(1));

        UltimocodigoprogramaOutput output = processor.updateCodigoPorPrograma(ID_PROGRAMA, 20);

        assertNotNull(output);
        assertEquals(ID_FILA, output.id());
        assertEquals(ID_PROGRAMA, output.idPrograma());
        assertEquals("Maestría B", output.nombrePrograma());
        assertEquals(20, output.codigo());
        verify(service).update(eq(ID_FILA), guardadoCon(20));
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listall: una entrada por programa, con 0 cuando no hay consecutivo")
    void findAll_unaEntradaPorPrograma() {
        when(service.findAllConNombrePrograma()).thenReturn(List.of(
                UltimocodigoprogramaDTO.builder()
                        .id(null)
                        .idPrograma(1)
                        .codigo(null)
                        .nombrePrograma("Especialización A")
                        .build(),
                UltimocodigoprogramaDTO.builder()
                        .id(4)
                        .idPrograma(2)
                        .codigo(12)
                        .nombrePrograma("Maestría B")
                        .build()));

        List<UltimocodigoprogramaOutput> salida = processor.findAll();

        assertEquals(2, salida.size());
        assertEquals(1, salida.get(0).id());
        assertEquals(1, salida.get(0).idPrograma());
        assertEquals("Especialización A", salida.get(0).nombrePrograma());
        assertEquals(0, salida.get(0).codigo());
        assertEquals(4, salida.get(1).id());
        assertEquals(2, salida.get(1).idPrograma());
        assertEquals(12, salida.get(1).codigo());
    }

    private static UltimocodigoprogramaDTO guardadoCon(Integer codigo) {
        return argThat(dto -> dto != null && codigo.equals(dto.getCodigo()));
    }
}
