package ufps.edu.co.auth.service;

import org.springframework.stereotype.Service;

import ufps.edu.co.auth.contract.RefreshTokenUseCase;
import ufps.edu.co.auth.contract.TokenIssuer;
import ufps.edu.co.auth.exception.MissingCredentialsException;
import ufps.edu.co.auth.records.output.LoginOutput;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenIssuer tokenIssuer;

    public RefreshTokenService(TokenIssuer tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public LoginOutput refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new MissingCredentialsException();
        }
        return tokenIssuer.refreshTokens(refreshToken);
    }
}
