package ufps.edu.co.processor.crud;

import java.time.*;
import java.util.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufps.edu.co.domain.exceptions.*;
import ufps.edu.co.domain.exceptions.errorcodes.*;
import ufps.edu.co.maps.specific.*;
import ufps.edu.co.records.input.entity.ListaadmitidosInput.*;
import ufps.edu.co.records.output.entity.*;
import ufps.edu.co.rest.dto.*;
import ufps.edu.co.rest.services.*;
import ufps.edu.co.services.*;
import ufps.edu.co.utils.*;

@Service
public class ListaadmitidosProcessor {

        private static final Logger logger = LoggerFactory.getLogger(ListaadmitidosProcessor.class);

        @Autowired
        private CohorteService cohorteService;

        @Autowired
        private AdministrativoService administrativoService;

        @Autowired
        private AspiranteService aspiranteService;

        @Autowired
        private ListaadmitidosService listaadmitidosService;

        @Autowired
        private EstadoService estadoService;

        @Autowired
        private ListaadmitidosMap map;

        @Autowired
        private SESService sesService;

        @Autowired
        private PdfGeneratorService pdfGeneratorService;

        public ListaAdmitidosResumenOutput generateAdmittedList(Integer idCohorte) {
                CohorteDTO cohorte = cohorteService.findById(idCohorte);
                if (cohorte == null) {
                        throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idCohorte);
                }

                int cupos = cohorte.getCupos();
                List<AspiranteDTO> admitidos = aspiranteService.findAdmitidosByCohorte(idCohorte)
                                .stream()
                                .limit(cupos)
                                .toList();

                long totalAdmitidos = admitidos.size();
                int cuposDisponibles = Math.max(0, cupos - (int) totalAdmitidos);
                boolean activa = cohorte.getEstado() != null
                                && "ABIERTA".equalsIgnoreCase(cohorte.getEstado().getTipo());

                List<ListaAdmitidosResumenOutput.AspiranteResumen> aspirantes = admitidos.stream()
                                .map(a -> {
                                        String nombre = a.getPersona() != null
                                                        ? ((a.getPersona().getNombres() != null
                                                                        ? a.getPersona().getNombres()
                                                                        : "") + " "
                                                                        + (a.getPersona().getApellidos() != null
                                                                                        ? a.getPersona().getApellidos()
                                                                                        : ""))
                                                                        .trim()
                                                        : "";
                                        String numerodocumento = a.getPersona() != null
                                                        && a.getPersona().getDocumentopersona() != null
                                                                        ? a.getPersona().getDocumentopersona()
                                                                                        .getNumerodocumento()
                                                                        : null;
                                        return ListaAdmitidosResumenOutput.AspiranteResumen.builder()
                                                        .id(a.getId())
                                                        .nombre(nombre)
                                                        .numerodocumento(numerodocumento)
                                                        .correo(a.getPersona() != null ? a.getPersona().getCorreo()
                                                                        : null)
                                                        .puntaje(a.getPuntuacion())
                                                        .build();
                                })
                                .toList();

