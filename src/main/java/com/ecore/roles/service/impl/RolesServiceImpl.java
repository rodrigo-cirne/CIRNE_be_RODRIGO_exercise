package com.ecore.roles.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.RolesService;
import com.ecore.roles.service.TeamsService;
import com.ecore.roles.service.UsersService;

import lombok.NonNull;

// Removed unused @Log4j2 annotation
@Service
public class RolesServiceImpl implements RolesService {

    public static final String DEFAULT_ROLE = "Developer";

    private final RoleRepository roleRepository;
    private final MembershipRepository membershipRepository;
    private final TeamsService teamsService;
    private final UsersService usersService;

    // Removed superfluous @Autowired annotation
    public RolesServiceImpl(
            RoleRepository roleRepository,
            MembershipRepository membershipRepository,
            TeamsService teamsService,
            UsersService usersService) {
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
        this.teamsService = teamsService;
        this.usersService = usersService;
    }

    @Override
    // renamed method to keep naming convention
    public Role createRole(@NonNull Role r) {
        if (roleRepository.findByName(r.getName()).isPresent()) {
            throw new ResourceExistsException(Role.class);
        }
        return roleRepository.save(r);
    }

    @Override
    // renamed method to keep naming convention
    public Role getRole(@NonNull UUID rid) {
        return roleRepository.findById(rid)
                .orElseThrow(() -> new ResourceNotFoundException(Role.class, rid));
    }

    @Override
    // renamed method to keep naming convention
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    // new method to get role by user id and team id
    @Override
    public Role getRole(UUID userId, UUID teamId) {
        // validate user exists
        User user = usersService.getUser(userId);

        if (Objects.isNull(user)) {
            throw new ResourceNotFoundException(User.class, userId);
        }

        // validate team exists
        Team team = teamsService.getTeam(teamId);

        if (Objects.isNull(team)) {
            throw new ResourceNotFoundException(Team.class, teamId);
        }

        // validate user belongs to team
        if (!team.getTeamMemberIds().contains(userId)) {
            throw new InvalidArgumentException(User.class,
                "The provided user doesn't belong to the provided team.");
        }

        return membershipRepository.findByUserIdAndTeamId(userId, teamId)
                .map(m -> m.getRole())
                .orElseThrow(() -> new ResourceNotFoundException(Membership.class));
    }

    // removed unused getDefaultRole method
}
