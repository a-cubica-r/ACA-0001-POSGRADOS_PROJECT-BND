package ufps.edu.co.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.auth.util.PasswordFormats;
import ufps.edu.co.persistence.entities.ClaveEntity;
import ufps.edu.co.persistence.repositories.ClaveRepository;

/**
 * Guarda en una transacción aparte el rehash de una clave heredada. Si el
 * guardado falla, el login puede continuar sin arrastrar la transacción de
 * autenticación a rollback-only.
 */
@Service
public class PasswordRehashService {

    private final ClaveRepository claveRepository;
    private final PasswordHashService passwordHashService;

    public PasswordRehashService(ClaveRepository claveRepository, PasswordHashService passwordHashService) {
        this.claveRepository = claveRepository;
        this.passwordHashService = passwordHashService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rehash(Integer idClave, String rawPassword) {
        if (idClave == null) {
            return;
        }
        ClaveEntity clave = claveRepository.findById(idClave).orElse(null);
        if (clave == null || clave.getValor() == null || clave.getValor().isBlank()) {
            return;
        }
        // Otra petición pudo cambiar la clave entre el matches y este rehash:
        // si ya es BCrypt, el valor nuevo manda y no se pisa.
        if (PasswordFormats.isBcrypt(clave.getValor())) {
            return;
        }
        clave.setValor(passwordHashService.encode(rawPassword));
        claveRepository.saveAndFlush(clave);
    }
}
