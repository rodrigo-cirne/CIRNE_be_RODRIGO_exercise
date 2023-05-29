package com.ecore.roles.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecore.roles.model.Membership;

// removed superfluous @Repository annotation
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Optional<Membership> findByUserIdAndTeamId(UUID userId, UUID teamId);

    List<Membership> findByRoleId(UUID roleId);
}
