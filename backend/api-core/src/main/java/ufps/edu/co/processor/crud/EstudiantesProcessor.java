package ufps.edu.co.processor.crud;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.records.input.entity.EstudiantesInput.*;
import ufps.edu.co.records.output.entity.*;
import ufps.edu.co.rest.dto.*;
import ufps.edu.co.rest.services.*;
import ufps.edu.co.services.*;
import ufps.edu.co.utils.*;

@Service
public class EstudiantesProcessor {

    private static final Logger log = LoggerFactory.getLogger(EstudiantesProcessor.class);

    @Autowired
    private EstudiantesService estudiantesService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private AspiranteService aspiranteService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private EstadoService estadoService;

    @Autowired
    private SESService sesService;

    public List<EstudiantesOutput> findAll() {
        return estudiantesService.findAll().stream()
                .map(this::toOutput)
                .toList();
    }

    public EstudiantesOutput findById(Integer id) {
        EstudiantesDTO dto = estudiantesService.findById(id);
        if (dto == null) return null;
        return toOutput(dto);
    }

    public List<EstudiantesOutput> findByProgramaId(Integer programaId) {
        return estudiantesService.findByProgramaId(programaId).stream()
                .map(this::toOutput)
                .toList();
    }

    public List<EstudiantesOutput> findByCohorteId(Integer cohorteId) {
        return estudiantesService.findByCohorteId(cohorteId).stream()
                .map(this::toOutput)
                .toList();
    }

    public List<EstudiantesOutput> findByProgramaIdAndCohorteId(Integer programaId, Integer cohorteId) {
        return estudiantesService.findByProgramaIdAndCohorteId(programaId, cohorteId).stream()
                .map(this::toOutput)
                .toList();
    }

    public EstudiantesOutput create(ESTUDIANTE_CREATE body) {
        if (estudiantesService.existsByCedula(body.cedula())) {
            throw new IllegalArgumentException("Ya existe un estudiante con la cédula: " + body.cedula());
        }
        EstudiantesDTO dto = EstudiantesDTO.builder()
                .apellido(body.apellido())
                .apellido2(body.apellido2())
                .cedula(body.cedula())
                .codigo(body.codigo())
                .email(body.email())
                .esposgrado(body.esposgrado())
                .fechalngreso(body.fechalngreso())
                .fechanacimiento(body.fechanacimiento())
                .migrado(body.migrado())
                .moodleld(body.moodleld())
                .nombre(body.nombre())
                .nombre2(body.nombre2())
                .telefono(body.telefono())
                .cohorteId(body.cohorteId())
                .estadoEstudianteId(body.estadoEstudianteId())
                .pensumId(body.pensumId())
                .programaId(body.programaId())
                .usuarioId(body.usuarioId())
                .build();
        return toOutput(estudiantesService.create(dto));
    }

    public EstudiantesOutput update(Integer id, ESTUDIANTE_UPDATE body) {
        if (estudiantesService.existsByCedulaAndIdNot(body.cedula(), id)) {
            throw new IllegalArgumentException("Ya existe otro estudiante con la cédula: " + body.cedula());
        }
        EstudiantesDTO dto = EstudiantesDTO.builder()
                .apellido(body.apellido())
                .apellido2(body.apellido2())
                .cedula(body.cedula())
                .codigo(body.codigo())
                .email(body.email())
                .esposgrado(body.esposgrado())
                .fechalngreso(body.fechalngreso())
                .fechanacimiento(body.fechanacimiento())
                .migrado(body.migrado())
                .moodleld(body.moodleld())
                .nombre(body.nombre())
                .nombre2(body.nombre2())
                .telefono(body.telefono())
                .cohorteId(body.cohorteId())
                .estadoEstudianteId(body.estadoEstudianteId())
                .pensumId(body.pensumId())
                .programaId(body.programaId())
                .usuarioId(body.usuarioId())
                .build();
        return toOutput(estudiantesService.update(id, dto));
    }

    public void delete(Integer id) {
        estudiantesService.deleteById(id);
    }

    private EstudiantesOutput toOutput(EstudiantesDTO dto) {
        ProgramaOutput programaOutput = null;
        if (dto.getProgramaId() != null) {
            ProgramaDTO prog = programaService.findById(dto.getProgramaId());
            if (prog != null) {
                programaOutput = ProgramaOutput.builder()
                        .id(prog.getId())
                        .nombre(prog.getNombre())
                        .codigo(prog.getCodigo())
                        .build();
            }
        }
        return EstudiantesOutput.builder()
                .id(dto.getId())
                .apellido(dto.getApellido())
                .apellido2(dto.getApellido2())
                .cedula(dto.getCedula())
                .codigo(dto.getCodigo())
                .email(dto.getEmail())
                .esposgrado(dto.getEsposgrado())
                .fechalngreso(dto.getFechalngreso())
                .fechanacimiento(dto.getFechanacimiento())
                .migrado(dto.getMigrado())
                .moodleld(dto.getMoodleld())
                .nombre(dto.getNombre())
                .nombre2(dto.getNombre2())
                .telefono(dto.getTelefono())
                .cohorteId(dto.getCohorteId())
                .estadoEstudianteId(dto.getEstadoEstudianteId())
                .pensumId(dto.getPensumId())
                .programaId(dto.getProgramaId())
                .usuarioId(dto.getUsuarioId())
                .programa(programaOutput)
                .build();
    }

