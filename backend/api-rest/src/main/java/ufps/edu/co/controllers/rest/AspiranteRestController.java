package ufps.edu.co.controllers.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.AspiranteCoreService;
import ufps.edu.co.records.input.entity.AspiranteInput.*;
import ufps.edu.co.records.output.entity.AspiranteOutput;
import ufps.edu.co.records.output.entity.EstadoOutput;

@RestController
@RequestMapping(value = "/aspirante", produces = MediaType.APPLICATION_JSON_VALUE)
public class AspiranteRestController {

    @Autowired
    private AspiranteCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<Page<AspiranteOutput>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(processor.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AspiranteOutput> findById(@PathVariable Integer id) {
        AspiranteOutput output = processor.findById(new ASPIRANTE_FIND(id));
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<AspiranteOutput> create(@Valid @RequestBody ASPIRANTE_CREATE request) {
        AspiranteOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<AspiranteOutput> update(@Valid @RequestBody ASPIRANTE_UPDATE request) {
        try {
            AspiranteOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@Valid @RequestBody ASPIRANTE_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/estado")
    public ResponseEntity<EstadoOutput> findEstadoById(@PathVariable Integer id) {
        EstadoOutput estado = processor.findEstadoById(new ASPIRANTE_FIND(id));
        if (estado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(estado);
    }
}
