package ufps.edu.co.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ufps.edu.co.mapping.EntityMapper;
import ufps.edu.co.persistence.entities.PersonaEntity;
import ufps.edu.co.persistence.repositories.PersonaRepository;
import ufps.edu.co.rest.dto.PersonaDTO;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaRepository repository;

    @Mock
    private EntityMapper<PersonaEntity, PersonaDTO> mapper;

    @InjectMocks
    private PersonaService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    @Test
    void createNormalizaElCorreoAntesDeGuardar() {
        PersonaDTO dto = PersonaDTO.builder().correo("  ADOLFO@Outlook.COM ").build();
        when(mapper.toEntity(any(PersonaDTO.class)))
                .thenAnswer(inv -> PersonaEntity.builder()
                        .correo(((PersonaDTO) inv.getArgument(0)).getCorreo())
                        .build());
        when(repository.save(any(PersonaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(PersonaEntity.class)))
                .thenAnswer(inv -> PersonaDTO.builder()
                        .correo(((PersonaEntity) inv.getArgument(0)).getCorreo())
                        .build());

        service.create(dto);

        ArgumentCaptor<PersonaEntity> captor = ArgumentCaptor.forClass(PersonaEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("adolfo@outlook.com", captor.getValue().getCorreo());
    }

    @Test
    void updateNormalizaElCorreoAntesDeGuardar() {
        PersonaDTO dto = PersonaDTO.builder().correo("  ADOLFO@Outlook.COM ").build();
        when(repository.findById(7)).thenReturn(Optional.of(PersonaEntity.builder().id(7).build()));
        when(mapper.toEntity(any(PersonaDTO.class)))
                .thenAnswer(inv -> PersonaEntity.builder()
                        .correo(((PersonaDTO) inv.getArgument(0)).getCorreo())
                        .build());
        when(repository.save(any(PersonaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(PersonaEntity.class)))
                .thenAnswer(inv -> PersonaDTO.builder()
                        .correo(((PersonaEntity) inv.getArgument(0)).getCorreo())
                        .build());

        service.update(7, dto);

        ArgumentCaptor<PersonaEntity> captor = ArgumentCaptor.forClass(PersonaEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("adolfo@outlook.com", captor.getValue().getCorreo());
    }

    @Test
    void existsByCorreoConsultaConElCorreoNormalizado() {
        when(repository.existsByCorreoIgnoreCase("a@b.co")).thenReturn(true);

        assertTrue(service.existsByCorreo("A@B.CO"));
    }

    @Test
    void updateCorreoGuardaElCorreoNormalizado() {
        PersonaEntity entity = PersonaEntity.builder().id(7).correo("viejo@b.co").build();
        when(repository.findById(7)).thenReturn(Optional.of(entity));

        service.updateCorreo(7, "  ADOLFO@Outlook.COM ");

        ArgumentCaptor<PersonaEntity> captor = ArgumentCaptor.forClass(PersonaEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("adolfo@outlook.com", captor.getValue().getCorreo());
    }
}
