package com.ecore.roles.service.impl;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.MembershipsService;
import com.ecore.roles.service.TeamsService;

import lombok.NonNull;

// Removed unused @Log4j2 annotation
@Service
public class MembershipsServiceImpl implements MembershipsService {

    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    // added TeamsClient object to validate teams
    private final TeamsService teamsService;

    // Removed superfluous @Autowired annotation
    public MembershipsServiceImpl(
            MembershipRepository membershipRepository,
            RoleRepository roleRepository,
            // added TeamsClient parameter for injection
            TeamsService teamsService) {
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.teamsService = teamsService;
    }

    @Override
    public Membership assignRoleToMembership(@NonNull Membership m) {
        // added team validation login
        Team team = teamsService.getTeam(m.getTeamId());

        if (Objects.isNull(team)) {
            throw new ResourceNotFoundException(Team.class, m.getTeamId());
        }
        
        // added user team member validation
        if (!team.getTeamMemberIds().contains(m.getUserId())) {
            throw new InvalidArgumentException(Membership.class, 
                    "The provided user doesn't belong to the provided team.");
        }

        UUID roleId = ofNullable(m.getRole()).map(Role::getId)
                .orElseThrow(() -> new InvalidArgumentException(Role.class));

        if (membershipRepository.findByUserIdAndTeamId(m.getUserId(), m.getTeamId())
                .isPresent()) {
            throw new ResourceExistsException(Membership.class);
        }

        roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException(Role.class, roleId));
        return membershipRepository.save(m);
    }

    @Override
    public List<Membership> getMemberships(@NonNull UUID rid) {
        return membershipRepository.findByRoleId(rid);
    }
}
