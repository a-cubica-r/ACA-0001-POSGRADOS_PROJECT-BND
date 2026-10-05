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

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.services.core.crud.ProgramaCoreService;
import ufps.edu.co.records.input.entity.CohorteInput.COHORTE_FIND;
import ufps.edu.co.records.input.entity.FacultadInput.FACULTAD_FIND;
import ufps.edu.co.records.input.entity.ProgramaInput.*;
import ufps.edu.co.records.output.entity.ProgramaOutput;

@RestController
@RequestMapping(value = "/programa", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProgramaRestController {

    @Autowired
    private ProgramaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<ProgramaOutput>> findAll() {
        List<ProgramaOutput> list = processor.findAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProgramaOutput> findById(@RequestBody PROGRAMA_FIND request) {
        ProgramaOutput output = processor.findById(request);
        if (output != null) {
            return ResponseEntity.ok(output);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ProgramaOutput> create(@RequestBody PROGRAMA_CREATE request) {
        ProgramaOutput output = processor.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PutMapping("/update")
    public ResponseEntity<ProgramaOutput> update(@RequestBody PROGRAMA_UPDATE request) {
        try {
            ProgramaOutput updated = processor.update(request);
            return ResponseEntity.ok(updated);
        } catch (DomainException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestBody PROGRAMA_DELETE request) {
        try {
            processor.deleteById(request);
            return ResponseEntity.noContent().build();
        } catch (DomainException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/listbyfacultad", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProgramaOutput>> findByIdFacultad(@RequestBody FACULTAD_FIND request) {
        List<ProgramaOutput> list = processor.findByIdFacultad(request.id());
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/count-en-proceso", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> countAspirantesEnProcesoEnCohorteAbierta(@RequestBody COHORTE_FIND request) {

        Long total = processor.countAspirantesEnProcesoEnCohorteAbierta(request.id());

        return ResponseEntity.ok(total);
    }
}
