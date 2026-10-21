package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;import org.springframework.dao.DataIntegrityViolationException;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.ProgramaErrorCode;
import ufps.edu.co.maps.specific.ProgramaMap;
import ufps.edu.co.persistence.entities.CargoEntity;
import ufps.edu.co.persistence.entities.CriterioevaluacionEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoprogramaEntity;
import ufps.edu.co.persistence.repositories.CargoRepository;
import ufps.edu.co.persistence.repositories.CriterioevaluacionRepository;
import ufps.edu.co.persistence.repositories.DocumentosrequisitoprogramaRepository;
import ufps.edu.co.persistence.repositories.ProgramaRepository;
import ufps.edu.co.records.input.entity.ProgramaInput.PROGRAMA_DELETE;
import ufps.edu.co.records.input.entity.ProgramaInput.PROGRAMA_UPDATE;
import ufps.edu.co.records.output.entity.ProgramaDependenciasOutput;
import ufps.edu.co.rest.dto.CargoDTO;
import ufps.edu.co.rest.dto.ProgramaDTO;
import ufps.edu.co.rest.services.AdministrativoService;
import ufps.edu.co.rest.services.CargoService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstudiantesService;
import ufps.edu.co.rest.services.ProgramaService;
import ufps.edu.co.services.core.crud.CohorteCoreService;
import ufps.edu.co.services.core.crud.ProgramaCoreService;

/**
 * D-05 — Eliminar programa: conflictos con código propio, cascada de lo que le
 * pertenece y red de seguridad ante llaves no cubiertas.
 */
@ExtendWith(MockitoExtension.class)
class ProgramaEliminarTest {

    private static final Integer ID = 18;

    @Mock private ProgramaService service;
    @Mock private ProgramaMap map;
    @Mock private CohorteCoreService cohorteProcessor;
    @Mock private CohorteService cohorteService;
    @Mock private EstudiantesService estudiantesService;
    @Mock private CargoService cargoService;
    @Mock private AdministrativoService administrativoService;
    @Mock private ProgramaRepository programaRepository;
    @Mock private CargoRepository cargoRepository;
    @Mock private CriterioevaluacionRepository criterioevaluacionRepository;
    @Mock private DocumentosrequisitoprogramaRepository documentosrequisitoprogramaRepository;

    @InjectMocks private ProgramaCoreService processor;

    private ProgramaDTO programa(String nombre, Integer directorId) {
        return ProgramaDTO.builder().id(ID).nombre(nombre).directorId(directorId).build();
    }

    /** Programa sin cohortes, sin estudiantes, sin director y sin administrativos. */
    private void programaLibre() {
        when(service.findById(ID)).thenReturn(programa("Libre", null));
        when(estudiantesService.countByProgramaId(ID)).thenReturn(0L);
        when(cohorteService.countByIdPrograma(ID)).thenReturn(0L);
        when(cargoService.findByIdPrograma(ID)).thenReturn(List.of());
        when(administrativoService.countByIdCargoIn(anyCollection())).thenReturn(0L);
    }

    // ─── T1: existencia y parámetro ──────────────────────────────────────────

