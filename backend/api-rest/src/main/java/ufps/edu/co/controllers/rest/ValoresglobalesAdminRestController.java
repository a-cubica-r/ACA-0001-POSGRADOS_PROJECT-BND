/*
 * Created on 2026-05-30
 */
package ufps.edu.co.controllers.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ufps.edu.co.services.core.crud.ValoresglobalesCoreService;
import ufps.edu.co.rest.dto.ValoresglobalesDTO;

@RestController
@RequestMapping(value = "/superadmin/valoresglobales", produces = MediaType.APPLICATION_JSON_VALUE)
public class ValoresglobalesAdminRestController {

    @Autowired
    private ValoresglobalesCoreService valoresglobalesProcessor;

    @PostMapping(value = "/tamano-maximo-archivos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValoresglobalesDTO> createTamanoMaximoArchivos(@RequestBody ValoresglobalesDTO dto) {
        ValoresglobalesDTO created = valoresglobalesProcessor.createTamanoMaximoArchivos(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/salario-minimo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValoresglobalesDTO> createSalarioMinimo(@RequestBody ValoresglobalesDTO dto) {
        ValoresglobalesDTO created = valoresglobalesProcessor.createSalarioMinimo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/valor-inscripcion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValoresglobalesDTO> createValorInscripcion(@RequestBody ValoresglobalesDTO dto) {
        ValoresglobalesDTO created = valoresglobalesProcessor.createValorInscripcion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(value = "/tamano-maximo-archivos")
    public ResponseEntity<List<ValoresglobalesDTO>> findAllTamanoMaximoArchivos() {
        return ResponseEntity.ok(valoresglobalesProcessor.findAllByPrefix("TAMANO_MAXIMO_ARCHIVOS"));
    }

    @GetMapping(value = "/tamano-maximo-archivos/actual")
    public ResponseEntity<ValoresglobalesDTO> findCurrentTamanoMaximoArchivos() {
        return ResponseEntity.ok(valoresglobalesProcessor.findCurrentByPrefix("TAMANO_MAXIMO_ARCHIVOS"));
    }

    @GetMapping(value = "/salario-minimo")
    public ResponseEntity<List<ValoresglobalesDTO>> findAllSalarioMinimo() {
        return ResponseEntity.ok(valoresglobalesProcessor.findAllByPrefix("SALARIO_MINIMO"));
    }

    @GetMapping(value = "/salario-minimo/actual")
    public ResponseEntity<ValoresglobalesDTO> findCurrentSalarioMinimo() {
        return ResponseEntity.ok(valoresglobalesProcessor.findCurrentByPrefix("SALARIO_MINIMO"));
    }

    @GetMapping(value = "/valor-inscripcion")
    public ResponseEntity<List<ValoresglobalesDTO>> findAllValorInscripcion() {
        return ResponseEntity.ok(valoresglobalesProcessor.findAllByPrefix("VALOR_INSCRIPCION"));
    }

    @GetMapping(value = "/valor-inscripcion/actual")
    public ResponseEntity<ValoresglobalesDTO> findCurrentValorInscripcion() {
        return ResponseEntity.ok(valoresglobalesProcessor.findCurrentByPrefix("VALOR_INSCRIPCION"));
    }
}
