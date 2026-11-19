package com.englishlab;

import static org.assertj.core.api.Assertions.assertThat;

import com.englishlab.dto.activity.ActivityAdminResponse;
import com.englishlab.dto.activity.ActivityRequest;
import com.englishlab.dto.auth.AuthResponse;
import com.englishlab.dto.auth.LoginRequest;
import com.englishlab.dto.auth.RegisterRequest;
import com.englishlab.dto.category.CategoryRequest;
import com.englishlab.dto.category.CategoryResponse;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.teacher.AssignStudentRequest;
import com.englishlab.dto.teacher.StudentResponse;
import com.englishlab.dto.user.UserAdminRequest;
import com.englishlab.dto.user.UserAdminResponse;
import com.englishlab.grading.ActivityType;
import com.englishlab.model.entity.Objective;
import com.englishlab.model.entity.RoleName;
import com.englishlab.repository.ObjectiveRepository;
import com.englishlab.service.AdminUserService;
import com.englishlab.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Pruebas de integración contra PostgreSQL REAL (englishlab_test) para el CRUD admin,
 * categorías, actividades y la vinculación de estudiantes por parte del TEACHER.
 *
 * <p>Autocontenida: crea sus propios usuarios (emails aleatorios) con AdminUserService /
 * AuthService, sin depender de ids del seed salvo los roles.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminTeacherIntegrationTest {

    private static final String TEST_PASSWORD = "ItTest#2026";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectiveRepository objectiveRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ------------------------------------------------------------------ helpers

    private String createUser(String prefix, RoleName role) {
        String email = prefix + "-" + UUID.randomUUID() + "@test.local";
        adminUserService.create(new UserAdminRequest("IT " + prefix, email, TEST_PASSWORD, role, null, null));
        return email;
    }

    private String createStudent() {
        String email = "it-student-" + UUID.randomUUID() + "@test.local";
        authService.register(new RegisterRequest("IT Student", email, TEST_PASSWORD));
        return email;
    }

    private String login(String email) {
        ResponseEntity<AuthResponse> response = rest.postForEntity(
                "/api/v1/auth/login", new LoginRequest(email, TEST_PASSWORD), AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().token();
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private Objective createObjective() {
        Objective objective = new Objective();
        objective.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        objective.setName("Test Objective");
        objective.setCefrLevel("A1");
        objective.setSkill("grammar");
        objective.setDescription("test objective");
        return objectiveRepository.save(objective);
    }

    // ------------------------------------------------------------------ tests

    @Test
    void adminPagesAndFiltersUsers() {
        String adminEmail = createUser("it-admin", RoleName.ADMIN);
        String teacherEmail = createUser("it-teacher", RoleName.TEACHER);
        String studentEmail = createStudent();
        String token = login(adminEmail);

        ResponseEntity<PageResponse<UserAdminResponse>> all = rest.exchange(
                "/api/v1/admin/users?page=0&size=5&sort=id,asc",
                HttpMethod.GET, new HttpEntity<>(auth(token)),
                new ParameterizedTypeReference<>() {});
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).isNotNull();
        assertThat(all.getBody().content()).isNotEmpty();
        assertThat(all.getBody().size()).isEqualTo(5);

        ResponseEntity<PageResponse<UserAdminResponse>> teachers = rest.exchange(
                "/api/v1/admin/users?role=TEACHER&text=" + teacherEmail + "&page=0&size=100",
                HttpMethod.GET, new HttpEntity<>(auth(token)),
                new ParameterizedTypeReference<>() {});
        assertThat(teachers.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(teachers.getBody().content())
                .isNotEmpty()
                .allSatisfy(u -> assertThat(u.role()).isEqualTo("TEACHER"))
                .extracting(UserAdminResponse::email)
                .contains(teacherEmail);

        ResponseEntity<PageResponse<UserAdminResponse>> byText = rest.exchange(
                "/api/v1/admin/users?text=" + studentEmail + "&page=0&size=100",
                HttpMethod.GET, new HttpEntity<>(auth(token)),
                new ParameterizedTypeReference<>() {});
        assertThat(byText.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byText.getBody().content())
                .extracting(UserAdminResponse::email)
                .contains(studentEmail);
    }

    @Test
    void studentCannotAccessAdminUsers() {
        String studentEmail = createStudent();
        String token = login(studentEmail);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/admin/users", HttpMethod.GET, new HttpEntity<>(auth(token)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createEditAndDeleteCategory() {
        String adminEmail = createUser("it-admin", RoleName.ADMIN);
        String token = login(adminEmail);

        String name = "Cat " + UUID.randomUUID();
        ResponseEntity<CategoryResponse> created = rest.exchange(
                "/api/v1/admin/categories", HttpMethod.POST,
                new HttpEntity<>(new CategoryRequest(name, "desc"), auth(token)), CategoryResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        Long id = created.getBody().id();
        assertThat(id).isNotNull();

        ResponseEntity<CategoryResponse> updated = rest.exchange(
                "/api/v1/admin/categories/" + id, HttpMethod.PUT,
                new HttpEntity<>(new CategoryRequest(name + "-2", "desc2"), auth(token)), CategoryResponse.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo(name + "-2");

        ResponseEntity<String> duplicate = rest.exchange(
                "/api/v1/admin/categories", HttpMethod.POST,
                new HttpEntity<>(new CategoryRequest(name + "-2", "dup"), auth(token)), String.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        ResponseEntity<Void> deleted = rest.exchange(
                "/api/v1/admin/categories/" + id, HttpMethod.DELETE, new HttpEntity<>(auth(token)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> deletedAgain = rest.exchange(
                "/api/v1/admin/categories/" + id, HttpMethod.DELETE, new HttpEntity<>(auth(token)), Void.class);
        assertThat(deletedAgain.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createEditAndDeleteActivity() throws Exception {
        String adminEmail = createUser("it-admin", RoleName.ADMIN);
        String token = login(adminEmail);
        Long objectiveId = createObjective().getId();

        ResponseEntity<CategoryResponse> category = rest.exchange(
                "/api/v1/admin/categories", HttpMethod.POST,
                new HttpEntity<>(new CategoryRequest("ActCat " + UUID.randomUUID(), null), auth(token)),
                CategoryResponse.class);
        Long categoryId = category.getBody().id();

        JsonNode validContent = objectMapper.readTree(
                "{\"prompt\":\"Choose the correct word\","
                        + "\"options\":[{\"id\":\"a\",\"text\":\"run\"},{\"id\":\"b\",\"text\":\"runs\"},{\"id\":\"c\",\"text\":\"running\"}],"
                        + "\"answer\":\"b\",\"explanation\":\"third person\"}");
        ActivityRequest request = new ActivityRequest(
                objectiveId, ActivityType.MULTIPLE_CHOICE, "A1", "grammar", "Test activity", validContent, categoryId);

        ResponseEntity<ActivityAdminResponse> created = rest.exchange(
                "/api/v1/content/activities", HttpMethod.POST,
                new HttpEntity<>(request, auth(token)), ActivityAdminResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        Long id = created.getBody().id();
        assertThat(id).isNotNull();
        assertThat(created.getBody().content().get("answer").asText()).isEqualTo("b");

        ResponseEntity<ActivityAdminResponse> updated = rest.exchange(
                "/api/v1/content/activities/" + id, HttpMethod.PUT,
                new HttpEntity<>(new ActivityRequest(
                        objectiveId, ActivityType.MULTIPLE_CHOICE, "A1", "grammar", "Test activity edited",
                        validContent, categoryId), auth(token)),
                ActivityAdminResponse.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().title()).isEqualTo("Test activity edited");

        ResponseEntity<Void> deleted = rest.exchange(
                "/api/v1/content/activities/" + id, HttpMethod.DELETE, new HttpEntity<>(auth(token)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void activityContentValidationAndMissingReferences() throws Exception {
        String adminEmail = createUser("it-admin", RoleName.ADMIN);
        String token = login(adminEmail);
        Long objectiveId = createObjective().getId();

        // MULTIPLE_CHOICE con solo 2 opciones -> 400
        JsonNode invalidContent = objectMapper.readTree(
                "{\"prompt\":\"...\",\"options\":[{\"id\":\"a\",\"text\":\"x\"},{\"id\":\"b\",\"text\":\"y\"}],\"answer\":\"a\"}");
        ResponseEntity<String> invalid = rest.exchange(
                "/api/v1/content/activities", HttpMethod.POST,
                new HttpEntity<>(new ActivityRequest(
                        objectiveId, ActivityType.MULTIPLE_CHOICE, "A1", "grammar", "Bad", invalidContent, null),
                        auth(token)),
                String.class);
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // objectiveId inexistente -> 404
        JsonNode ok = objectMapper.readTree(
                "{\"prompt\":\"...\",\"options\":[{\"id\":\"a\",\"text\":\"x\"},{\"id\":\"b\",\"text\":\"y\"},{\"id\":\"c\",\"text\":\"z\"}],\"answer\":\"a\"}");
        ResponseEntity<String> missingObjective = rest.exchange(
                "/api/v1/content/activities", HttpMethod.POST,
                new HttpEntity<>(new ActivityRequest(
                        999_999_999L, ActivityType.MULTIPLE_CHOICE, "A1", "grammar", "Bad", ok, null), auth(token)),
                String.class);
        assertThat(missingObjective.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void teacherSeesOnlyOwnStudents() {
        String teacherAEmail = createUser("it-teacher-a", RoleName.TEACHER);
        String teacherBEmail = createUser("it-teacher-b", RoleName.TEACHER);
        String studentAEmail = createStudent();
        String studentBEmail = createStudent();

        String tokenA = login(teacherAEmail);
        String tokenB = login(teacherBEmail);

        ResponseEntity<StudentResponse> assignA = rest.exchange(
                "/api/v1/teacher/students", HttpMethod.POST,
                new HttpEntity<>(new AssignStudentRequest(studentAEmail), auth(tokenA)), StudentResponse.class);
        assertThat(assignA.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<StudentResponse> assignB = rest.exchange(
                "/api/v1/teacher/students", HttpMethod.POST,
                new HttpEntity<>(new AssignStudentRequest(studentBEmail), auth(tokenB)), StudentResponse.class);
        assertThat(assignB.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<PageResponse<StudentResponse>> listA = rest.exchange(
                "/api/v1/teacher/students?page=0&size=100", HttpMethod.GET,
                new HttpEntity<>(auth(tokenA)), new ParameterizedTypeReference<>() {});
        assertThat(listA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listA.getBody().content())
                .extracting(StudentResponse::email)
                .contains(studentAEmail)
                .doesNotContain(studentBEmail);
    }

    @Test
    void assignAlreadyAssignedStudentFails() {
        String teacherEmail = createUser("it-teacher", RoleName.TEACHER);
        String studentEmail = createStudent();
        String token = login(teacherEmail);

        ResponseEntity<StudentResponse> first = rest.exchange(
                "/api/v1/teacher/students", HttpMethod.POST,
                new HttpEntity<>(new AssignStudentRequest(studentEmail), auth(token)), StudentResponse.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> second = rest.exchange(
                "/api/v1/teacher/students", HttpMethod.POST,
                new HttpEntity<>(new AssignStudentRequest(studentEmail), auth(token)), String.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void registerDuplicateEmailReturns422() {
        String email = "it-dup-" + UUID.randomUUID() + "@test.local";
        RegisterRequest request = new RegisterRequest("IT Student", email, TEST_PASSWORD);

        ResponseEntity<AuthResponse> first = rest.postForEntity(
                "/api/v1/auth/register", request, AuthResponse.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> second = rest.postForEntity(
                "/api/v1/auth/register", request, String.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}