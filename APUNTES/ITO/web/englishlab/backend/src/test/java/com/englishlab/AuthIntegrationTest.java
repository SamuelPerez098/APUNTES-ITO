package com.englishlab;

import static org.assertj.core.api.Assertions.assertThat;

import com.englishlab.dto.auth.AuthResponse;
import com.englishlab.dto.auth.LoginRequest;
import com.englishlab.dto.auth.RegisterRequest;
import com.englishlab.dto.user.UserAdminRequest;
import com.englishlab.dto.user.UserAdminResponse;
import com.englishlab.model.entity.RoleName;
import com.englishlab.service.AdminUserService;
import com.englishlab.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de integracion contra PostgreSQL REAL (englishlab_test), levantando todo el
 * contexto Spring. Autocontenida: crea su propio usuario (email aleatorio) en el setup
 * via AuthService, sin depender de IDs ni credenciales del seed.
 *
 * <p>Requiere TEST_DB_USER, TEST_DB_PASSWORD y JWT_SECRET en el entorno.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    /** Contraseña local de prueba (NO es una credencial del seed). */
    private static final String TEST_PASSWORD = "ItTest#2026";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminUserService adminUserService;

    private String email;

    @BeforeEach
    void setUp() {
        email = "it-" + UUID.randomUUID() + "@test.local";
        // Monta el usuario directamente por servicio (sin depender de HTTP ni del seed).
        authService.register(new RegisterRequest("IT Student", email, TEST_PASSWORD));
    }

    private UserAdminResponse createAdmin() {
        String adminEmail = "it-admin-" + UUID.randomUUID() + "@test.local";
        return adminUserService.create(
                new UserAdminRequest("IT Admin", adminEmail, TEST_PASSWORD, RoleName.ADMIN, null, null));
    }

    private String loginToken(String userEmail) {
        ResponseEntity<AuthResponse> response = rest.postForEntity(
                "/api/v1/auth/login", new LoginRequest(userEmail, TEST_PASSWORD), AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().token();
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void loginLogoutAndRevokedTokenIsRejected() {
        // 1. Login -> 200
        ResponseEntity<AuthResponse> login = rest.postForEntity(
                "/api/v1/auth/login",
                new LoginRequest(email, TEST_PASSWORD),
                AuthResponse.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).isNotNull();
        String token = login.getBody().token();
        assertThat(token).isNotBlank();

        // 2. Logout con token valido -> 204
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Void> logout = rest.exchange(
                "/api/v1/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 3. Logout de nuevo con el MISMO token (ya revocado) -> 401
        ResponseEntity<Void> logoutAgain = rest.exchange(
                "/api/v1/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);
        assertThat(logoutAgain.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenOfDeletedUserIsRejectedWith401() {
        UserAdminResponse admin = createAdmin();
        String token = loginToken(admin.email());

        // El token vale mientras el usuario existe.
        ResponseEntity<String> before = rest.exchange(
                "/api/v1/admin/users", HttpMethod.GET, new HttpEntity<>(auth(token)), String.class);
        assertThat(before.getStatusCode()).isEqualTo(HttpStatus.OK);

        // El propio admin se da de baja con su token.
        ResponseEntity<Void> deleted = rest.exchange(
                "/api/v1/admin/users/" + admin.id(), HttpMethod.DELETE,
                new HttpEntity<>(auth(token)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // El mismo token, ya sin usuario en la BD, deja de autenticar.
        ResponseEntity<String> after = rest.exchange(
                "/api/v1/admin/users", HttpMethod.GET, new HttpEntity<>(auth(token)), String.class);
        assertThat(after.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void demotedAdminTokenLosesAdminAccessWith403() {
        UserAdminResponse actor = createAdmin();
        UserAdminResponse target = createAdmin();
        String actorToken = loginToken(actor.email());
        String targetToken = loginToken(target.email());

        // El token emitido como ADMIN funciona antes de la degradación.
        ResponseEntity<String> before = rest.exchange(
                "/api/v1/admin/users", HttpMethod.GET, new HttpEntity<>(auth(targetToken)), String.class);
        assertThat(before.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Otro admin degrada al usuario a STUDENT (password nula: se conserva la actual).
        ResponseEntity<UserAdminResponse> updated = rest.exchange(
                "/api/v1/admin/users/" + target.id(), HttpMethod.PUT,
                new HttpEntity<>(new UserAdminRequest(
                        "IT Admin", target.email(), null, RoleName.STUDENT, null, null), auth(actorToken)),
                UserAdminResponse.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().role()).isEqualTo("STUDENT");

        // El token viejo sigue siendo válido pero el rol se resuelve en la BD -> 403.
        ResponseEntity<String> after = rest.exchange(
                "/api/v1/admin/users", HttpMethod.GET, new HttpEntity<>(auth(targetToken)), String.class);
        assertThat(after.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