    @Transactional
    public void registrarAspiranteComoEstudiante(Integer idAspirante) {
        log.info("Iniciando registro de aspirante {} como estudiante", idAspirante);

        AspiranteDTO aspirante = aspiranteService.findById(idAspirante);
        if (aspirante == null) {
            throw new RuntimeException("Aspirante no encontrado: " + idAspirante);
        }

        PersonaDTO persona = personaService.findById(aspirante.getIdPersona());
        if (persona == null) {
            throw new RuntimeException("Persona no encontrada para aspirante: " + idAspirante);
        }

        String cedula = persona.getDocumentopersona() != null && persona.getDocumentopersona().getNumerodocumento() != null
                ? String.valueOf(persona.getDocumentopersona().getNumerodocumento())
                : null;

        if (cedula != null && estudiantesService.existsByCedula(cedula)) {
            log.warn("Ya existe un estudiante con la cédula del aspirante {}, omitiendo registro", idAspirante);
            return;
        }

        CohorteDTO cohorte = cohorteService.findById(aspirante.getIdCohorte());
        if (cohorte == null || cohorte.getSemestre() == null) {
            throw new RuntimeException("Cohorte o semestre no encontrado para aspirante: " + idAspirante);
        }

        if (cohorte.getIdPrograma() == null) {
            throw new RuntimeException("La cohorte " + cohorte.getId() + " no tiene programa asociado");
        }

        ProgramaDTO programa = programaService.findById(cohorte.getIdPrograma());
        if (programa == null || programa.getCodigo() == null) {
            throw new RuntimeException("Programa no encontrado o sin código: " + cohorte.getIdPrograma());
        }

        String ultimoCodigo = estudiantesService.findUltimoCodigoPorPrograma(cohorte.getIdPrograma());
        String secuencial;
        if (ultimoCodigo == null || ultimoCodigo.length() < 4) {
            secuencial = "0000";
        } else {
            try {
                int ultimo = Integer.parseInt(ultimoCodigo.substring(ultimoCodigo.length() - 4));
                secuencial = String.format("%04d", (ultimo + 1) % 10000);
            } catch (NumberFormatException e) {
                log.warn("No se pudo parsear el secuencial del código '{}', comenzando en 0000", ultimoCodigo);
                secuencial = "0000";
            }
        }
        String codigoEstudiante = programa.getCodigo() + secuencial;
        log.info("Código generado para aspirante {}: {}", idAspirante, codigoEstudiante);

        String[] partesNombre = persona.getNombres() != null
                ? persona.getNombres().trim().split("\\s+", 2) : new String[]{};
        String nombre = partesNombre.length > 0 ? partesNombre[0] : null;
        String nombre2 = partesNombre.length > 1 ? partesNombre[1] : null;

        String[] partesApellido = persona.getApellidos() != null
                ? persona.getApellidos().trim().split("\\s+", 2) : new String[]{};
        String apellido = partesApellido.length > 0 ? partesApellido[0] : null;
        String apellido2 = partesApellido.length > 1 ? partesApellido[1] : null;

        EstadoDTO estadoActivo = estadoService.findByTipoAndEntidad("ACTIVO", "estudiante");
        if (estadoActivo == null) {
            estadoActivo = estadoService.findByTipoAndEntidad("ACTIVO", "ESTUDIANTE");
        }
        if (estadoActivo == null) {
            log.warn("No se encontró estado ACTIVO para entidad estudiante, el campo quedará en null");
        }

        EstudiantesDTO dto = EstudiantesDTO.builder()
                .nombre(nombre)
                .nombre2(nombre2)
                .apellido(apellido)
                .apellido2(apellido2)
                .cedula(cedula)
                .email(persona.getCorreo())
                .telefono(persona.getTelefono() != null ? persona.getTelefono() : persona.getCelular())
                .fechanacimiento(persona.getFechanacimiento())
                .codigo(codigoEstudiante)
                .fechalngreso(LocalDate.now())
                .programaId(cohorte.getIdPrograma())
                .cohorteId(cohorte.getId())
                .esposgrado(programa.getEsPosgrado())
                .estadoEstudianteId(estadoActivo != null ? estadoActivo.getId() : null)
                .pensumId(null)
                .usuarioId(null)
                .migrado("N")
                .moodleld(null)
                .build();

        EstudiantesDTO creado = estudiantesService.create(dto);
        log.info("Aspirante {} registrado como estudiante con id {} y código {}",
                idAspirante, creado.getId(), creado.getCodigo());

        if (persona.getCorreo() != null) {
            sesService.enviarCorreoAsync(
                    persona.getCorreo(),
                    EmailTemplates.ASUNTO_CODIGO_ESTUDIANTIL,
                    EmailTemplates.cuerpoCodigoEstudiantil(
                            persona.getNombres(), persona.getApellidos(),
                            codigoEstudiante, programa.getNombre()));
        }
    }
}
