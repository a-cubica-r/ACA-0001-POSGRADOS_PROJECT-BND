package ufps.edu.co.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ufps.edu.co.auth.config.AuthProperties;
import ufps.edu.co.auth.contract.PasswordHashService;
import ufps.edu.co.auth.util.PasswordFormats;

@Service
public class BCryptPasswordHashService implements PasswordHashService {

    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;

    public BCryptPasswordHashService(PasswordEncoder passwordEncoder, AuthProperties properties) {
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (PasswordFormats.isBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        return properties.getAuth().getPassword().isAllowPlainTextLegacy() && rawPassword.equals(storedPassword);
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean needsRehash(String storedPassword) {
        return !PasswordFormats.isBcrypt(storedPassword);
    }
}
