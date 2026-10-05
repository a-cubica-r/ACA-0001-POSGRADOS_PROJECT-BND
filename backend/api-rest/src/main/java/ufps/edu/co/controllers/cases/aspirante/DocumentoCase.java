package ufps.edu.co.controllers.cases.aspirante;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.DocumentoCoreService;
import ufps.edu.co.records.input.entity.DocumentoInput.DOCUMENTO_ESTADO_UPDATE;
import ufps.edu.co.records.output.entity.DocumentoEstadoOutput;

@RestController
@RequestMapping(value = "/documentos", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentoCase {

    @Autowired
    private DocumentoCoreService documentoProcessor;

    @PatchMapping(value = "/{idDoc}/estado", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentoEstadoOutput> updateEstadoDocumento(
            @PathVariable Integer idDoc,
            @RequestBody DOCUMENTO_ESTADO_UPDATE body) {
        return ResponseEntity.ok(documentoProcessor.updateEstadoDocumento(idDoc, body));
    }
}
