package ufps.edu.co.processor.integracion;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.IntegracionTramitesErrorCode;
import ufps.edu.co.records.output.cases.SituacionAcademicaOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.EstudiantesDTO;
import ufps.edu.co.rest.dto.ProgramaDTO;
import ufps.edu.co.rest.dto.SemestreDTO;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.EstudiantesService;
import ufps.edu.co.rest.services.ProgramaService;
import ufps.edu.co.services.core.integracion.IntegracionTramitesCoreService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SC-03 - Contrato de GET /integracion/v1/estudiantes/{cedula} que consume
 * Trámites: campos que espera su cliente y códigos de error.
 */
@ExtendWith(MockitoExtension.class)
class IntegracionTramitesCoreServiceTest {

    @Mock private EstudiantesService estudiantesService;
    @Mock private ProgramaService programaService;
    @Mock private CohorteService cohorteService;
    @Mock private EstadoService estadoService;
    @InjectMocks private IntegracionTramitesCoreService service;

    @Test
    void estudianteExistente_devuelveElContrato() {
        EstudiantesDTO est = new EstudiantesDTO();
        est.setCedula("1090000001");
        est.setCodigo("11550007");
        est.setNombre("Laura");
        est.setNombre2("Marcela");
        est.setApellido("Pérez");
        est.setEmail("laura@example.com");
        est.setFechalngreso(LocalDate.of(2025, 2, 3));
        est.setCohorteId(34);
        est.setEstadoEstudianteId(1);

        ProgramaDTO programa = new ProgramaDTO();
        programa.setId(12);
        programa.setCodigo("1155");
        programa.setNombre("Maestría de ejemplo");
        programa.setCreditos(56);
        programa.setValormatricula(new BigDecimal("4500000"));

        SemestreDTO semestre = new SemestreDTO();
        semestre.setNombre("2025-1");
        CohorteDTO cohorte = new CohorteDTO();
        cohorte.setNombre("Cohorte 2025-1");
        cohorte.setSemestre(semestre);

        EstadoDTO estado = new EstadoDTO();
        estado.setTipo("ACTIVO");

        when(estudiantesService.findByCedula("1090000001")).thenReturn(est);
        // Como en producción: el DTO llega sin programaId y se consulta aparte.
        when(estudiantesService.findProgramaIdByCedula("1090000001")).thenReturn(12);
        when(programaService.findById(12)).thenReturn(programa);
        when(cohorteService.findById(34)).thenReturn(cohorte);
        when(estadoService.findById(1)).thenReturn(estado);

        SituacionAcademicaOutput out = service.consultar("1090000001");

        assertEquals("Laura Marcela", out.nombres());
        assertEquals("Pérez", out.apellidos());
        assertEquals("ACTIVO", out.estado());
        assertEquals("1155", out.programa().codigo());
        assertEquals(56, out.programa().creditosRequeridos());
        assertEquals(34, out.admision().cohorteId());
        assertEquals("2025-1", out.admision().semestre());
        assertEquals(LocalDate.of(2025, 2, 3), out.admision().fechaIngreso());
        assertEquals("COMPLETADO", out.matricula().estado());
        assertEquals(56, out.creditos().requeridos());
        assertNull(out.creditos().aprobados());
    }

    @Test
    void cedulaSinEstudiante_lanzaNotFound() {
        when(estudiantesService.findByCedula("1090000002")).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class, () -> service.consultar("1090000002"));
        assertEquals(IntegracionTramitesErrorCode.ESTUDIANTE_NOT_FOUND, ex.getCode());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "1234", "12345678901234567", "10900A0001", "1090 0001" })
    void cedulaInvalida_lanzaCedulaInvalidaSinConsultar(String cedula) {
        DomainException ex = assertThrows(DomainException.class, () -> service.consultar(cedula));
        assertEquals(IntegracionTramitesErrorCode.CEDULA_INVALIDA, ex.getCode());
        verifyNoInteractions(estudiantesService);
    }
}
