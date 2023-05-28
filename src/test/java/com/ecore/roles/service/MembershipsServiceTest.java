package com.ecore.roles.service;

import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.MembershipsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ecore.roles.utils.TestData.buildDefaultMembership;
import static com.ecore.roles.utils.TestData.buildDeveloperRole;
import static com.ecore.roles.utils.TestData.buildOrdinaryCoralLynxTeam;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MembershipsServiceTest {

    @InjectMocks
    private MembershipsServiceImpl membershipsService;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UsersService usersService;
    @Mock
    private TeamsService teamsService;

    @Test
    // removed public access modifier
    void shouldCreateMembership() {
        Membership expectedMembership = buildDefaultMembership();
        when(roleRepository.findById(expectedMembership.getRole().getId()))
                .thenReturn(Optional.ofNullable(buildDeveloperRole()));
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.empty());
        when(membershipRepository
                .save(expectedMembership))
                        .thenReturn(expectedMembership);
        // added mock return to pass new Team validation
        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(buildOrdinaryCoralLynxTeam());

        Membership actualMembership = membershipsService.assignRoleToMembership(expectedMembership);

        assertNotNull(actualMembership);
        assertEquals(actualMembership, expectedMembership);
        verify(roleRepository).findById(expectedMembership.getRole().getId());
    }

    @Test
    // removed public access modifier
    void shouldFailToCreateMembershipWhenMembershipsIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.assignRoleToMembership(null));
    }

    @Test
    // removed public access modifier
    void shouldFailToCreateMembershipWhenItExists() {
        Membership expectedMembership = buildDefaultMembership();
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.of(expectedMembership));
        // added mock return to pass new Team validation
        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(buildOrdinaryCoralLynxTeam());

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals("Membership already exists", exception.getMessage());
        verify(roleRepository, times(0)).getById(any());
        verify(usersService, times(0)).getUser(any());
        verify(teamsService, times(1)).getTeam(any());
    }

    @Test
    // removed public access modifier
    void shouldFailToCreateMembershipWhenItHasInvalidRole() {
        Membership expectedMembership = buildDefaultMembership();
        expectedMembership.setRole(null);
        // added mock return to pass new Team validation
        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(buildOrdinaryCoralLynxTeam());

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals("Invalid 'Role' object", exception.getMessage());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(roleRepository, times(0)).getById(any());
        verify(usersService, times(0)).getUser(any());
        verify(teamsService, times(1)).getTeam(any());
    }

    @Test
    // removed public access modifier
    void shouldFailToGetMembershipsWhenRoleIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.getMemberships(null));
    }
}
