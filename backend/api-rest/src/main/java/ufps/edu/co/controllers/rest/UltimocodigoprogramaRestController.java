package ufps.edu.co.controllers.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ufps.edu.co.records.output.entity.UltimocodigoprogramaOutput;
import ufps.edu.co.services.core.crud.UltimocodigoprogramaCoreService;

/**
 * D-00 — Consecutivos del código de estudiante por programa.
 *
 * <p>Prefijo global: {@code /api/dev/endpoint}. Solo lo consulta la pantalla de
 * «Últimos códigos» del Superadministrador. La edición del consecutivo vive en
 * {@code DirectorProgramaCase} por compatibilidad con el contrato del legacy.</p>
 */
@RestController
@RequestMapping(value = "/ultimocodigoprograma", produces = MediaType.APPLICATION_JSON_VALUE)
public class UltimocodigoprogramaRestController {

    @Autowired
    private UltimocodigoprogramaCoreService processor;

    @GetMapping("/listall")
    public ResponseEntity<List<UltimocodigoprogramaOutput>> findAll() {
        return ResponseEntity.ok(processor.findAll());
    }
}
