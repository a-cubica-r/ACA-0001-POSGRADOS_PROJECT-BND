package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.DocumentosrequisitoprogramaErrorCode;
import ufps.edu.co.maps.specific.DocumentosrequisitoprogramaMap;
import ufps.edu.co.records.input.entity.DocumentosrequisitoprogramaInput.DOCUMENTOSREQUISITOPROGRAMA_DELETE;
import ufps.edu.co.rest.dto.DocumentosrequisitoprogramaDTO;
import ufps.edu.co.rest.dto.DocumentosrequisitoprogramacohorteDTO;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramaService;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramacohorteService;
import ufps.edu.co.services.core.crud.DocumentosrequisitoprogramaCoreService;

/**
 * PLN0010 T2 — Un documento requerido asignado a una cohorte no se borra (409
 * DOCUMENTO_REQUISITO_EN_USO_CONFLICT) y uno inexistente responde 404
 * DOCUMENTO_REQUISITO_NOT_FOUND.
 */
@ExtendWith(MockitoExtension.class)
class DocumentoEnUsoTest {

    @Mock private DocumentosrequisitoprogramaService service;
    @Mock private DocumentosrequisitoprogramaMap map;
    @Mock private DocumentosrequisitoprogramacohorteService programaCohorteService;

    @InjectMocks
    private DocumentosrequisitoprogramaCoreService processor;

    private static DocumentosrequisitoprogramaDTO documento(Integer id) {
        return DocumentosrequisitoprogramaDTO.builder().id(id).nombre("Documento").build();
    }

    @Test
    @DisplayName("deleteById de un documento asignado a la cohorte 8 → DOCUMENTO_REQUISITO_EN_USO_CONFLICT")
    @SuppressWarnings("unchecked")
    void documentoAsignadoACohorte() {
        when(service.findById(1)).thenReturn(documento(1));
        when(programaCohorteService.findByIdDocrequisito(1)).thenReturn(List.of(
                DocumentosrequisitoprogramacohorteDTO.builder().id(70).idDocrequisito(1).idCohorte(8).build()));

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new DOCUMENTOSREQUISITOPROGRAMA_DELETE(1)));

        assertEquals(DocumentosrequisitoprogramaErrorCode.DOCUMENTO_REQUISITO_EN_USO_CONFLICT, ex.getCode());
        Map<String, Object> param = (Map<String, Object>) ex.getParam();
        assertEquals(1, param.get("documentoId"));
        assertEquals(List.of(8), param.get("cohortes"));
        verify(service, never()).deleteById(1);
    }

    @Test
    @DisplayName("deleteById de un documento inexistente → DOCUMENTO_REQUISITO_NOT_FOUND")
    void documentoInexistente() {
        when(service.findById(99999)).thenReturn(null);

        DomainException ex = assertThrows(DomainException.class,
                () -> processor.deleteById(new DOCUMENTOSREQUISITOPROGRAMA_DELETE(99999)));

        assertEquals(DocumentosrequisitoprogramaErrorCode.DOCUMENTO_REQUISITO_NOT_FOUND, ex.getCode());
        verify(service, never()).deleteById(99999);
    }

    @Test
    @DisplayName("deleteById de un documento sin asignaciones → borra")
    void documentoSinAsignaciones() {
        when(service.findById(2)).thenReturn(documento(2));
        when(programaCohorteService.findByIdDocrequisito(2)).thenReturn(List.of());

        processor.deleteById(new DOCUMENTOSREQUISITOPROGRAMA_DELETE(2));

        verify(service).deleteById(2);
    }
}
