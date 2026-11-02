package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.CohorteErrorCode;
import ufps.edu.co.maps.specific.CohorteMap;
import ufps.edu.co.persistence.entities.CriteriocohorteEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoconsejocohorteEntity;
import ufps.edu.co.persistence.entities.DocumentosrequisitoprogramacohorteEntity;
import ufps.edu.co.persistence.repositories.CohorteRepository;
import ufps.edu.co.persistence.repositories.CriteriocohorteRepository;
import ufps.edu.co.persistence.repositories.DocumentoRepository;
import ufps.edu.co.persistence.repositories.DocumentosrequisitoconsejocohorteRepository;
import ufps.edu.co.persistence.repositories.DocumentosrequisitoprogramacohorteRepository;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_DELETE;
import ufps.edu.co.records.output.entity.CohorteDependenciasOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.services.AdmitidoService;
import ufps.edu.co.rest.services.AspiranteService;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.PlazoService;
import ufps.edu.co.rest.services.PruebaService;
import ufps.edu.co.services.core.crud.CohorteCoreService;

/**
 * D-09 T3 — Eliminar cohorte: conflicto con código propio según el uso,
 * cascada de documentos y criterios, plazos solo si nadie más los usa y red de
 * seguridad ante llaves no cubiertas.
 */
@ExtendWith(MockitoExtension.class)
class CohorteEliminarTest {

    private static final Integer ID = 6;
    private static final Integer PLAZO_DOC = 27;
    private static final Integer PLAZO_INSC = 28;
    private static final Integer PLAZO_PAGO = 29;

    @Mock private CohorteService service;
    @Mock private CohorteMap map;

    @Mock private CohorteRepository cohorteRepository;
    @Mock private CriteriocohorteRepository criteriocohorteRepository;
    @Mock private DocumentosrequisitoconsejocohorteRepository documentosrequisitoconsejocohorteRepository;
    @Mock private DocumentosrequisitoprogramacohorteRepository documentosrequisitoprogramacohorteRepository;
    @Mock private DocumentoRepository documentoRepository;

    @Mock private AspiranteService aspiranteService;
    @Mock private AdmitidoService admitidoService;
    @Mock private PruebaService pruebaService;
    @Mock private PlazoService plazoService;

    @InjectMocks private CohorteCoreService processor;

    private static CohorteDTO cohorte(Integer doc, Integer insc, Integer pago) {
        return CohorteDTO.builder()
                .id(ID)
                .idPlazodocumentacion(doc)
                .idPlazoinscripcion(insc)
                .idPlazopago(pago)
                .build();
    }

    private static DataIntegrityViolationException enUso() {
        return new DataIntegrityViolationException("fallo de integridad",
                new SQLException("Cannot delete or update a parent row: a foreign key constraint fails "
                        + "(`railway`.`criteriocohorte`, CONSTRAINT `criteriocohorte_ibfk_1` "
                        + "FOREIGN KEY (`id_cohorte`) REFERENCES `cohorte` (`id`) ON DELETE RESTRICT)",
                        "23000", 1451));
    }

    /** Cohorte sin aspirantes, admitidos ni pruebas y con los tres plazos. */
    private void cohorteSinUso() {
        when(service.findById(ID)).thenReturn(cohorte(PLAZO_DOC, PLAZO_INSC, PLAZO_PAGO));
        when(aspiranteService.countByCohorte(ID)).thenReturn(0L);
        when(admitidoService.countByCohorte(ID)).thenReturn(0L);
        when(pruebaService.countByCohorte(ID)).thenReturn(0L);
        when(criteriocohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(documentosrequisitoconsejocohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(documentosrequisitoprogramacohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(cohorteRepository.countByPlazo(anyInt())).thenReturn(0L);
        when(documentoRepository.countByIdPlazo(anyInt())).thenReturn(0L);
    }

    // ─── Parámetro y existencia ──────────────────────────────────────────────

    @Test
    @DisplayName("id nulo → COHORTE_PARAMETRO_REQUERIDO y no se toca nada")
    void idNulo() {
        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new COHORTE_DELETE(null)));

        assertEquals(CohorteErrorCode.COHORTE_PARAMETRO_REQUERIDO, ex.getCode());
        verifyNoInteractions(service, aspiranteService, admitidoService, pruebaService, plazoService);
    }

    @Test
    @DisplayName("Cohorte inexistente → COHORTE_NOT_FOUND y no se borra nada")
    void cohorteInexistente() {
        when(service.findById(ID)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new COHORTE_DELETE(ID)));

        assertEquals(CohorteErrorCode.COHORTE_NOT_FOUND, ex.getCode());
        verify(service, never()).deleteById(any());
        verifyNoInteractions(plazoService);
    }

