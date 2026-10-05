package ufps.edu.co.controllers.cases.programa;

import ufps.edu.co.records.input.entity.DocumentosrequisitoprogramaInput.*;
import ufps.edu.co.records.output.cases.Listdocumentosprogramaconsejo;
import ufps.edu.co.records.output.entity.DocumentosrequisitoprogramaOutput;
import ufps.edu.co.services.S3Service;
import ufps.edu.co.services.core.cases.DocumentosrequisitoprogramaPE;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/director-programa/programa", produces = MediaType.APPLICATION_JSON_VALUE)
public class Programadocumentos {

    @Autowired
    private DocumentosrequisitoprogramaPE processor;

    @Autowired
    private S3Service s3Service;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createDocumentosrequisitoprograma(
            @Parameter(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DOCUMENTOSREQUISITOPROGRAMA_CREATE.class))) @RequestPart("body") String body,
            @Parameter(required = false, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            DOCUMENTOSREQUISITOPROGRAMA_CREATE requestBody = objectMapper.readValue(
                    body, DOCUMENTOSREQUISITOPROGRAMA_CREATE.class);

            String urlformato = null;
            if (file != null && !file.isEmpty()) {
                urlformato = s3Service.uploadFile(file).enlaceurl();
            }
            DOCUMENTOSREQUISITOPROGRAMA_CREATEDOCUMENT bodyConUrlUpdate = DOCUMENTOSREQUISITOPROGRAMA_CREATEDOCUMENT
                    .builder()
                    .nombre(requestBody.nombre())
                    .tamanomaximo(5)
                    .urlformato(urlformato)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(processor.create(bodyConUrlUpdate, requestBody.idPrograma()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{idprograma}/documentos")
    public ResponseEntity<List<DocumentosrequisitoprogramaOutput>> listDocumentosrequisitoprogramaByPrograma(
            @PathVariable Integer idprograma) {

        if (idprograma != null) {
            try {

                return ResponseEntity.ok(processor.findByIdPrograma(idprograma));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping(value = "/documento/{documentoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateDocumentosrequisitoprograma(
            @PathVariable Integer documentoId,
            @Parameter(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DOCUMENTOSREQUISITOPROGRAMA_CREATE.class))) @RequestPart("body") String body,
            @Parameter(required = false, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            DOCUMENTOSREQUISITOPROGRAMA_CREATE requestBody = objectMapper.readValue(
                    body, DOCUMENTOSREQUISITOPROGRAMA_CREATE.class);

            String urlformato = null;
            if (file != null && !file.isEmpty()) {
                urlformato = s3Service.uploadFile(file).enlaceurl();
            }

            DOCUMENTOSREQUISITOPROGRAMA_CREATEDOCUMENT bodyConUrlUpdate = DOCUMENTOSREQUISITOPROGRAMA_CREATEDOCUMENT
                    .builder()
                    .nombre(requestBody.nombre())
                    .tamanomaximo(5)
                    .urlformato(urlformato)
                    .build();

            return ResponseEntity.ok(processor.update(bodyConUrlUpdate, documentoId, requestBody.idPrograma()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/documento/{documentoId}")
    public ResponseEntity<Void> deleteDocumentosrequisitoprograma(@PathVariable Integer documentoId) {
        try {

            processor.deleteById(new DOCUMENTOSREQUISITOPROGRAMA_DELETE(documentoId));
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ###################### CASES ######################

    @GetMapping("/{idprograma}/documentos/requeridos")
    public ResponseEntity<Listdocumentosprogramaconsejo> getDocsfromProgramaAndConsejo(
            @PathVariable Integer idprograma) {

        if (idprograma != null) {
            try {
                return ResponseEntity.ok(processor.findByIdProgramaAndConsejo(idprograma));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Listdocumentosprogramaconsejo.builder()
                                .documentosConsejo(List.of())
                                .documentosPrograma(List.of())
                                .build());
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
