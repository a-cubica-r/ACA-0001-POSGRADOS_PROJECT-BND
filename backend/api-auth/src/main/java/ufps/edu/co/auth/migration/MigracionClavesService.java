package ufps.edu.co.auth.migration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ufps.edu.co.auth.util.PasswordFormats;
import ufps.edu.co.persistence.entities.ClaveEntity;
import ufps.edu.co.persistence.repositories.ClaveRepository;

/**
 * Migra a BCrypt las claves que todavía estén en texto plano. Es idempotente:
 * las filas que ya son BCrypt no se tocan.
 */
@Service
@Profile("migrar-claves")
public class MigracionClavesService {

    private final ClaveRepository claveRepository;
    private final PasswordEncoder passwordEncoder;

    public MigracionClavesService(ClaveRepository claveRepository, PasswordEncoder passwordEncoder) {
        this.claveRepository = claveRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Resultado migrar() {
        List<ClaveEntity> claves = claveRepository.findAll();
        int total = claves.size();
        int yaCifradas = 0;
        int migradas = 0;
        List<Integer> vacias = new ArrayList<>();
        List<Integer> pendientes = new ArrayList<>();

        for (ClaveEntity clave : claves) {
            String valor = clave.getValor();
            if (valor == null || valor.isBlank()) {
                vacias.add(clave.getId());
                pendientes.add(clave.getId());
                continue;
            }
            if (PasswordFormats.isBcrypt(valor)) {
                yaCifradas++;
                continue;
            }
            clave.setValor(passwordEncoder.encode(valor));
            claveRepository.save(clave);
            migradas++;
        }

        claveRepository.flush();

        for (ClaveEntity clave : claveRepository.findAll()) {
            if (clave.getValor() == null || !PasswordFormats.isBcrypt(clave.getValor())) {
                if (!pendientes.contains(clave.getId())) {
                    pendientes.add(clave.getId());
                }
            }
        }

        return new Resultado(total, yaCifradas, migradas, List.copyOf(vacias), List.copyOf(pendientes));
    }

    public record Resultado(
            int total,
            int yaCifradas,
            int migradas,
            List<Integer> vacias,
            List<Integer> pendientes) {
    }
}