    // ─── Conflictos de uso ───────────────────────────────────────────────────

    @Test
    @DisplayName("Con aspirantes → COHORTE_CON_ASPIRANTES_CONFLICT y no se borra nada")
    void conAspirantes() {
        when(service.findById(ID)).thenReturn(cohorte(PLAZO_DOC, PLAZO_INSC, PLAZO_PAGO));
        when(aspiranteService.countByCohorte(ID)).thenReturn(3L);
        when(admitidoService.countByCohorte(ID)).thenReturn(0L);
        when(pruebaService.countByCohorte(ID)).thenReturn(0L);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new COHORTE_DELETE(ID)));

        assertEquals(CohorteErrorCode.COHORTE_CON_ASPIRANTES_CONFLICT, ex.getCode());
        CohorteDependenciasOutput dependencias = (CohorteDependenciasOutput) ex.getParam();
        assertEquals(3L, dependencias.aspirantes());
        assertEquals(List.of("COHORTE_CON_ASPIRANTES_CONFLICT"), dependencias.conflictos());
        verify(service, never()).deleteById(any());
        verifyNoInteractions(plazoService);
    }

    @Test
    @DisplayName("Con aspirantes y pruebas → responde aspirantes y detalla ambos en orden")
    void conAspirantesYPruebas() {
        when(service.findById(ID)).thenReturn(cohorte(PLAZO_DOC, PLAZO_INSC, PLAZO_PAGO));
        when(aspiranteService.countByCohorte(ID)).thenReturn(2L);
        when(admitidoService.countByCohorte(ID)).thenReturn(0L);
        when(pruebaService.countByCohorte(ID)).thenReturn(4L);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new COHORTE_DELETE(ID)));

        assertEquals(CohorteErrorCode.COHORTE_CON_ASPIRANTES_CONFLICT, ex.getCode());
        CohorteDependenciasOutput dependencias = (CohorteDependenciasOutput) ex.getParam();
        assertEquals(List.of("COHORTE_CON_ASPIRANTES_CONFLICT", "COHORTE_CON_PRUEBAS_CONFLICT"),
                dependencias.conflictos());
        verify(service, never()).deleteById(any());
    }

    // ─── Cascada ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Cohorte libre → borra criterios, documentos, cohorte y plazos en orden")
    void cohorteLibreBorraEnCascada() {
        cohorteSinUso();
        List<CriteriocohorteEntity> criterios = List.of(
                CriteriocohorteEntity.builder().id(1).build(),
                CriteriocohorteEntity.builder().id(2).build(),
                CriteriocohorteEntity.builder().id(3).build());
        List<DocumentosrequisitoconsejocohorteEntity> docsConsejo = List.of(
                DocumentosrequisitoconsejocohorteEntity.builder().id(10).build(),
                DocumentosrequisitoconsejocohorteEntity.builder().id(11).build());
        List<DocumentosrequisitoprogramacohorteEntity> docsPrograma = List.of(
                DocumentosrequisitoprogramacohorteEntity.builder().id(20).build());
        when(criteriocohorteRepository.findByIdCohorte(ID)).thenReturn(criterios);
        when(documentosrequisitoconsejocohorteRepository.findByIdCohorte(ID)).thenReturn(docsConsejo);
        when(documentosrequisitoprogramacohorteRepository.findByIdCohorte(ID)).thenReturn(docsPrograma);

        processor.deleteById(new COHORTE_DELETE(ID));

        InOrder orden = inOrder(criteriocohorteRepository,
                documentosrequisitoconsejocohorteRepository,
                documentosrequisitoprogramacohorteRepository,
                service, plazoService, cohorteRepository);
        orden.verify(criteriocohorteRepository).deleteAll(criterios);
        orden.verify(documentosrequisitoconsejocohorteRepository).deleteAll(docsConsejo);
        orden.verify(documentosrequisitoprogramacohorteRepository).deleteAll(docsPrograma);
        orden.verify(cohorteRepository).flush();   // flush de los hijos
        orden.verify(service).deleteById(ID);
        orden.verify(cohorteRepository).flush();   // DELETE de la cohorte antes de contar plazos
        orden.verify(plazoService).deleteById(PLAZO_DOC);
        orden.verify(plazoService).deleteById(PLAZO_INSC);
        orden.verify(plazoService).deleteById(PLAZO_PAGO);
        orden.verify(cohorteRepository).flush();   // flush de los plazos
    }

    @Test
    @DisplayName("Plazo compartido por documentación e inscripción → se borra una sola vez")
    void plazoCompartidoSeBorraUnaVez() {
        when(service.findById(ID)).thenReturn(cohorte(PLAZO_DOC, PLAZO_DOC, PLAZO_PAGO));
        when(aspiranteService.countByCohorte(ID)).thenReturn(0L);
        when(admitidoService.countByCohorte(ID)).thenReturn(0L);
        when(pruebaService.countByCohorte(ID)).thenReturn(0L);
        when(criteriocohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(documentosrequisitoconsejocohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(documentosrequisitoprogramacohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(cohorteRepository.countByPlazo(anyInt())).thenReturn(0L);
        when(documentoRepository.countByIdPlazo(anyInt())).thenReturn(0L);

        processor.deleteById(new COHORTE_DELETE(ID));

        verify(plazoService, times(1)).deleteById(PLAZO_DOC);
        verify(plazoService, times(1)).deleteById(PLAZO_PAGO);
    }

    @Test
    @DisplayName("Plazo usado por otra cohorte → no se borra")
    void plazoUsadoPorOtraCohorte() {
        cohorteSinUso();
        when(cohorteRepository.countByPlazo(PLAZO_DOC)).thenReturn(1L);

        processor.deleteById(new COHORTE_DELETE(ID));

        verify(plazoService, never()).deleteById(PLAZO_DOC);
        verify(plazoService).deleteById(PLAZO_INSC);
        verify(plazoService).deleteById(PLAZO_PAGO);
    }

    @Test
    @DisplayName("Plazo usado por un documento → no se borra")
    void plazoUsadoPorDocumento() {
        cohorteSinUso();
        when(documentoRepository.countByIdPlazo(PLAZO_INSC)).thenReturn(2L);

        processor.deleteById(new COHORTE_DELETE(ID));

        verify(plazoService, never()).deleteById(PLAZO_INSC);
        verify(plazoService).deleteById(PLAZO_DOC);
        verify(plazoService).deleteById(PLAZO_PAGO);
    }

    // ─── Red de seguridad ────────────────────────────────────────────────────

    @Test
    @DisplayName("Llave no cubierta en el flush → COHORTE_REFERENCIADO_CONFLICT con la restricción")
    @SuppressWarnings("unchecked")
    void llaveNoCubierta() {
        when(service.findById(ID)).thenReturn(cohorte(PLAZO_DOC, PLAZO_INSC, PLAZO_PAGO));
        when(aspiranteService.countByCohorte(ID)).thenReturn(0L);
        when(admitidoService.countByCohorte(ID)).thenReturn(0L);
        when(pruebaService.countByCohorte(ID)).thenReturn(0L);
        when(criteriocohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(documentosrequisitoconsejocohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        when(documentosrequisitoprogramacohorteRepository.findByIdCohorte(ID)).thenReturn(List.of());
        doThrow(enUso()).when(cohorteRepository).flush();

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new COHORTE_DELETE(ID)));

        assertEquals(CohorteErrorCode.COHORTE_REFERENCIADO_CONFLICT, ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals(ID, param.get("idCohorte"));
        assertEquals("criteriocohorte_ibfk_1", param.get("restriccion"));
    }

    @Test
    @DisplayName("En un conflicto no se llama a deleteById ni a los plazos")
    void conflictoNoBorraNada() {
        when(service.findById(ID)).thenReturn(cohorte(PLAZO_DOC, PLAZO_INSC, PLAZO_PAGO));
        when(aspiranteService.countByCohorte(ID)).thenReturn(0L);
        when(admitidoService.countByCohorte(ID)).thenReturn(1L);
        when(pruebaService.countByCohorte(ID)).thenReturn(0L);

        assertThrows(DomainException.class, () -> processor.deleteById(new COHORTE_DELETE(ID)));

        verify(service, never()).deleteById(any());
        verify(criteriocohorteRepository, never()).deleteAll(any());
        verify(plazoService, never()).deleteById(any());
    }
}
