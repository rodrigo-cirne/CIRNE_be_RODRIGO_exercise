package com.ecore.roles.service;

import static com.ecore.roles.utils.TestData.UUID_1;
import static com.ecore.roles.utils.TestData.buildDefaultMembership;
import static com.ecore.roles.utils.TestData.buildDeveloperRole;
import static com.ecore.roles.utils.TestData.buildGianniUser;
import static com.ecore.roles.utils.TestData.buildOrdinaryCoralLynxTeam;
import static com.ecore.roles.utils.TestData.buildUserWithoutMembership;
import static java.lang.String.format;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.RolesServiceImpl;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

    @InjectMocks
    private RolesServiceImpl rolesService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MembershipRepository membershipRepository;

    // removed unused MembershipsService

    // added TeamsService and UsersService mocks
    @Mock
    private TeamsService teamsService;

    @Mock
    private UsersService usersService;

    @Test
    // removed public access modifier
    void shouldCreateRole() {
        Role developerRole = buildDeveloperRole();
        when(roleRepository.save(developerRole)).thenReturn(developerRole);

        Role role = rolesService.createRole(developerRole);

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    // removed public access modifier
    void shouldFailToCreateRoleWhenRoleIsNull() {
        assertThrows(NullPointerException.class,
                () -> rolesService.createRole(null));
    }

    @Test
    // removed public access modifier
    void shouldReturnRoleWhenRoleIdExists() {
        Role developerRole = buildDeveloperRole();
        when(roleRepository.findById(developerRole.getId())).thenReturn(Optional.of(developerRole));

        Role role = rolesService.getRole(developerRole.getId());

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    // removed public access modifier
    void shouldFailToGetRoleWhenRoleIdDoesNotExist() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(UUID_1));

        assertEquals(format("Role %s not found", UUID_1), exception.getMessage());
    }

    // new test cases for new getRole(UUID, UUID) method
    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenUserIdIsNull() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(null, UUID_1));
        assertEquals("User null not found", exception.getMessage());
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenTeamIdIsNull() {
        User user = buildUserWithoutMembership();
        UUID userId = user.getId();
        when(usersService.getUser(userId)).thenReturn(user);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(userId, null));
        assertEquals("Team null not found", exception.getMessage());
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenUserDoesntBelongToTeam() {
        User user = buildUserWithoutMembership();
        UUID userId = user.getId();
        Team team = buildOrdinaryCoralLynxTeam();
        UUID teamId = team.getId();
        when(usersService.getUser(userId)).thenReturn(user);
        when(teamsService.getTeam(teamId)).thenReturn(team);
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> rolesService.getRole(userId, teamId));
        assertEquals("Invalid 'User' object. The provided user doesn't belong to the provided team.", exception.getMessage());
    }

    @Test
    void shouldGetRoleByUserIdAndTeamId() {
        Role developerRole = buildDeveloperRole();
        User user = buildGianniUser();
        Team team = buildOrdinaryCoralLynxTeam();
        Membership membership = buildDefaultMembership();
        when(usersService.getUser(user.getId())).thenReturn(user);
        when(teamsService.getTeam(team.getId())).thenReturn(team);
        when(membershipRepository.findByUserIdAndTeamId(user.getId(), team.getId()))
                .thenReturn(of(membership));
        Role role = rolesService.getRole(user.getId(), team.getId());
        assertNotNull(role);
        assertEquals(developerRole.getName(), role.getName());
    }
}
