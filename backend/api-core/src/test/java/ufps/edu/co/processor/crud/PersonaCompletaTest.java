package ufps.edu.co.processor.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import ufps.edu.co.services.core.cases.PersonaCompletaCoreService;

/**
 * D-00 — POST /superadmin/persona/crear-completo: validaciones antes de
 * insertar y creación transaccional de ubicaciones, documento y persona.
 */
@ExtendWith(MockitoExtension.class)
class PersonaCompletaTest {

    private static final String CORREO = "adolfoalejandroar@ufps.edu.co";

    @Mock
    private PersonaService personaService;

    @Mock
    private UbicacionService ubicacionService;

    @Mock
    private DocumentopersonaService documentopersonaService;

    @Mock
    private PersonaMap personaMap;

    @InjectMocks
    private PersonaCompletaCoreService service;

    private static PERSONA_FULL_CREATE.PERSONA_FULL_CREATEBuilder obligatorios() {
        return PERSONA_FULL_CREATE.builder()
                .nombres("Adolfo Alejandro")
                .apellidos("Arenas Ramos")
                .correo(CORREO)
                .celular("3138726612");
    }

    @Test
    @DisplayName("Solo obligatorios: crea la persona sin ubicaciones ni documento")
    void soloObligatorios_creaPersonaSinUbicacionesNiDocumento() {
        PersonaDTO creada = PersonaDTO.builder().id(10).build();
        when(personaService.create(any(PersonaDTO.class))).thenReturn(creada);
        when(personaMap.toOutput(creada)).thenReturn(PersonaOutput.builder().id(10).build());

        PersonaOutput output = service.crear(obligatorios().build());

        assertEquals(10, output.id());
        verify(personaService).existsByCorreo(CORREO);
        verifyNoInteractions(ubicacionService, documentopersonaService);

        ArgumentCaptor<PersonaDTO> captor = ArgumentCaptor.forClass(PersonaDTO.class);
        verify(personaService).create(captor.capture());
        PersonaDTO dto = captor.getValue();
        assertNull(dto.getIdUbicacionvivienda());
        assertNull(dto.getIdUbicacionnacimiento());
        assertNull(dto.getIdUbicaciontrabajo());
        assertNull(dto.getIdDocumentopersona());
        assertNull(dto.getTelefono());
    }

    @Test
    @DisplayName("Correo ya registrado (sin importar mayúsculas): 409 y no crea nada")
    void correoExistente_lanzaDuplicadoYNoCreaNada() {
        when(personaService.existsByCorreo(CORREO)).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.crear(obligatorios().build()));

