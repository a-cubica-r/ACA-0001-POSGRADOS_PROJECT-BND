package ufps.edu.co.controllers.cases.sede;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.services.core.crud.SedeCoreService;
import ufps.edu.co.records.input.entity.SedeInput.SEDE_CREATE_WITH_UBICACION;
import ufps.edu.co.records.output.entity.SedeOutput;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(value = "/sede", produces = MediaType.APPLICATION_JSON_VALUE)
public class SedeRestCase {

    @Autowired
    private SedeCoreService processor;

    @PostMapping("/createWithUbicacion")
    public ResponseEntity<SedeOutput> createWithUbicacion(@RequestBody SEDE_CREATE_WITH_UBICACION request) {
        SedeOutput output = processor.createWithUbicacion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }
}