                return ListaAdmitidosResumenOutput.builder()
                                .cohorteActual(ListaAdmitidosResumenOutput.CohorteResumen.builder()
                                                .id(cohorte.getId())
                                                .nombre(cohorte.getNombre())
                                                .activa(activa)
                                                .cuposDisponibles(cuposDisponibles)
                                                .totalAdmitidos(totalAdmitidos)
                                                .build())
                                .aspirantes(aspirantes)
                                .build();
        }

        public List<ListaadmitidosOutput> admitirAspirantes(GENERATE_LISTA input) {
                CohorteDTO cohorte = validateAndGetCohorte(input.idCohorte(), input.idAdministrativo());
                List<AspiranteDTO> admitidos = getTopCandidates(input.idCohorte(), null, cohorte.getCupos());
                LocalDate today = LocalDate.now();

                EstadoDTO estadoPorLegalizar = estadoService.findByTipoAndEntidad("POR LEGALIZAR", "aspirante");
                if (estadoPorLegalizar == null) {
                        estadoPorLegalizar = estadoService.findByTipoAndEntidad("POR LEGALIZAR", "ASPIRANTE");
                }
                if (estadoPorLegalizar == null) {
                        throw new DomainException(ListaadmitidosErrorCode.ESTADO_POR_LEGALIZAR_NOT_FOUND, null);
                }
                final Integer idEstadoPorLegalizar = estadoPorLegalizar.getId();

                List<ListaadmitidosOutput> outputs = admitidos.stream()
                                .map(a -> {
                                        AdmitidoDTO dto = new AdmitidoDTO();
                                        dto.setIdCohorte(input.idCohorte());
                                        dto.setIdAspirante(a.getId());
                                        dto.setFechageneracion(today);
                                        if (listaadmitidosService.existsByIdCohorteAndIdAspirante(input.idCohorte(),
                                                        a.getId())) {
                                                throw new DuplicateAdmisionException(a.getId(), input.idCohorte());
                                        }
                                        this.notifyAspirant(
                                                        a.getPersona().getCorreo(),
                                                        EmailTemplates.ASUNTO_ADMITIDO,
                                                        EmailTemplates.cuerpoAdmitido(
                                                                        a.getPersona().getNombres(),
                                                                        a.getPersona().getApellidos(),
                                                                        cohorte.getNombre()));
                                        AdmitidoDTO saved = listaadmitidosService.create(dto);
                                        aspiranteService.updateEstado(a.getId(), idEstadoPorLegalizar);
                                        saved.setAspirante(a);
                                        return map.toOutput(saved);
                                })
                                .toList();

                if (!outputs.isEmpty()) {
                        try {
                                String cohorteNombre = cohorte.getNombre() != null ? cohorte.getNombre() : "Cohorte";
                                List<AspiranteOutput> aspirantesAdmitidos = outputs.stream()
                                                .map(ListaadmitidosOutput::aspirante)
                                                .filter(Objects::nonNull)
                                                .toList();
                                AdministrativoDTO admin = administrativoService.findById(input.idAdministrativo());
                                String directorNombre = admin.getPersona().getNombres() + " "
                                                + admin.getPersona().getApellidos();
                                String directorCorreo = admin.getPersona().getCorreo();
                                byte[] pdf = pdfGeneratorService.generarListaAdmitidos(
                                                cohorteNombre, LocalDateTime.now(), aspirantesAdmitidos,
                                                directorNombre);
                                sesService.sendPdfToDirector(directorCorreo, directorNombre, cohorteNombre, pdf);
                        } catch (Exception e) {
                                logger.error("Error generando PDF o enviando correo al director tras admisión en cohorte {}",
                                                input.idCohorte(), e);
                                throw new RuntimeException(
                                                "Aspirantes admitidos correctamente, pero no se pudo enviar el correo al director.",
                                                e);
                        }
                }

                return outputs;
        }

        private void notifyAspirant(String email, String asunto, String cuerpoHtml) {
                try {
                        sesService.enviarCorreo(email, asunto, cuerpoHtml);
                } catch (Exception ex) {
                        logger.error("[ADMISION_EMAIL] Fallo al enviar correo de admisión a '{}': {}", email,
                                        ex.getMessage(), ex);
                }
        }

        public List<ListaadmitidosOutput> rechazarAspirante(RECHAZAR_ASPIRANTE input) {
                CohorteDTO cohorte = validateAndGetCohorte(input.idCohorte(), input.idAdministrativo());
                List<AspiranteDTO> nuevosAdmitidos = getTopCandidates(
                                input.idCohorte(), input.idAspiranteRechazado(), cohorte.getCupos());
                LocalDate today = LocalDate.now();
                for (AspiranteDTO a : nuevosAdmitidos) {
                        if (!listaadmitidosService.existsByIdCohorteAndIdAspirante(input.idCohorte(), a.getId())) {
                                AdmitidoDTO dto = new AdmitidoDTO();
                                dto.setIdCohorte(input.idCohorte());
                                dto.setIdAspirante(a.getId());
                                dto.setFechageneracion(today);
                                this.notifyAspirant(
                                                a.getPersona().getCorreo(),
                                                EmailTemplates.ASUNTO_RECHAZADO,
                                                EmailTemplates.cuerpoRechazado(
                                                                a.getPersona().getNombres(),
                                                                a.getPersona().getApellidos(),
                                                                cohorte.getNombre()));
                                listaadmitidosService.create(dto);
                        }
                }
                listaadmitidosService.deleteByIdCohorteAndIdAspirante(input.idCohorte(), input.idAspiranteRechazado());
                return listaadmitidosService.findByIdCohorte(input.idCohorte()).stream()
                                .map(map::toOutput)
                                .toList();
        }

        public List<ListaadmitidosOutput> findByIdCohorte(Integer idCohorte) {
                return listaadmitidosService.findByIdCohorte(idCohorte).stream()
                                .map(map::toOutput)
                                .toList();
        }

        public byte[] generarPdfAdmitidos(Integer idCohorte, String directorNombre) {
                CohorteDTO cohorte = cohorteService.findById(idCohorte);
                if (cohorte == null) {
                        throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idCohorte);
                }

                List<AspiranteDTO> admitidos = aspiranteService.findAdmitidosByCohorte(idCohorte);

                List<AspiranteOutput> aspirantesOutput = admitidos.stream()
                                .map(a -> AspiranteOutput.builder()
                                                .id(a.getId())
                                                .puntuacion(a.getPuntuacion())
                                                .persona(a.getPersona() != null ? PersonaOutput.builder()
                                                                .nombres(a.getPersona().getNombres())
                                                                .apellidos(a.getPersona().getApellidos())
                                                                .correo(a.getPersona().getCorreo())
                                                                .build() : null)
                                                .build())
                                .toList();

                return pdfGeneratorService.generarListaAdmitidos(
                                cohorte.getNombre(), LocalDateTime.now(), aspirantesOutput, directorNombre);
        }

        public byte[] generarPdfAdmitidosYActualizarEstados(Integer idCohorte, String directorNombre) {
                // Generate the PDF first (same output as generarPdfAdmitidos)
                byte[] pdf = generarPdfAdmitidos(idCohorte, directorNombre);

                // Resolve 'POR LEGALIZAR' estado
                EstadoDTO estadoPorLegalizar = estadoService.findByTipoAndEntidad("POR LEGALIZAR", "aspirante");
                if (estadoPorLegalizar == null) {
                        estadoPorLegalizar = estadoService.findByTipoAndEntidad("POR LEGALIZAR", "ASPIRANTE");
                }
                if (estadoPorLegalizar == null) {
                        throw new DomainException(ListaadmitidosErrorCode.ESTADO_POR_LEGALIZAR_NOT_FOUND, null);
                }
                final Integer idEstadoPorLegalizar = estadoPorLegalizar.getId();

                // Update all admitted aspirantes to POR LEGALIZAR (best-effort per aspirante)
                List<AspiranteDTO> admitidos = aspiranteService.findAdmitidosByCohorte(idCohorte);
                if (admitidos != null) {
                        for (AspiranteDTO a : admitidos) {
                                try {
                                        aspiranteService.updateEstado(a.getId(), idEstadoPorLegalizar);
                                } catch (Exception ex) {
                                        logger.warn("No se pudo actualizar estado de aspirante id={} : {}", a.getId(),
                                                        ex.getMessage());
                                }
                        }
                }

                return pdf;
        }

        private CohorteDTO validateAndGetCohorte(Integer idCohorte, Integer idAdministrativo) {
                CohorteDTO cohorte = cohorteService.findById(idCohorte);
                if (cohorte == null) {
                        throw new DomainException(CohorteErrorCode.COHORTE_NOT_FOUND, idCohorte);
                }
                AdministrativoDTO admin = administrativoService.findById(idAdministrativo);
                if (admin == null || admin.getCargo() == null) {
                        throw new DomainException(ListaadmitidosErrorCode.DIRECTOR_NO_VALIDO, idAdministrativo);
                }
                if (cohorte.getIdPrograma() == null || admin.getCargo().getIdPrograma() == null
                                || !cohorte.getIdPrograma().equals(admin.getCargo().getIdPrograma())) {
                        throw new DomainException(ListaadmitidosErrorCode.COHORTE_NO_PERTENECE_AL_DIRECTOR, idCohorte);
                }
                return cohorte;
        }

        private List<AspiranteDTO> getTopCandidates(Integer idCohorte, Integer excludeIdAspirante, int cupos) {
                List<AspiranteDTO> todos = aspiranteService.findByCohorte(idCohorte);

                List<AspiranteDTO> sinPuntuacion = todos.stream()
                                .filter(a -> a.getPuntuacion() == null)
                                .toList();

                if (!sinPuntuacion.isEmpty()) {
                        sinPuntuacion.forEach(a -> logger.warn(
                                        "Aspirante id={} omitido: puntuacion null (cohorte {})", a.getId(), idCohorte));
                }

                return todos.stream()
                                .filter(a -> a.getPuntuacion() != null)
                                .filter(a -> excludeIdAspirante == null || !excludeIdAspirante.equals(a.getId()))
                                .sorted(Comparator.comparing(AspiranteDTO::getPuntuacion, Comparator.reverseOrder()))
                                .limit(cupos)
                                .toList();
        }
}
