package ufps.edu.co.processor.cases;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.maps.specific.DocumentosrequisitoprogramaMap;
import ufps.edu.co.processor.crud.DocumentosrequisitoprogramaProcessor;
import ufps.edu.co.records.output.cases.Listdocumentosprogramaconsejo;
import ufps.edu.co.records.output.entity.DocumentoRequeridoOutput;
import ufps.edu.co.records.output.entity.DocumentosrequisitoprogramaOutput;
import ufps.edu.co.rest.dto.DocumentosrequisitoconsejoDTO;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramaService;
import ufps.edu.co.rest.services.DocumentosrequisitoprogramacohorteService;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejoService;
import ufps.edu.co.rest.services.DocumentosrequisitoconsejocohorteService;

@Service
public class DocumentosrequisitoprogramaPE extends DocumentosrequisitoprogramaProcessor {

    @Autowired
    private DocumentosrequisitoprogramaService service;

    @Autowired
    private DocumentosrequisitoprogramaMap map;

    @Autowired
    private DocumentosrequisitoconsejoService consejoService;

    @Autowired
    private DocumentosrequisitoprogramacohorteService programaCohorteService;

    @Autowired
    private DocumentosrequisitoconsejocohorteService consejoCohorteService;

    public List<DocumentoRequeridoOutput> findByIdCohorte(Integer idCohorte, Integer idAspirante) {
        List<DocumentoRequeridoOutput> result = new ArrayList<>();

        consejoCohorteService.findByIdCohorte(idCohorte).forEach(junction -> {
            DocumentosrequisitoconsejoDTO doc = consejoService.findById(junction.getIdDocrequisito());
            result.add(DocumentoRequeridoOutput.builder()
                    .idDocumentosrequisitoconsejocohorte(junction.getId())
                    .idDocumentosrequisitoprogramacohorte(null)
                    .nombre(doc.getNombre())
                    .urlformato(doc.getUrlformato())
                    .tamanoMaximoMB(doc.getTamanomaximo())
                    .build());
        });

        programaCohorteService.findByIdCohorte(idCohorte).forEach(junction -> {
            var doc = service.findById(junction.getIdDocrequisito());
            result.add(DocumentoRequeridoOutput.builder()
                    .idDocumentosrequisitoconsejocohorte(null)
                    .idDocumentosrequisitoprogramacohorte(junction.getId())
                    .nombre(doc.getNombre())
                    .urlformato(doc.getUrlformato())
                    .tamanoMaximoMB(doc.getTamanomaximo())
                    .build());
        });

        return result;
    }

    public List<DocumentosrequisitoprogramaOutput> findByIdPrograma(Integer idPrograma) {
        try {
            return service.findByIdPrograma(idPrograma).stream().map(map::toOutput).toList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding Documentosrequisitoprograma by programa: " + e.getMessage(), e);
        }
    }

    public Listdocumentosprogramaconsejo findByIdProgramaAndConsejo(Integer idPrograma) {
        try {
            List<DocumentosrequisitoprogramaOutput> documentosPrograma = findByIdPrograma(idPrograma);
            List<DocumentosrequisitoprogramaOutput> documentosConsejo = consejoService.findAll()
                    .stream()
                    .map(doc -> toProgramaOutput(doc, idPrograma))
                    .toList();

            return Listdocumentosprogramaconsejo.builder()
                    .documentosConsejo(documentosConsejo)
                    .documentosPrograma(documentosPrograma)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error finding Documentosrequisitoprograma by programa y consejo: " + e.getMessage(), e);
        }
    }

    private DocumentosrequisitoprogramaOutput toProgramaOutput(DocumentosrequisitoconsejoDTO consejoDoc,
            Integer idPrograma) {
        return DocumentosrequisitoprogramaOutput.builder()
                .id(consejoDoc.getId())
                .nombre(consejoDoc.getNombre())
                .tamanomaximo(consejoDoc.getTamanomaximo())
                .urlformato(consejoDoc.getUrlformato())
                .id_programa(idPrograma)
                .build();
    }
}