    @Test
    void idNulo_lanzaParametroRequerido() {
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new PROGRAMA_DELETE(null)));

        assertEquals(ProgramaErrorCode.PROGRAMA_PARAMETRO_REQUERIDO, ex.getCode());
        verify(service, never()).deleteById(any());
    }

    @Test
    void idInexistente_lanzaNotFound() {
        when(service.findById(999999)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new PROGRAMA_DELETE(999999)));

        assertEquals(ProgramaErrorCode.PROGRAMA_NOT_FOUND, ex.getCode());
        verify(service, never()).deleteById(any());
    }

    // ─── T2: conflictos, uno por tipo y con prioridad ────────────────────────

    @Test
    void soloCohortes_lanzaConflictosDeCohortes() {
        when(service.findById(ID)).thenReturn(programa("Con cohortes", null));
        when(estudiantesService.countByProgramaId(ID)).thenReturn(0L);
        when(cohorteService.countByIdPrograma(ID)).thenReturn(2L);
        when(cargoService.findByIdPrograma(ID)).thenReturn(List.of());
        when(administrativoService.countByIdCargoIn(anyCollection())).thenReturn(0L);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new PROGRAMA_DELETE(ID)));

        assertEquals(ProgramaErrorCode.PROGRAMA_CON_COHORTES_CONFLICT, ex.getCode());
        ProgramaDependenciasOutput dep = (ProgramaDependenciasOutput) ex.getParam();
        assertEquals(List.of("PROGRAMA_CON_COHORTES_CONFLICT"), dep.conflictos());
        assertEquals(2L, dep.cohortes());
        verify(service, never()).deleteById(any());
    }

    @Test
    void estudiantesYCohortes_priorizaEstudiantesYTraeLosDos() {
        when(service.findById(ID)).thenReturn(programa("Con todo", null));
        when(estudiantesService.countByProgramaId(ID)).thenReturn(3L);
        when(cohorteService.countByIdPrograma(ID)).thenReturn(2L);
        when(cargoService.findByIdPrograma(ID)).thenReturn(List.of());
        when(administrativoService.countByIdCargoIn(anyCollection())).thenReturn(0L);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new PROGRAMA_DELETE(ID)));

        assertEquals(ProgramaErrorCode.PROGRAMA_CON_ESTUDIANTES_CONFLICT, ex.getCode());
        ProgramaDependenciasOutput dep = (ProgramaDependenciasOutput) ex.getParam();
        assertEquals(List.of("PROGRAMA_CON_ESTUDIANTES_CONFLICT", "PROGRAMA_CON_COHORTES_CONFLICT"),
                dep.conflictos());
        verify(service, never()).deleteById(any());
    }

    @Test
    void administrativoEnUnCargoDelPrograma_lanzaConflictoDeDirector() {
        when(service.findById(ID)).thenReturn(programa("Con administrativo", null));
        when(estudiantesService.countByProgramaId(ID)).thenReturn(0L);
        when(cohorteService.countByIdPrograma(ID)).thenReturn(0L);
        when(cargoService.findByIdPrograma(ID)).thenReturn(List.of(CargoDTO.builder().id(26).build()));
        when(administrativoService.countByIdCargoIn(anyCollection())).thenReturn(1L);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new PROGRAMA_DELETE(ID)));

        assertEquals(ProgramaErrorCode.PROGRAMA_CON_DIRECTOR_CONFLICT, ex.getCode());
        ProgramaDependenciasOutput dep = (ProgramaDependenciasOutput) ex.getParam();
        assertEquals(1L, dep.administrativos());
        verify(service, never()).deleteById(any());
    }

    @Test
    void directorIdNoNuloSinAdministrativos_lanzaConflictoDeDirector() {
        when(service.findById(ID)).thenReturn(programa("Con director_id", 7));
        when(estudiantesService.countByProgramaId(ID)).thenReturn(0L);
        when(cohorteService.countByIdPrograma(ID)).thenReturn(0L);
        when(cargoService.findByIdPrograma(ID)).thenReturn(List.of());
        when(administrativoService.countByIdCargoIn(anyCollection())).thenReturn(0L);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new PROGRAMA_DELETE(ID)));

        assertEquals(ProgramaErrorCode.PROGRAMA_CON_DIRECTOR_CONFLICT, ex.getCode());
        ProgramaDependenciasOutput dep = (ProgramaDependenciasOutput) ex.getParam();
        assertTrue(dep.directorAsignado());
        verify(service, never()).deleteById(any());
    }

    @Test
    void sinDependencias_noLanzaConflicto() {
        programaLibre();

        processor.deleteById(new PROGRAMA_DELETE(ID));

        verify(service).deleteById(ID);
    }

    // ─── T3: cascada y red de seguridad ──────────────────────────────────────

    @Test
    @DisplayName("Borra documentos, criterios, cargos y el programa, en ese orden")
    void borraLosHijosEnOrdenYDespuesElPrograma() {
        programaLibre();

        List<DocumentosrequisitoprogramaEntity> documentos = List.of(
                DocumentosrequisitoprogramaEntity.builder().id(1).urlformato("s3://a.pdf").build(),
                DocumentosrequisitoprogramaEntity.builder().id(2).build(),
                DocumentosrequisitoprogramaEntity.builder().id(3).build());
        List<CriterioevaluacionEntity> criterios = List.of(
                CriterioevaluacionEntity.builder().id(11).build(),
                CriterioevaluacionEntity.builder().id(12).build());
        List<CargoEntity> cargos = List.of(CargoEntity.builder().id(26).build());

        when(documentosrequisitoprogramaRepository.findByIdPrograma(ID)).thenReturn(documentos);
        when(criterioevaluacionRepository.findByIdPrograma(ID)).thenReturn(criterios);
        when(cargoRepository.findByIdPrograma(ID)).thenReturn(cargos);

        processor.deleteById(new PROGRAMA_DELETE(ID));

        InOrder orden = inOrder(documentosrequisitoprogramaRepository, criterioevaluacionRepository,
                cargoRepository, service, programaRepository);
        orden.verify(documentosrequisitoprogramaRepository).deleteAll(documentos);
        orden.verify(criterioevaluacionRepository).deleteAll(criterios);
        orden.verify(cargoRepository).deleteAll(cargos);
        orden.verify(programaRepository).flush();   // flush de los hijos
        orden.verify(service).deleteById(ID);
        orden.verify(programaRepository).flush();   // DELETE del programa
    }

    @Test
    void programaLibreSinHijos_soloSeBorraElPrograma() {
        programaLibre();

        processor.deleteById(new PROGRAMA_DELETE(ID));

        verify(service).deleteById(ID);
        verify(documentosrequisitoprogramaRepository, never()).deleteAll(any());
        verify(criterioevaluacionRepository, never()).deleteAll(any());
        verify(cargoRepository, never()).deleteAll(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void unaLlaveNoCubierta_enElFlush_seConvierteEnReferenciadoConflict() {        programaLibre();
        doThrow(new DataIntegrityViolationException("fallo",
                new SQLException("Cannot delete or update a parent row: a foreign key constraint fails "
                        + "(`railway`.`cohorte`, CONSTRAINT `cohorte_ibfk_7` FOREIGN KEY (`id_programa`) "
                        + "REFERENCES `programas` (`id`))", "23000", 1451)))
                .when(programaRepository).flush();

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new PROGRAMA_DELETE(ID)));

        assertEquals(ProgramaErrorCode.PROGRAMA_REFERENCIADO_CONFLICT, ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals(ID, param.get("idPrograma"));
        assertEquals("cohorte_ibfk_7", param.get("restriccion"));
    }

    // ─── director_id: se conserva al editar ──────────────────────────────────

    @Test
    @DisplayName("update conserva director_id cuando el body no lo trae")
    void updateConservaDirectorId() {
        ProgramaDTO existente = programa("Con director", 7);
        ProgramaDTO entrante = ProgramaDTO.builder().id(ID).nombre("Editado").build();
        when(service.findById(ID)).thenReturn(existente);
        when(map.toDto(any())).thenReturn(entrante);
        when(map.toOutput(any())).thenReturn(null);

        processor.update(new PROGRAMA_UPDATE(ID, null, "Editado", 4, null, null, null, null, null, null, null, null,
                1, 1, 1, 1, 1));

        ArgumentCaptor<ProgramaDTO> captor = ArgumentCaptor.forClass(ProgramaDTO.class);
        verify(service).update(eq(ID), captor.capture());
        assertEquals(7, captor.getValue().getDirectorId());
    }
}
