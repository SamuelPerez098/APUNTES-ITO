package com.englishlab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.englishlab.config.AuthProperties;
import com.englishlab.domain.MasteryService;
import com.englishlab.domain.ObjectiveState;
import com.englishlab.dto.activity.ActivityRequest;
import com.englishlab.dto.activity.ActivityAdminResponse;
import com.englishlab.dto.assignment.AssignmentCreateRequest;
import com.englishlab.dto.assignment.AssignmentResponse;
import com.englishlab.dto.attempt.AttemptRequest;
import com.englishlab.dto.attempt.AttemptResponse;
import com.englishlab.dto.auth.AuthResponse;
import com.englishlab.dto.auth.ForgotPasswordRequest;
import com.englishlab.dto.auth.LoginRequest;
import com.englishlab.dto.auth.RegisterRequest;
import com.englishlab.dto.objective.ObjectiveResponse;
import com.englishlab.dto.objective.ObjectiveStateResponse;
import com.englishlab.dto.objective.PracticeSessionResponse;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.recommendation.RecommendationResponse;
import com.englishlab.dto.user.UserAdminRequest;
import com.englishlab.dto.user.UserAdminResponse;
import com.englishlab.grading.ActivityType;
import com.englishlab.model.entity.Activity;
import com.englishlab.model.entity.Assignment;
import com.englishlab.model.entity.AssignmentStatus;
import com.englishlab.model.entity.Attempt;
import com.englishlab.model.entity.LearnerSkillState;
import com.englishlab.model.entity.Objective;
import com.englishlab.model.entity.RoleName;
import com.englishlab.repository.ActivityRepository;
import com.englishlab.repository.AssignmentRepository;
import com.englishlab.repository.AttemptRepository;
import com.englishlab.repository.LearnerSkillStateRepository;
import com.englishlab.repository.ObjectiveRepository;
import com.englishlab.service.ActivityService;
import com.englishlab.service.AdminUserService;
import com.englishlab.service.AssignmentService;
import com.englishlab.service.AuthService;
import com.englishlab.service.TeacherStudentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
 * Pruebas de integración del núcleo adaptativo, asignaciones, progreso y preparación de
 * correo. Contra PostgreSQL REAL (englishlab_test). Autocontenida: crea sus propios
 * usuarios/objetivos/actividades, sin depender de ids del seed (salvo roles).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdaptiveIntegrationTest {

    private static final String TEST_PASSWORD = "ItTest#2026";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TeacherStudentService teacherStudentService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private MasteryService masteryService;

    @Autowired
    private ObjectiveRepository objectiveRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private LearnerSkillStateRepository learnerSkillStateRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private AuthProperties authProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ------------------------------------------------------------------ helpers

    private UserAdminResponse createTeacher() {
        String email = "it-teacher-" + UUID.randomUUID() + "@test.local";
        return adminUserService.create(
                new UserAdminRequest("IT Teacher", email, TEST_PASSWORD, RoleName.TEACHER, null, null));
    }

    private AuthResponse registerStudent() {
        String email = "it-student-" + UUID.randomUUID() + "@test.local";
        return authService.register(new RegisterRequest("IT Student", email, TEST_PASSWORD));
    }

    private Long createObjective() {
        Objective objective = new Objective();
        objective.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        objective.setName("Test Objective");
        objective.setCefrLevel("A1");
        objective.setSkill("grammar");
        objective.setDescription("test objective");
        return objectiveRepository.save(objective).getId();
    }

    private Long createMultipleChoiceActivity(Long objectiveId) throws Exception {
        JsonNode content = objectMapper.readTree(
                "{\"prompt\":\"Choose the correct word\","
                        + "\"options\":[{\"id\":\"a\",\"text\":\"run\"},{\"id\":\"b\",\"text\":\"runs\"},{\"id\":\"c\",\"text\":\"running\"}],"
                        + "\"answer\":\"b\",\"explanation\":\"third person\"}");
        return activityService.create(new ActivityRequest(
                objectiveId, ActivityType.MULTIPLE_CHOICE, "A1", "grammar", "Test activity", content, null)).id();
    }

    private String login(String email) {
        ResponseEntity<AuthResponse> response = rest.postForEntity(
                "/api/v1/auth/login", new LoginRequest(email, TEST_PASSWORD), AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().token();
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    /** Estado de un objetivo concreto tal como lo ve el alumno en GET /me/objectives. */
    private ObjectiveStateResponse fetchObjectiveState(String token, Long objectiveId) {
        ResponseEntity<ObjectiveStateResponse[]> response = rest.exchange(
                "/api/v1/me/objectives", HttpMethod.GET,
                new HttpEntity<>(auth(token)), ObjectiveStateResponse[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return Arrays.stream(response.getBody())
                .filter(s -> s.objectiveId().equals(objectiveId))
                .findFirst()
                .orElseThrow();
    }

    // ------------------------------------------------------------------ tests

    @Test
    void correctAttemptPersistsAttemptUpdatesMasteryAndCompletesAssignment() throws Exception {
        UserAdminResponse teacher = createTeacher();
        AuthResponse student = registerStudent();
        teacherStudentService.assign(teacher.id(), student.user().email());

        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);
        AssignmentResponse assignment = assignmentService.create(
                teacher.id(), new AssignmentCreateRequest(student.user().id(), activityId));

        ResponseEntity<AttemptResponse> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "b", assignment.id()), auth(student.token())),
                AttemptResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AttemptResponse body = response.getBody();
        assertThat(body.correct()).isTrue();
        assertThat(body.score()).isEqualTo(1.0);
        assertThat(body.solution()).isEqualTo("b");
        assertThat(body.explanation()).isEqualTo("third person");
        assertThat(body.mastery()).isEqualTo(0.755, within(1e-6));

        LearnerSkillState state = learnerSkillStateRepository
                .findByUserIdAndObjectiveId(student.user().id(), objectiveId).orElseThrow();
        assertThat(state.getMastery().doubleValue()).isEqualTo(0.755, within(1e-6));
        assertThat(state.getAttemptsCount()).isEqualTo(1);
        assertThat(state.getCorrectCount()).isEqualTo(1);

        Assignment saved = assignmentRepository.findById(assignment.id()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        assertThat(saved.getCompletedAt()).isNotNull();
    }

    @Test
    void multipleChoiceSolutionIsAnswerKeyAndCanBeResubmitted() throws Exception {
        AuthResponse student = registerStudent();
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        // intento incorrecto -> solution debe ser la CLAVE (id "b"), no el texto ("runs")
        ResponseEntity<AttemptResponse> wrong = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "a", null), auth(student.token())),
                AttemptResponse.class);
        assertThat(wrong.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(wrong.getBody().correct()).isFalse();
        assertThat(wrong.getBody().solution()).isEqualTo("b");

        // reintento usando ESE valor como answer -> correcto
        ResponseEntity<AttemptResponse> retry = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, wrong.getBody().solution(), null), auth(student.token())),
                AttemptResponse.class);
        assertThat(retry.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retry.getBody().correct()).isTrue();
        assertThat(retry.getBody().solution()).isEqualTo("b");
    }

    @Test
    void attemptWithActivityDifferentFromAssignmentReturns422AndKeepsAssignmentPending() throws Exception {
        UserAdminResponse teacher = createTeacher();
        AuthResponse student = registerStudent();
        teacherStudentService.assign(teacher.id(), student.user().email());

        Long objectiveId = createObjective();
        Long assignedActivityId = createMultipleChoiceActivity(objectiveId);
        Long otherActivityId = createMultipleChoiceActivity(objectiveId);
        AssignmentResponse assignment = assignmentService.create(
                teacher.id(), new AssignmentCreateRequest(student.user().id(), assignedActivityId));

        // Intento "correcto" pero contra otra actividad: no debe completar la asignación.
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(otherActivityId, "b", assignment.id()), auth(student.token())),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("La actividad no corresponde");

        Assignment saved = assignmentRepository.findById(assignment.id()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AssignmentStatus.PENDING);
        assertThat(saved.getCompletedAt()).isNull();
    }

    @Test
    void malformedPathVariableReturns400InsteadOf500() {
        AuthResponse student = registerStudent();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/activities/abc", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void attemptOnForeignAssignmentReturns403() throws Exception {
        UserAdminResponse teacherA = createTeacher();
        UserAdminResponse teacherB = createTeacher();
        AuthResponse studentA = registerStudent();
        AuthResponse studentB = registerStudent();
        teacherStudentService.assign(teacherA.id(), studentA.user().email());
        teacherStudentService.assign(teacherB.id(), studentB.user().email());

        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);
        AssignmentResponse assignment = assignmentService.create(
                teacherB.id(), new AssignmentCreateRequest(studentB.user().id(), activityId));

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "b", assignment.id()), auth(studentA.token())),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void teacherCannotAssignUnlinkedStudentReturns403() throws Exception {
        UserAdminResponse teacher = createTeacher();
        AuthResponse student = registerStudent(); // no vinculado
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);
        String teacherToken = login(teacher.email());

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/teacher/assignments", HttpMethod.POST,
                new HttpEntity<>(new AssignmentCreateRequest(student.user().id(), activityId), auth(teacherToken)),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void meActivityDetailReturnsStudentViewWithoutAnswerKey() throws Exception {
        AuthResponse student = registerStudent();
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/activities/" + activityId, HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).contains("\"id\":" + activityId)
                .contains("\"type\":\"MULTIPLE_CHOICE\"")
                .contains("\"level\":\"A1\"")
                .contains("\"skill\":\"grammar\"")
                .contains("\"title\":\"Test activity\"")
                .contains("\"prompt\"")
                .contains("\"options\"");
        assertThat(body).doesNotContain("\"answer\"", "\"accepted\"", "\"explanation\"");
    }

    @Test
    void meActivityDetailOfMissingActivityReturns404() {
        AuthResponse student = registerStudent();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/activities/99999", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void meActivityDetailIsForbiddenForTeacher() throws Exception {
        UserAdminResponse teacher = createTeacher();
        String teacherToken = login(teacher.email());
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/activities/" + activityId, HttpMethod.GET,
                new HttpEntity<>(auth(teacherToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void myAssignmentsDoesNotExposeAnswerKeys() throws Exception {
        UserAdminResponse teacher = createTeacher();
        AuthResponse student = registerStudent();
        teacherStudentService.assign(teacher.id(), student.user().email());

        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);
        assignmentService.create(teacher.id(), new AssignmentCreateRequest(student.user().id(), activityId));

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/assignments?page=0&size=100", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).contains("prompt").contains("options");
        assertThat(body).doesNotContain("\"answer\"", "\"accepted\"", "\"explanation\"");
    }

    @Test
    void progressOfForeignStudentReturns403() {
        UserAdminResponse teacherA = createTeacher();
        UserAdminResponse teacherB = createTeacher();
        AuthResponse studentA = registerStudent();
        teacherStudentService.assign(teacherA.id(), studentA.user().email());
        String teacherBToken = login(teacherB.email());

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/teacher/students/" + studentA.user().id() + "/progress", HttpMethod.GET,
                new HttpEntity<>(auth(teacherBToken)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void forgotPasswordReturns200AndResetTokenLogIsDisabledByDefault() {
        AuthResponse student = registerStudent();

        ResponseEntity<Void> response = rest.postForEntity(
                "/api/v1/auth/forgot-password",
                new ForgotPasswordRequest(student.user().email()), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // El flag que expone el token en el log está apagado por defecto.
        assertThat(authProperties.logResetToken()).isFalse();
    }

    @Test
    void meObjectivesReturnsExactlyOneRecommended() {
        AuthResponse student = registerStudent();

        ResponseEntity<com.englishlab.dto.objective.ObjectiveStateResponse[]> response = rest.exchange(
                "/api/v1/me/objectives", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())),
                com.englishlab.dto.objective.ObjectiveStateResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var states = response.getBody();
        assertThat(states).isNotEmpty();
        long recommended = Arrays.stream(states)
                .filter(s -> s.state() == ObjectiveState.RECOMMENDED)
                .count();
        assertThat(recommended).isEqualTo(1);
    }

    /**
     * El catalogo del seed tiene 14 objetivos (12 + PASSIV + REPSP). La BD de pruebas es
     * compartida y otros tests crean objetivos auxiliares con ids > 14, por eso se filtra
     * a los ids del seed antes de contar.
     */
    @Test
    void meObjectivesReturnsFourteenSeedObjectivesAndNewB2AreBlocked() {
        AuthResponse student = registerStudent();

        ResponseEntity<ObjectiveStateResponse[]> response = rest.exchange(
                "/api/v1/me/objectives", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), ObjectiveStateResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ObjectiveStateResponse> seed = Arrays.stream(response.getBody())
                .filter(s -> s.objectiveId() <= 14L)
                .toList();
        assertThat(seed).hasSize(14);
        assertThat(seed).extracting(ObjectiveStateResponse::code)
                .contains("PRON", "BE", "COND2", "PASSIV", "REPSP");

        Map<Long, ObjectiveStateResponse> byId = seed.stream()
                .collect(Collectors.toMap(ObjectiveStateResponse::objectiveId, s -> s));
        // Estudiante nuevo: PP (10) y PAST (7) no estan dominados -> los B2 nuevos BLOCKED.
        assertThat(byId.get(13L).state()).isEqualTo(ObjectiveState.BLOCKED);
        assertThat(byId.get(14L).state()).isEqualTo(ObjectiveState.BLOCKED);
        assertThat(byId.get(13L).previousMastery()).isNull();
        assertThat(byId.get(14L).previousMastery()).isNull();
    }

    /**
     * previous_mastery guarda el dominio que habia ANTES de cada intento: null sin sesiones,
     * 0.30 (default) en el primero y el dominio anterior en los siguientes.
     */
    @Test
    void previousMasteryKeepsValueBeforeLastAttempt() {
        AuthResponse student = registerStudent();
        Long objectiveId = 1L; // PRON: actividad 1 del seed, respuesta correcta "b"

        ObjectiveStateResponse before = fetchObjectiveState(student.token(), objectiveId);
        assertThat(before.mastery()).isEqualTo(0.30, within(1e-6));
        assertThat(before.previousMastery()).isNull();

        ResponseEntity<AttemptResponse> first = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(1L, "b", null), auth(student.token())),
                AttemptResponse.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody().mastery()).isEqualTo(0.755, within(1e-6));

        LearnerSkillState state = learnerSkillStateRepository
                .findByUserIdAndObjectiveId(student.user().id(), objectiveId).orElseThrow();
        assertThat(state.getPreviousMastery().doubleValue()).isEqualTo(0.30, within(1e-6));
        assertThat(state.getMastery().doubleValue()).isEqualTo(0.755, within(1e-6));

        ObjectiveStateResponse afterFirst = fetchObjectiveState(student.token(), objectiveId);
        assertThat(afterFirst.mastery()).isEqualTo(0.755, within(1e-6));
        assertThat(afterFirst.previousMastery()).isEqualTo(0.30, within(1e-6));

        // Segundo intento (fallo): previous pasa a ser el dominio anterior (0.755).
        ResponseEntity<AttemptResponse> second = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(1L, "a", null), auth(student.token())),
                AttemptResponse.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        // 0.65 * ((1.0 + 0.0) / 2) + 0.35 * 0.755 = 0.5893
        assertThat(second.getBody().mastery()).isEqualTo(0.5893, within(1e-6));

        ObjectiveStateResponse afterSecond = fetchObjectiveState(student.token(), objectiveId);
        assertThat(afterSecond.previousMastery()).isEqualTo(0.755, within(1e-6));
        assertThat(afterSecond.mastery()).isEqualTo(0.5893, within(1e-6));
    }

    /** El grafo del seed (14 objetivos) sigue siendo aciclico y los B2 cuelgan de PP/PAST. */
    @Test
    void seedPrerequisiteGraphHasFourteenObjectivesAndRemainsAcyclic() {
        List<Objective> seed = objectiveRepository.findAllWithPrerequisites().stream()
                .filter(o -> o.getId() <= 14L)
                .toList();
        assertThat(seed).hasSize(14);

        List<long[]> edges = seed.stream()
                .flatMap(o -> o.getPrerequisiteLinks().stream()
                        .map(link -> new long[]{o.getId(), link.getPrerequisite().getId()}))
                .toList();
        assertThat(masteryService.hasCycle(edges)).isFalse();

        assertThat(seed.stream().filter(o -> o.getId().equals(13L))
                .flatMap(o -> o.getPrerequisiteLinks().stream())
                .map(link -> link.getPrerequisite().getId()))
                .containsExactly(10L);
        assertThat(seed.stream().filter(o -> o.getId().equals(14L))
                .flatMap(o -> o.getPrerequisiteLinks().stream())
                .map(link -> link.getPrerequisite().getId()))
                .containsExactly(7L);
    }

    @Test
    void recommendationNextReturnsWellFormedResponse() {
        AuthResponse student = registerStudent();

        ResponseEntity<RecommendationResponse> response = rest.exchange(
                "/api/v1/recommendations/next", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), RecommendationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        RecommendationResponse body = response.getBody();
        assertThat(body).isNotNull();
        if (body.objectiveId() != null) {
            assertThat(body.activityId()).isNotNull();
            assertThat(body.reason()).isNotBlank();
        } else {
            assertThat(body.message()).isNotBlank();
        }
    }

    @Test
    void objectivesListIsAccessibleToTeacherAndReturnsSeedObjectives() {
        UserAdminResponse teacher = createTeacher();
        String token = login(teacher.email());

        ResponseEntity<PageResponse<ObjectiveResponse>> response = rest.exchange(
                "/api/v1/content/objectives?page=0&size=100",
                HttpMethod.GET, new HttpEntity<>(auth(token)),
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isNotEmpty();
        assertThat(response.getBody().content())
                .allSatisfy(o -> {
                    assertThat(o.id()).isNotNull();
                    assertThat(o.code()).isNotBlank();
                    assertThat(o.name()).isNotBlank();
                    assertThat(o.cefrLevel()).isNotBlank();
                    assertThat(o.skill()).isNotBlank();
                })
                .extracting(ObjectiveResponse::code)
                .contains("PRON", "BE");
    }

    @Test
    void studentCanListActivitiesOfUnlockedObjectiveWithoutAnswerKey() throws Exception {
        AuthResponse student = registerStudent();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/objectives/1/activities?page=0&size=10", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).doesNotContain("\"answer\"", "\"accepted\"", "\"explanation\"", "\"prompt\"");

        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("content").isArray()).isTrue();
        assertThat(root.path("content")).isNotEmpty();
        JsonNode first = root.path("content").get(0);
        assertThat(first.size()).isEqualTo(5);
        assertThat(first.has("id")).isTrue();
        assertThat(first.has("title")).isTrue();
        assertThat(first.has("type")).isTrue();
        assertThat(first.has("level")).isTrue();
        assertThat(first.has("skill")).isTrue();
        assertThat(first.path("id").asLong()).isEqualTo(1L);
        assertThat(first.path("title").asText()).isEqualTo("Choose the correct subject pronoun");
        assertThat(first.path("type").asText()).isEqualTo("MULTIPLE_CHOICE");
        assertThat(first.path("level").asText()).isEqualTo("A1");
        assertThat(first.path("skill").asText()).isEqualTo("grammar");
    }

    @Test
    void activitiesOfMissingObjectiveReturn404() {
        AuthResponse student = registerStudent();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/objectives/99999/activities", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void activitiesOfBlockedObjectiveReturn422() {
        AuthResponse student = registerStudent();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/objectives/12/activities", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("El objetivo está bloqueado por prerrequisitos");
    }

    @Test
    void objectiveActivitiesAreForbiddenForTeacher() {
        UserAdminResponse teacher = createTeacher();
        String teacherToken = login(teacher.email());

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/objectives/1/activities", HttpMethod.GET,
                new HttpEntity<>(auth(teacherToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void practiceSessionReturnsUnrepeatedActivitiesOfUnlockedObjective() {
        AuthResponse student = registerStudent();

        ResponseEntity<PracticeSessionResponse> response = rest.exchange(
                "/api/v1/me/objectives/1/practice-session", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), PracticeSessionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Long> ids = response.getBody().activityIds();
        assertThat(ids).isNotEmpty().doesNotHaveDuplicates();
        Set<Long> objectiveActivityIds = activityRepository.findByObjectiveIdOrderByIdAsc(1L).stream()
                .map(Activity::getId)
                .collect(Collectors.toSet());
        assertThat(objectiveActivityIds).containsAll(ids);
    }

    /**
     * Con el banco del seed (10 actividades por objetivo) el orden es determinista:
     * tras un intento quedan 9 nunca intentadas y una sesión de 5 no puede incluirlo.
     */
    @Test
    void practiceSessionDoesNotRepeatActivityAlreadyAttempted() {
        AuthResponse student = registerStudent();

        ResponseEntity<AttemptResponse> attempt = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(1L, "b", null), auth(student.token())),
                AttemptResponse.class);
        assertThat(attempt.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<PracticeSessionResponse> response = rest.exchange(
                "/api/v1/me/objectives/1/practice-session?size=5", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), PracticeSessionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().activityIds()).hasSize(5).doesNotContain(1L);
    }

    @Test
    void practiceSessionOfMissingObjectiveReturns404() {
        AuthResponse student = registerStudent();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/objectives/99999/practice-session", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void practiceSessionOfBlockedObjectiveReturns422() {
        AuthResponse student = registerStudent();

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/objectives/12/practice-session", HttpMethod.GET,
                new HttpEntity<>(auth(student.token())), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("El objetivo está bloqueado por prerrequisitos");
    }

    @Test
    void practiceSessionIsForbiddenForTeacher() {
        UserAdminResponse teacher = createTeacher();
        String teacherToken = login(teacher.email());

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/me/objectives/1/practice-session", HttpMethod.GET,
                new HttpEntity<>(auth(teacherToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ------------------------------------------------------------ modo RETO (challenge)

    /** Regresión: en modo NORMAL el answer sigue siendo obligatorio (400 con fieldError). */
    @Test
    void normalAttemptWithEmptyAnswerReturns400() throws Exception {
        AuthResponse student = registerStudent();
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "", null), auth(student.token())),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"field\":\"answer\"");
        assertThat(attemptRepository.findByUserIdAndObjectiveId(student.user().id(), objectiveId)).isEmpty();
    }

    @Test
    void challengeCorrectAttemptScoresOneAndRaisesMastery() throws Exception {
        AuthResponse student = registerStudent();
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        ResponseEntity<AttemptResponse> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "b", null, "CHALLENGE"), auth(student.token())),
                AttemptResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AttemptResponse body = response.getBody();
        assertThat(body.correct()).isTrue();
        assertThat(body.score()).isEqualTo(1.0);
        assertThat(body.solution()).isEqualTo("b");
        assertThat(body.explanation()).isEqualTo("third person");
        // Mismo cálculo que NORMAL: 0.65*1.0 + 0.35*0.30 = 0.755
        assertThat(body.mastery()).isEqualTo(0.755, within(1e-6));

        LearnerSkillState state = learnerSkillStateRepository
                .findByUserIdAndObjectiveId(student.user().id(), objectiveId).orElseThrow();
        assertThat(state.getAttemptsCount()).isEqualTo(1);
        assertThat(state.getCorrectCount()).isEqualTo(1);
        assertThat(state.getMastery().doubleValue()).isEqualTo(0.755, within(1e-6));
    }

    @Test
    void challengeWrongAttemptScoresMinusHalfAndIsPersisted() throws Exception {
        AuthResponse student = registerStudent();
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        ResponseEntity<AttemptResponse> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "a", null, "CHALLENGE"), auth(student.token())),
                AttemptResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().correct()).isFalse();
        assertThat(response.getBody().score()).isEqualTo(-0.5);
        // El alumno aprende cuál era la respuesta aunque falle.
        assertThat(response.getBody().solution()).isEqualTo("b");
        assertThat(response.getBody().explanation()).isNotBlank();

        List<Attempt> attempts =
                attemptRepository.findByUserIdAndObjectiveId(student.user().id(), objectiveId);
        assertThat(attempts).hasSize(1);
        Attempt saved = attempts.get(0);
        assertThat(saved.getScore()).isEqualByComparingTo(BigDecimal.valueOf(-0.5));
        assertThat(saved.isCorrect()).isFalse();

        // El -0.5 pesa en el dominio tal cual: 0.65*(-0.5) + 0.35*0.30 = -0.22 -> clamp 0.0
        LearnerSkillState state = learnerSkillStateRepository
                .findByUserIdAndObjectiveId(student.user().id(), objectiveId).orElseThrow();
        assertThat(state.getMastery().doubleValue()).isEqualTo(0.0, within(1e-6));
        assertThat(state.getAttemptsCount()).isEqualTo(1);
        assertThat(state.getCorrectCount()).isZero();
    }

    @Test
    void challengeUnansweredAttemptScoresZeroShowsSolutionAndCounts() throws Exception {
        AuthResponse student = registerStudent();
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        // Intento previo correcto para comprobar que attempts_count se incrementa sobre estado existente.
        ResponseEntity<AttemptResponse> first = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "b", null), auth(student.token())),
                AttemptResponse.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<AttemptResponse> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, null, null, "CHALLENGE"), auth(student.token())),
                AttemptResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AttemptResponse body = response.getBody();
        assertThat(body.correct()).isFalse();
        assertThat(body.score()).isEqualTo(0.0);
        // Aunque no responda, recibe solución y explicación.
        assertThat(body.solution()).isEqualTo("b");
        assertThat(body.explanation()).isNotBlank();

        LearnerSkillState state = learnerSkillStateRepository
                .findByUserIdAndObjectiveId(student.user().id(), objectiveId).orElseThrow();
        assertThat(state.getAttemptsCount()).isEqualTo(2);
        assertThat(state.getCorrectCount()).isEqualTo(1);

        Attempt unanswered = attemptRepository
                .findByUserIdAndObjectiveId(student.user().id(), objectiveId).stream()
                .filter(a -> a.getUserAnswer().isEmpty())
                .findFirst()
                .orElseThrow();
        assertThat(unanswered.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(unanswered.isCorrect()).isFalse();
    }

    @Test
    void unknownModeReturns400WithFieldError() throws Exception {
        AuthResponse student = registerStudent();
        Long objectiveId = createObjective();
        Long activityId = createMultipleChoiceActivity(objectiveId);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/attempts", HttpMethod.POST,
                new HttpEntity<>(new AttemptRequest(activityId, "b", null, "LOCO"), auth(student.token())),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("\"field\":\"mode\"");
        assertThat(attemptRepository.findByUserIdAndObjectiveId(student.user().id(), objectiveId)).isEmpty();
    }
}