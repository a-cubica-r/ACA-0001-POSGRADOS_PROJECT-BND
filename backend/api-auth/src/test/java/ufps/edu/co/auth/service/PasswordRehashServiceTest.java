package ufps.edu.co.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.persistence.entities.ClaveEntity;
import ufps.edu.co.persistence.repositories.ClaveRepository;

@ExtendWith(MockitoExtension.class)
class PasswordRehashServiceTest {

    @Mock
    private ClaveRepository claveRepository;

    @Mock
    private PasswordHashService passwordHashService;

    private PasswordRehashService service;

    @BeforeEach
    void setUp() {
        service = new PasswordRehashService(claveRepository, passwordHashService);
    }

    @Test
    @DisplayName("Clave en texto plano: guarda el hash")
    void textoPlano_guardaHash() {
        ClaveEntity clave = ClaveEntity.builder().id(1).valor("Clave123*").build();
        when(claveRepository.findById(1)).thenReturn(Optional.of(clave));
        when(passwordHashService.encode("Clave123*")).thenReturn("hash-bcrypt");

        service.rehash(1, "Clave123*");

        ArgumentCaptor<ClaveEntity> captor = ArgumentCaptor.forClass(ClaveEntity.class);
        verify(claveRepository).saveAndFlush(captor.capture());
        assertEquals("hash-bcrypt", captor.getValue().getValor());
    }

    @Test
    @DisplayName("Si otra petición ya la cifró, no se pisa el valor nuevo")
    void yaEsBcrypt_noGuarda() {
        ClaveEntity clave = ClaveEntity.builder()
                .id(1)
                .valor(new BCryptPasswordEncoder().encode("Clave123*"))
                .build();
        when(claveRepository.findById(1)).thenReturn(Optional.of(clave));

        service.rehash(1, "Clave123*");

        verifyNoInteractions(passwordHashService);
        verify(claveRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Clave inexistente o vacía: no hace nada")
    void inexistenteOVacia_noGuarda() {
        when(claveRepository.findById(1)).thenReturn(Optional.empty());
        service.rehash(1, "Clave123*");

        ClaveEntity vacia = ClaveEntity.builder().id(2).valor("").build();
        when(claveRepository.findById(2)).thenReturn(Optional.of(vacia));
        service.rehash(2, "Clave123*");

        verify(claveRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Id nulo: no toca el repositorio")
    void idNulo_noHaceNada() {
        service.rehash(null, "Clave123*");

        verifyNoInteractions(claveRepository, passwordHashService);
    }
}
