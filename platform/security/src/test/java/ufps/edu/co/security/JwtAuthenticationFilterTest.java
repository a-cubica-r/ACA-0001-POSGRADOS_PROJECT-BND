package ufps.edu.co.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ufps.edu.co.auth.contract.AccessTokenValidator;
import ufps.edu.co.auth.model.AuthPrincipal;

/**
 * Pruebas unitarias del filtro JWT: lógica de exclusión de rutas y
 * comportamiento ante tokens válidos, inválidos y ausentes.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AccessTokenValidator accessTokenValidator;

    @Mock
    private SpringSecurityPrincipalAdapter principalAdapter;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    // ─── shouldNotFilter ───────────────────────────────────────────────────

    @Test
    @DisplayName("Ruta /auth/login es excluida del filtro")
    void shouldNotFilter_rutaAuthLogin_retornaTrue() {
        when(request.getServletPath()).thenReturn("/auth/login");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("Ruta /auth/refresh es excluida del filtro")
    void shouldNotFilter_rutaAuthRefresh_retornaTrue() {
        when(request.getServletPath()).thenReturn("/auth/refresh");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("Ruta /swagger-ui es excluida del filtro")
    void shouldNotFilter_rutaSwaggerUi_retornaTrue() {
        when(request.getServletPath()).thenReturn("/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("Ruta /v3/api-docs es excluida del filtro")
    void shouldNotFilter_rutaApiDocs_retornaTrue() {
        when(request.getServletPath()).thenReturn("/v3/api-docs");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("Método OPTIONS es excluido del filtro (preflight CORS)")
    void shouldNotFilter_metodoOptions_retornaTrue() {
        when(request.getServletPath()).thenReturn("/api/dev/endpoint/programa/listall");
        when(request.getMethod()).thenReturn("OPTIONS");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("Ruta protegida con método GET no es excluida")
    void shouldNotFilter_rutaProtegida_retornaFalse() {
        when(request.getServletPath()).thenReturn("/api/dev/endpoint/programa/listall");
        when(request.getMethod()).thenReturn("GET");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("Ruta de aspirantes no es excluida del filtro")
    void shouldNotFilter_rutaAspirante_retornaFalse() {
        when(request.getServletPath()).thenReturn("/api/application/case/aspirantes/datos");
        when(request.getMethod()).thenReturn("GET");
        assertFalse(filter.shouldNotFilter(request));
    }

    // ─── doFilterInternal: sin token ──────────────────────────────────────

    @Test
    @DisplayName("Sin header Authorization: pasa la cadena de filtros sin autenticar")
    void doFilterInternal_sinHeader_pasaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(accessTokenValidator, never()).isValid(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Header Authorization sin prefijo Bearer: pasa sin autenticar")
    void doFilterInternal_headerSinBearer_pasaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(accessTokenValidator, never()).isValid(any());
    }

    // ─── doFilterInternal: token inválido ─────────────────────────────────

    @Test
    @DisplayName("Token inválido: limpia el contexto y pasa la cadena")
    void doFilterInternal_tokenInvalido_limpiaCadenaYPasa() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-malo");
        when(accessTokenValidator.isValid("token-malo")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(accessTokenValidator).isValid("token-malo");
        verify(principalAdapter, never()).toAuthentication(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ─── doFilterInternal: token válido ───────────────────────────────────

    @Test
    @DisplayName("Token válido: llama al principalAdapter y luego al filterChain")
    void doFilterInternal_tokenValido_delegaAlPrincipalAdapter() throws Exception {
        AuthPrincipal principal = new AuthPrincipal(1, "admin@ufps.edu.co", List.of("SUPER_ADMINISTRADOR"));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(accessTokenValidator.isValid("token-valido")).thenReturn(true);
        when(accessTokenValidator.parse("token-valido")).thenReturn(principal);
        when(principalAdapter.toAuthentication(eq(principal), eq(request))).thenReturn(auth);

        filter.doFilterInternal(request, response, filterChain);

        verify(principalAdapter).toAuthentication(principal, request);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("El contexto de seguridad es limpiado después del filterChain (finally)")
    void doFilterInternal_tokenValido_limpiaContextoDespues() throws Exception {
        AuthPrincipal principal = new AuthPrincipal(2, "director@ufps.edu.co", List.of("DIRECTOR_DE_PROGRAMA"));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer token-director");
        when(accessTokenValidator.isValid("token-director")).thenReturn(true);
        when(accessTokenValidator.parse("token-director")).thenReturn(principal);
        when(principalAdapter.toAuthentication(any(), any())).thenReturn(auth);

        filter.doFilterInternal(request, response, filterChain);

        // El finally del filtro limpia el contexto
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
