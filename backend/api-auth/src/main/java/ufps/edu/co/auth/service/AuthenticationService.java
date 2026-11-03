package ufps.edu.co.auth.service;

import org.springframework.stereotype.Service;

import ufps.edu.co.auth.contract.AuthenticationUseCase;
import ufps.edu.co.auth.contract.TokenIssuer;
import ufps.edu.co.auth.exception.InvalidCredentialsException;
import ufps.edu.co.auth.exception.MissingCredentialsException;
import ufps.edu.co.auth.exception.RoleMismatchException;
import ufps.edu.co.auth.model.AuthPrincipal;
import ufps.edu.co.auth.records.input.LoginInput;
import ufps.edu.co.auth.records.output.LoginOutput;

@Service
public class AuthenticationService implements AuthenticationUseCase {

    private final CredentialService credentialService;
    private final TokenIssuer tokenIssuer;

    public AuthenticationService(CredentialService credentialService, TokenIssuer tokenIssuer) {
        this.credentialService = credentialService;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public LoginOutput login(LoginInput input) {
        return login(input, null);
    }

    @Override
    public LoginOutput login(LoginInput input, String requestedRole) {
        if (input == null) {
            throw new MissingCredentialsException();
        }
        if (input.username() == null || input.username().isBlank()
                || input.password() == null || input.password().isBlank()) {
            throw new MissingCredentialsException();
        }

        String roleToValidate = (requestedRole != null && !requestedRole.isBlank())
                ? requestedRole
                : input.requestedRole();

        AuthPrincipal principal = credentialService.authenticate(input.username(), input.password());
        if (principal == null) {
            throw new InvalidCredentialsException();
        }

        if (roleToValidate != null && !roleToValidate.isBlank()) {
            validateRole(principal, roleToValidate);
        }

        return tokenIssuer.issueTokens(principal);
    }

    private void validateRole(AuthPrincipal principal, String requestedRole) {
        String normalized = RoleNameNormalizer.normalize(requestedRole);
        boolean hasRole = principal.roles().stream()
                .anyMatch(r -> RoleNameNormalizer.normalize(r).equals(normalized));
        if (!hasRole) {
            throw new RoleMismatchException("User does not have the requested role: " + requestedRole);
        }
    }
}
