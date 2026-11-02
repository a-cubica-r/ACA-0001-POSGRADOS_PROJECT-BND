package ufps.edu.co.services.core.integracion;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.IntegracionTramitesErrorCode;
import ufps.edu.co.records.output.cases.SituacionAcademicaOutput;
import ufps.edu.co.rest.dto.CohorteDTO;
import ufps.edu.co.rest.dto.EstadoDTO;
import ufps.edu.co.rest.dto.EstudiantesDTO;
import ufps.edu.co.rest.dto.ProgramaDTO;
import ufps.edu.co.rest.services.CohorteService;
import ufps.edu.co.rest.services.EstadoService;
import ufps.edu.co.rest.services.EstudiantesService;
import ufps.edu.co.rest.services.ProgramaService;

/**
 * SC-03 - Situación académica de un estudiante para el sistema de Trámites.
 * Solo lectura.
 */
@Service
public class IntegracionTramitesCoreService {

    private static final Pattern CEDULA = Pattern.compile("\\d{5,15}");

    /**
     * El estudiante solo se crea cuando se aprueba el pago de la matrícula
     * (EstudianteRegistroListener), así que si existe, la matrícula está pagada.
     */
    static final String MATRICULA_COMPLETADA = "COMPLETADO";

    @Autowired
    private EstudiantesService estudiantesService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private EstadoService estadoService;

    @Transactional(readOnly = true)
    public SituacionAcademicaOutput consultar(String cedula) {
        if (cedula == null || !CEDULA.matcher(cedula).matches()) {
            throw new DomainException(IntegracionTramitesErrorCode.CEDULA_INVALIDA, cedula);
        }
        EstudiantesDTO est = estudiantesService.findByCedula(cedula);
        if (est == null) {
            throw new DomainException(IntegracionTramitesErrorCode.ESTUDIANTE_NOT_FOUND, cedula);
        }

        // El DTO no trae programaId (EstudiantesMapper lo ignora): se consulta aparte.
        Integer programaId = estudiantesService.findProgramaIdByCedula(cedula);
        ProgramaDTO programa = programaId != null ? programaService.findById(programaId) : null;
        CohorteDTO cohorte = est.getCohorteId() != null ? cohorteService.findById(est.getCohorteId()) : null;
        EstadoDTO estado = est.getEstadoEstudianteId() != null ? estadoService.findById(est.getEstadoEstudianteId()) : null;
        Integer creditosRequeridos = programa != null ? programa.getCreditos() : null;

        return SituacionAcademicaOutput.builder()
                .cedula(est.getCedula())
                .codigo(est.getCodigo())
                .nombres(unir(est.getNombre(), est.getNombre2()))
                .apellidos(unir(est.getApellido(), est.getApellido2()))
                .email(est.getEmail())
                .estado(estado != null ? estado.getTipo() : null)
                .programa(programa == null ? null : SituacionAcademicaOutput.Programa.builder()
                        .id(programa.getId())
                        .codigo(programa.getCodigo())
                        .nombre(programa.getNombre())
                        .creditosRequeridos(creditosRequeridos)
                        .build())
                .admision(SituacionAcademicaOutput.Admision.builder()
                        .cohorteId(est.getCohorteId())
                        .cohorte(cohorte != null ? cohorte.getNombre() : null)
                        .semestre(cohorte != null && cohorte.getSemestre() != null
                                ? cohorte.getSemestre().getNombre() : null)
                        .fechaIngreso(est.getFechalngreso())
                        .build())
                .matricula(SituacionAcademicaOutput.Matricula.builder()
                        .estado(MATRICULA_COMPLETADA)
                        .valor(programa != null ? programa.getValormatricula() : null)
                        .build())
                .creditos(SituacionAcademicaOutput.Creditos.builder()
                        .requeridos(creditosRequeridos)
                        .build())
                .build();
    }

    private static String unir(String primero, String segundo) {
        String texto = Stream.of(primero, segundo)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
        return texto.isEmpty() ? null : texto;
    }
}
