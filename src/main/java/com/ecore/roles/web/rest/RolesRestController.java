package com.ecore.roles.web.rest;

import static com.ecore.roles.web.dto.RoleDto.fromModel;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecore.roles.model.Role;
import com.ecore.roles.service.RolesService;
import com.ecore.roles.web.RolesApi;
import com.ecore.roles.web.dto.RoleDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/roles")
public class RolesRestController implements RolesApi {

    private final RolesService rolesService;

    @Override
    @PostMapping(
            consumes = {"application/json"},
            produces = {"application/json"})
    public ResponseEntity<RoleDto> createRole(
            @Valid @RequestBody RoleDto role) {
        return ResponseEntity
                .status(201) // changed response status to 201 CREATED
                .body(fromModel(rolesService.createRole(role.toModel())));
    }

    @Override
    @GetMapping( // changed to GET Mapping, the appropriate HTTP method for retrieving data
            produces = {"application/json"})
    public ResponseEntity<List<RoleDto>> getRoles() {

        // changed variable name to clarify
        List<Role> roles = rolesService.getRoles();

        // changed to use Stream API
        List<RoleDto> roleDtoList = roles.stream()
                .map(r -> fromModel(r))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(200)
                .body(roleDtoList);
    }

    // new endpoint to search Role by UserId and TeamId
    @Override
    @GetMapping(
            path = "/search",
            produces = {"application/json"})
    public ResponseEntity<RoleDto> getRole(
            @RequestParam @NotNull UUID teamMemberId,
            @RequestParam @NotNull UUID teamId) {
        return ResponseEntity
                .status(200)
                .body(fromModel(rolesService.getRole(teamMemberId, teamId)));
    }

    @Override
    @GetMapping( // changed to GET Mapping, the appropriate HTTP method for retrieving data
            path = "/{roleId}",
            produces = {"application/json"})
    public ResponseEntity<RoleDto> getRole(
            @PathVariable UUID roleId) {
        return ResponseEntity
                .status(200)
                .body(fromModel(rolesService.getRole(roleId)));
    }

}
