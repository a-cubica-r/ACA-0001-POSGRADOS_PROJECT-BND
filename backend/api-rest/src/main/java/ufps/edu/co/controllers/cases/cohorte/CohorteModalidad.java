package ufps.edu.co.controllers.cases.cohorte;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.List;
import org.springframework.http.ResponseEntity;
import ufps.edu.co.services.core.crud.ProgramaCoreService;
import ufps.edu.co.records.output.entity.ModalidadOutput;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping(value = "/director-programa/programa", produces = MediaType.APPLICATION_JSON_VALUE)
public class CohorteModalidad {

	@Autowired
	private ProgramaCoreService programaProcessor;

	@GetMapping(value = "/{programaId}/modalidades")
	public ResponseEntity<List<ModalidadOutput>> findModalidadesByPrograma(@PathVariable Integer programaId) {
		return ResponseEntity.ok(programaProcessor.findModalidadesByPrograma(programaId));
	}
}