        assertEquals(PersonaErrorCode.PERSONA_CORREO_DUPLICADO, ex.getCode());
        assertEquals(CORREO, ex.getParam());
        verify(personaService, never()).create(any());
        verifyNoInteractions(ubicacionService, documentopersonaService);
    }

    @Test
    @DisplayName("numerodocumento vacío con idTipodocumento: ignora el documento y el lugar de expedición")
    void numerodocumentoVacio_ignoraDocumento() {
        PersonaDTO creada = PersonaDTO.builder().id(11).build();
        when(personaService.create(any(PersonaDTO.class))).thenReturn(creada);
        when(personaMap.toOutput(creada)).thenReturn(PersonaOutput.builder().id(11).build());

        service.crear(obligatorios()
                .numerodocumento("")
                .idTipodocumento(1)
                .lugarexpedicion(new UBICACION_OPCIONAL("Calle 5 # 6-7", 5))
                .build());

        verifyNoInteractions(ubicacionService, documentopersonaService);
        ArgumentCaptor<PersonaDTO> captor = ArgumentCaptor.forClass(PersonaDTO.class);
        verify(personaService).create(captor.capture());
        assertNull(captor.getValue().getIdDocumentopersona());
    }

    @Test
    @DisplayName("numerodocumento sin idTipodocumento: PERSONA_DOCUMENTO_INCOMPLETO")
    void numerodocumentoSinTipo_lanzaDocumentoIncompleto() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.crear(obligatorios().numerodocumento("123").build()));

        assertEquals(PersonaErrorCode.PERSONA_DOCUMENTO_INCOMPLETO, ex.getCode());
        assertEquals("123", ex.getParam());
        verify(personaService, never()).create(any());
        verifyNoInteractions(ubicacionService, documentopersonaService);
    }

    @Test
    @DisplayName("numerodocumento con tipo pero sin lugar de expedición completo: PERSONA_DOCUMENTO_INCOMPLETO")
    void numerodocumentoSinLugarExpedicionCompleto_lanzaDocumentoIncompleto() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.crear(obligatorios()
                        .numerodocumento("123")
                        .idTipodocumento(1)
                        .lugarexpedicion(new UBICACION_OPCIONAL("Calle 5", null))
                        .build()));

        assertEquals(PersonaErrorCode.PERSONA_DOCUMENTO_INCOMPLETO, ex.getCode());
        assertEquals("123", ex.getParam());
        verify(personaService, never()).create(any());
        verifyNoInteractions(ubicacionService, documentopersonaService);
    }

    @Test
    @DisplayName("numerodocumento no numérico: PERSONA_DOCUMENTO_INVALIDO")
    void numerodocumentoNoNumerico_lanzaDocumentoInvalido() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.crear(obligatorios().numerodocumento("12a").build()));

        assertEquals(PersonaErrorCode.PERSONA_DOCUMENTO_INVALIDO, ex.getCode());
        assertEquals("12a", ex.getParam());
        verify(personaService, never()).create(any());
        verifyNoInteractions(ubicacionService, documentopersonaService);
    }

    @Test
    @DisplayName("Documento completo: crea ubicación de expedición, documento y persona en ese orden")
    void documentoCompleto_creaUbicacionDocumentoYPersona() {
        UbicacionDTO lugarCreado = UbicacionDTO.builder().id(77).build();
        DocumentopersonaDTO documentoCreado = DocumentopersonaDTO.builder().id(88).build();
        PersonaDTO personaCreada = PersonaDTO.builder().id(99).build();

        when(ubicacionService.create(any(UbicacionDTO.class))).thenReturn(lugarCreado);
        when(documentopersonaService.create(any(DocumentopersonaDTO.class))).thenReturn(documentoCreado);
        when(personaService.create(any(PersonaDTO.class))).thenReturn(personaCreada);
        when(personaMap.toOutput(personaCreada)).thenReturn(PersonaOutput.builder().id(99).build());

        PersonaOutput output = service.crear(obligatorios()
                .numerodocumento("123")
                .idTipodocumento(1)
                .lugarexpedicion(new UBICACION_OPCIONAL("  Calle 5 # 6-7  ", 5))
                .build());

        assertEquals(99, output.id());

        InOrder orden = inOrder(ubicacionService, documentopersonaService, personaService);
        ArgumentCaptor<UbicacionDTO> ubicacionCaptor = ArgumentCaptor.forClass(UbicacionDTO.class);
        orden.verify(ubicacionService).create(ubicacionCaptor.capture());
        ArgumentCaptor<DocumentopersonaDTO> documentoCaptor = ArgumentCaptor.forClass(DocumentopersonaDTO.class);
        orden.verify(documentopersonaService).create(documentoCaptor.capture());
        ArgumentCaptor<PersonaDTO> personaCaptor = ArgumentCaptor.forClass(PersonaDTO.class);
        orden.verify(personaService).create(personaCaptor.capture());

        assertEquals("Calle 5 # 6-7", ubicacionCaptor.getValue().getDireccion());
        assertEquals(5, ubicacionCaptor.getValue().getIdMunicipio());
        assertEquals("123", documentoCaptor.getValue().getNumerodocumento());
        assertEquals(1, documentoCaptor.getValue().getIdTipodocumento());
        assertEquals(77, documentoCaptor.getValue().getIdLugarexpedicion());
        assertEquals(88, personaCaptor.getValue().getIdDocumentopersona());
    }

    @Test
    @DisplayName("Tres ubicaciones completas: se crean en orden y cada id queda en su campo de la persona")
    void tresUbicacionesCompletas_seCreanYSeAsignanALaPersona() {
        when(ubicacionService.create(any(UbicacionDTO.class)))
                .thenReturn(UbicacionDTO.builder().id(1).build())
                .thenReturn(UbicacionDTO.builder().id(2).build())
                .thenReturn(UbicacionDTO.builder().id(3).build());
        PersonaDTO personaCreada = PersonaDTO.builder().id(20).build();
        when(personaService.create(any(PersonaDTO.class))).thenReturn(personaCreada);
        when(personaMap.toOutput(personaCreada)).thenReturn(PersonaOutput.builder().id(20).build());

        service.crear(obligatorios()
                .ubicacionvivienda(new UBICACION_OPCIONAL("Casa 1", 10))
                .ubicacionnacimiento(new UBICACION_OPCIONAL("Calle 2", 20))
                .ubicaciontrabajo(new UBICACION_OPCIONAL("Oficina 3", 30))
                .build());

        ArgumentCaptor<UbicacionDTO> ubicacionCaptor = ArgumentCaptor.forClass(UbicacionDTO.class);
        verify(ubicacionService, times(3)).create(ubicacionCaptor.capture());
        List<UbicacionDTO> ubicaciones = ubicacionCaptor.getAllValues();
        assertEquals("Casa 1", ubicaciones.get(0).getDireccion());
        assertEquals(10, ubicaciones.get(0).getIdMunicipio());
        assertEquals("Calle 2", ubicaciones.get(1).getDireccion());
        assertEquals(20, ubicaciones.get(1).getIdMunicipio());
        assertEquals("Oficina 3", ubicaciones.get(2).getDireccion());
        assertEquals(30, ubicaciones.get(2).getIdMunicipio());

        ArgumentCaptor<PersonaDTO> personaCaptor = ArgumentCaptor.forClass(PersonaDTO.class);
        verify(personaService).create(personaCaptor.capture());
        assertEquals(1, personaCaptor.getValue().getIdUbicacionvivienda());
        assertEquals(2, personaCaptor.getValue().getIdUbicacionnacimiento());
        assertEquals(3, personaCaptor.getValue().getIdUbicaciontrabajo());
    }

    @Test
    @DisplayName("Ubicación a medias: PERSONA_UBICACION_INCOMPLETA con el nombre del campo y sin crear nada")
    void ubicacionIncompleta_lanzaErrorConElCampoYNoCreaNada() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.crear(obligatorios()
                        .ubicacionvivienda(new UBICACION_OPCIONAL(null, 5))
                        .build()));

        assertEquals(PersonaErrorCode.PERSONA_UBICACION_INCOMPLETA, ex.getCode());
        assertEquals("ubicacionvivienda", ex.getParam());
        verify(personaService, never()).create(any());
        verifyNoInteractions(ubicacionService, documentopersonaService);
    }

    @Test
    @DisplayName("telefono vacío se guarda como null")
    void telefonoVacio_seGuardaComoNull() {
        PersonaDTO creada = PersonaDTO.builder().id(12).build();
        when(personaService.create(any(PersonaDTO.class))).thenReturn(creada);
        when(personaMap.toOutput(any(PersonaDTO.class))).thenReturn(PersonaOutput.builder().id(12).build());

        service.crear(obligatorios().telefono("").build());

        ArgumentCaptor<PersonaDTO> captor = ArgumentCaptor.forClass(PersonaDTO.class);
        verify(personaService).create(captor.capture());
        assertNull(captor.getValue().getTelefono());
    }
}
