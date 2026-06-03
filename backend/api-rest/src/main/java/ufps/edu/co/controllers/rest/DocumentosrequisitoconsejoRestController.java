package ufps.edu.co.controllers.rest;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.processor.crud.DocumentosrequisitoconsejoProcessor;
import ufps.edu.co.records.input.entity.DocumentosrequisitoconsejoInput.*;
import ufps.edu.co.records.output.entity.DocumentosrequisitoconsejoOutput;

@RestController
@RequestMapping(value = "/documentosrequisitoconsejo", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentosrequisitoconsejoRestController {

    @Autowired
    private DocumentosrequisitoconsejoProcessor processor;

    @Autowired
    private ufps.edu.co.services.S3Service s3Service;

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/listall")
    public ResponseEntity<List<DocumentosrequisitoconsejoOutput>> findAll() {
        return ResponseEntity.ok(processor.findAll());
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoconsejoOutput> findById(@RequestBody DOCUMENTOSREQUISITOCONSEJO_FIND request) {
        DocumentosrequisitoconsejoOutput output = processor.findById(request);
        return output != null ? ResponseEntity.ok(output) : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoconsejoOutput> create(@RequestBody DOCUMENTOSREQUISITOCONSEJO_CREATE request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processor.create(request));
    }

    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentosrequisitoconsejoOutput> update(@RequestBody DOCUMENTOSREQUISITOCONSEJO_UPDATE request) {
        try {
            return ResponseEntity.ok(processor.update(request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@RequestBody DOCUMENTOSREQUISITOCONSEJO_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

        @PutMapping(value = "/{id}/formato", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> uploadFormato(@PathVariable Integer id,
            @RequestPart(value = "file", required = true) MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "file is required"));
            }
            var upload = s3Service.uploadFile(file);
            String url = upload.enlaceurl();
            processor.updateUrlFormato(id, url);
            return ResponseEntity.ok(Map.of("urlformato", url));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/superadmin/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createWithFile(@RequestPart(value = "body", required = true) String body,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            DOCUMENTOSREQUISITOCONSEJO_CREATE request = objectMapper.readValue(body,
                    DOCUMENTOSREQUISITOCONSEJO_CREATE.class);

            DocumentosrequisitoconsejoOutput created = processor.create(request);

            if (file != null && !file.isEmpty()) {
                var upload = s3Service.uploadFile(file);
                String url = upload.enlaceurl();
                processor.updateUrlFormato(created.id(), url);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("id", created.id(), "urlformato", url));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/superadmin/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editDocumentosrequisitoconsejo(@PathVariable Integer id,
            @RequestPart(value = "body", required = false) String body,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            // If body present, parse and update metadata
            if (body != null && !body.isBlank()) {
                DOCUMENTOSREQUISITOCONSEJO_UPDATE request = objectMapper.readValue(body,
                        DOCUMENTOSREQUISITOCONSEJO_UPDATE.class);
                // Ensure path id is authoritative
                if (request.id() == null || !request.id().equals(id)) {
                    request = new DOCUMENTOSREQUISITOCONSEJO_UPDATE(id, request.nombre(), request.tamanomaximo());
                }
                processor.update(request);
            }

            // If file present, upload and update urlformato
            if (file != null && !file.isEmpty()) {
                var upload = s3Service.uploadFile(file);
                String url = upload.enlaceurl();
                processor.updateUrlFormato(id, url);
                return ResponseEntity.ok(Map.of("urlformato", url));
            }

            // Return the updated record
            DocumentosrequisitoconsejoOutput updated = processor.findById(
                    new DOCUMENTOSREQUISITOCONSEJO_FIND(id));
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
