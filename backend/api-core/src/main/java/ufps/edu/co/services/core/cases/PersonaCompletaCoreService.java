package ufps.edu.co.services.core.cases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.domain.exceptions.DomainException;
import ufps.edu.co.domain.exceptions.errorcodes.PersonaErrorCode;
import ufps.edu.co.maps.specific.PersonaMap;
import ufps.edu.co.records.input.entity.PersonaInput.PERSONA_FULL_CREATE;
import ufps.edu.co.records.input.entity.PersonaInput.UBICACION_OPCIONAL;
import ufps.edu.co.records.output.entity.PersonaOutput;
import ufps.edu.co.rest.dto.DocumentopersonaDTO;
import ufps.edu.co.rest.dto.PersonaDTO;
import ufps.edu.co.rest.dto.UbicacionDTO;
import ufps.edu.co.rest.services.DocumentopersonaService;
import ufps.edu.co.rest.services.PersonaService;
import ufps.edu.co.rest.services.UbicacionService;

/**
 * Alta completa de una persona para el Superadministrador: valida todo antes de
 * insertar y crea, en una sola transacción, las ubicaciones, el documento y la
 * persona. No usa los {@code *CoreService.create} porque estos envuelven
 * cualquier fallo en {@link RuntimeException} y ocultan los errores de
 * integridad al {@code GlobalExceptionHandler}.
 */
@Service
public class PersonaCompletaCoreService {

    private static final String DOCUMENTO_NUMERICO = "\\d+";

    @Autowired
    private PersonaService personaService;

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private DocumentopersonaService documentopersonaService;

    @Autowired
    private PersonaMap personaMap;

    @Transactional
    public PersonaOutput crear(PERSONA_FULL_CREATE in) {
        String correo = in.correo().trim();
        if (personaService.existsByCorreo(correo)) {
            throw new DomainException(PersonaErrorCode.PERSONA_CORREO_DUPLICADO, correo);
        }

        DocumentoPlan documento = validarDocumento(in);
        UbicacionDTO vivienda = validarUbicacion(in.ubicacionvivienda(), "ubicacionvivienda");
        UbicacionDTO nacimiento = validarUbicacion(in.ubicacionnacimiento(), "ubicacionnacimiento");
        UbicacionDTO trabajo = validarUbicacion(in.ubicaciontrabajo(), "ubicaciontrabajo");

        Integer idVivienda = crearUbicacion(vivienda);
        Integer idNacimiento = crearUbicacion(nacimiento);
        Integer idTrabajo = crearUbicacion(trabajo);

        Integer idDocumento = null;
        if (documento != null) {
            Integer idLugarExpedicion = crearUbicacion(documento.lugarExpedicion());
            DocumentopersonaDTO documentoDto = DocumentopersonaDTO.builder()
                    .numerodocumento(documento.numero())
                    .idTipodocumento(documento.idTipodocumento())
                    .idLugarexpedicion(idLugarExpedicion)
                    .build();
            idDocumento = documentopersonaService.create(documentoDto).getId();
        }

        PersonaDTO personaDto = PersonaDTO.builder()
                .nombres(in.nombres())
                .apellidos(in.apellidos())
                .correo(correo)
                .fechanacimiento(in.fechanacimiento())
                .celular(in.celular())
                .telefono(aTextoNull(in.telefono()))
                .idUbicacionvivienda(idVivienda)
                .idUbicacionnacimiento(idNacimiento)
                .idUbicaciontrabajo(idTrabajo)
                .idGenero(in.idGenero())
                .idEstadocivil(in.idEstadocivil())
                .idGrupoetnico(in.idGrupoetnico())
                .idPoblacionindigena(in.idPoblacionindigena())
                .idDiscapacidad(in.idDiscapacidad())
                .idCapacidadexepcional(in.idCapacidadexepcional())
                .idDocumentopersona(idDocumento)
                .promediopregrado(in.promediopregrado())
                .titulopregrado(in.titulopregrado())
                .titulosposgrados(in.titulosposgrados())
                .empresa(in.empresa())
                .experiencialaboral(in.experiencialaboral())
                .egresadoufps(in.egresadoufps())
                .build();

        return personaMap.toOutput(personaService.create(personaDto));
    }

    private DocumentoPlan validarDocumento(PERSONA_FULL_CREATE in) {
        String numero = in.numerodocumento() == null ? "" : in.numerodocumento().trim();
        if (numero.isEmpty()) {
            return null;
        }
        if (!numero.matches(DOCUMENTO_NUMERICO)) {
            throw new DomainException(PersonaErrorCode.PERSONA_DOCUMENTO_INVALIDO, numero);
        }
        try {
            Integer.parseInt(numero);
        } catch (NumberFormatException ex) {
            throw new DomainException(PersonaErrorCode.PERSONA_DOCUMENTO_INVALIDO, numero);
        }
        if (in.idTipodocumento() == null || !esUbicacionCompleta(in.lugarexpedicion())) {
            throw new DomainException(PersonaErrorCode.PERSONA_DOCUMENTO_INCOMPLETO, numero);
        }
        return new DocumentoPlan(numero, in.idTipodocumento(), aDto(in.lugarexpedicion()));
    }

    private UbicacionDTO validarUbicacion(UBICACION_OPCIONAL ubicacion, String campo) {
        if (ubicacion == null) {
            return null;
        }
        boolean tieneDireccion = ubicacion.direccion() != null && !ubicacion.direccion().isBlank();
        boolean tieneMunicipio = ubicacion.idMunicipio() != null;
        if (!tieneDireccion && !tieneMunicipio) {
            return null;
        }
        if (!tieneDireccion || !tieneMunicipio) {
            throw new DomainException(PersonaErrorCode.PERSONA_UBICACION_INCOMPLETA, campo);
        }
        return aDto(ubicacion);
    }

    private boolean esUbicacionCompleta(UBICACION_OPCIONAL ubicacion) {
        return ubicacion != null
                && ubicacion.direccion() != null
                && !ubicacion.direccion().isBlank()
                && ubicacion.idMunicipio() != null;
    }

    private UbicacionDTO aDto(UBICACION_OPCIONAL ubicacion) {
        return UbicacionDTO.builder()
                .direccion(ubicacion.direccion().trim())
                .idMunicipio(ubicacion.idMunicipio())
                .build();
    }

    private Integer crearUbicacion(UbicacionDTO ubicacion) {
        return ubicacion == null ? null : ubicacionService.create(ubicacion).getId();
    }

    private String aTextoNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private record DocumentoPlan(
            String numero,
            Integer idTipodocumento,
            UbicacionDTO lugarExpedicion) {
    }
}
