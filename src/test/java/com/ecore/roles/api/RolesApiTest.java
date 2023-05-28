package com.ecore.roles.api;

import static com.ecore.roles.utils.MockUtils.mockGetTeamById;
import static com.ecore.roles.utils.MockUtils.mockGetUserById;
import static com.ecore.roles.utils.RestAssuredHelper.createMembership;
import static com.ecore.roles.utils.RestAssuredHelper.createRole;
import static com.ecore.roles.utils.RestAssuredHelper.getRole;
import static com.ecore.roles.utils.RestAssuredHelper.getRoles;
import static com.ecore.roles.utils.RestAssuredHelper.sendRequest;
import static com.ecore.roles.utils.TestData.GIANNI_USER_UUID;
import static com.ecore.roles.utils.TestData.ORDINARY_CORAL_LYNX_TEAM_UUID;
import static com.ecore.roles.utils.TestData.UUID_1;
import static com.ecore.roles.utils.TestData.buildDefaultMembership;
import static com.ecore.roles.utils.TestData.buildDeveloperRole;
import static com.ecore.roles.utils.TestData.buildDevopsRole;
import static com.ecore.roles.utils.TestData.buildGianniUser;
import static com.ecore.roles.utils.TestData.buildOrdinaryCoralLynxTeam;
import static com.ecore.roles.utils.TestData.buildProductOwnerRole;
import static com.ecore.roles.utils.TestData.buildTesterRole;
import static com.ecore.roles.utils.TestData.buildUserWithoutMembership;
import static io.restassured.RestAssured.when;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.utils.RestAssuredHelper;
import com.ecore.roles.web.dto.RoleDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// removed public access modifier from test class
class RolesApiTest {

    private final RestTemplate restTemplate;
    private final RoleRepository roleRepository;

    private MockRestServiceServer mockServer;

    @LocalServerPort
    private int port;

    @Autowired
    public RolesApiTest(RestTemplate restTemplate, RoleRepository roleRepository) {
        this.restTemplate = restTemplate;
        this.roleRepository = roleRepository;
    }

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestAssuredHelper.setUp(port);
        Optional<Role> devOpsRole = roleRepository.findByName(buildDevopsRole().getName());
        devOpsRole.ifPresent(roleRepository::delete);
    }

    @Test
    void shouldFailWhenPathDoesNotExist() {
        sendRequest(when()
                .get("/v1/role")
                .then())
                        .validate(404, "Not Found");
    }

    @Test
    void shouldCreateNewRole() {
        Role expectedRole = buildDevopsRole();

        RoleDto actualRole = createRole(expectedRole)
                .statusCode(201)
                .extract().as(RoleDto.class);

        assertThat(actualRole.getName()).isEqualTo(expectedRole.getName());
    }

    @Test
    void shouldFailToCreateNewRoleWhenNull() {
        createRole(null)
                .validate(400, "Bad Request");
    }

    @Test
    void shouldFailToCreateNewRoleWhenMissingName() {
        createRole(Role.builder().build())
                .validate(400, "Bad Request");
    }

    @Test
    void shouldFailToCreateNewRoleWhenBlankName() {
        createRole(Role.builder().name("").build())
                .validate(400, "Bad Request");
    }

    @Test
    void shouldFailToCreateNewRoleWhenNameAlreadyExists() {
        createRole(buildDeveloperRole())
                .validate(400, "Role already exists");
    }

    @Test
    void shouldGetAllRoles() {
        RoleDto[] roles = getRoles()
                .extract().as(RoleDto[].class);

        // chained assertions and changed to more meaningful method
        assertThat(roles)
            .hasSizeGreaterThanOrEqualTo(3)
            .contains(
                RoleDto.fromModel(buildDeveloperRole()),
                RoleDto.fromModel(buildProductOwnerRole()),
                RoleDto.fromModel(buildTesterRole()));
    }

    @Test
    void shouldGetRoleById() {
        Role expectedRole = buildDeveloperRole();

        getRole(expectedRole.getId())
                .statusCode(200)
                .body("name", equalTo(expectedRole.getName()));
    }

    @Test
    void shouldFailToGetRoleById() {
        getRole(UUID_1)
                .validate(404, format("Role %s not found", UUID_1));
    }

    @Test
    void shouldGetRoleByUserIdAndTeamId() {
        Membership expectedMembership = buildDefaultMembership();
        mockGetTeamById(mockServer, ORDINARY_CORAL_LYNX_TEAM_UUID, buildOrdinaryCoralLynxTeam());
        mockGetUserById(mockServer, expectedMembership.getUserId(), buildGianniUser());
        createMembership(expectedMembership)
                .statusCode(201);

        getRole(expectedMembership.getUserId(), expectedMembership.getTeamId())
                .statusCode(200)
                .body("name", equalTo(expectedMembership.getRole().getName()));
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingUserId() {
        getRole(null, ORDINARY_CORAL_LYNX_TEAM_UUID)
                .validate(400, "Bad Request");
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingTeamId() {
        getRole(GIANNI_USER_UUID, null)
                .validate(400, "Bad Request");
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenItDoesNotExist() {
        mockGetUserById(mockServer, GIANNI_USER_UUID, buildGianniUser());
        mockGetTeamById(mockServer, UUID_1, null);
        getRole(GIANNI_USER_UUID, UUID_1)
                .validate(404, format("Team %s not found", UUID_1));
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenUserDoesntBelongToTeam() {
        mockGetUserById(mockServer, UUID_1, buildUserWithoutMembership());
        mockGetTeamById(mockServer, ORDINARY_CORAL_LYNX_TEAM_UUID, buildOrdinaryCoralLynxTeam());
        getRole(UUID_1, ORDINARY_CORAL_LYNX_TEAM_UUID)
                .validate(400, 
                "Invalid 'User' object. The provided user doesn't belong to the provided team.");
    }
}
