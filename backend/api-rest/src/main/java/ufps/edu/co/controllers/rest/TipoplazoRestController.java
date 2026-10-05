package ufps.edu.co.controllers.rest;

import java.util.List;
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

import ufps.edu.co.services.core.crud.TipoplazoCoreService;
import ufps.edu.co.records.input.entity.TipoplazoInput.*;
import ufps.edu.co.records.output.entity.TipoplazoOutput;

@RestController
@RequestMapping(value = "/tipoplazo", produces = MediaType.APPLICATION_JSON_VALUE)
public class TipoplazoRestController {

    @Autowired
    private TipoplazoCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<TipoplazoOutput>> findAll() {
        List<TipoplazoOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TipoplazoOutput> findById(@RequestBody TIPOPLAZO_FIND request) {
        TipoplazoOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<TipoplazoOutput> create(@RequestBody TIPOPLAZO_CREATE request) {
        TipoplazoOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<TipoplazoOutput> update(@RequestBody TIPOPLAZO_UPDATE request) {
        try {
            TipoplazoOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody TIPOPLAZO_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
