package com.ecore.roles.web.rest;

import static com.ecore.roles.web.dto.TeamDto.fromModel;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecore.roles.service.TeamsService;
import com.ecore.roles.web.TeamsApi;
import com.ecore.roles.web.dto.TeamDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/teams")
public class TeamsRestController implements TeamsApi {

    private final TeamsService teamsService;

    @Override
    @PostMapping(
            produces = {"application/json"})
    public ResponseEntity<List<TeamDto>> getTeams() {
        return ResponseEntity
                .status(200)
                .body(teamsService.getTeams().stream()
                        .map(TeamDto::fromModel)
                        .collect(Collectors.toList()));
    }

    @Override
    @GetMapping( // changed to GET Mapping, the appropriate HTTP method for retrieving data
            path = "/{teamId}",
            produces = {"application/json"})
    public ResponseEntity<TeamDto> getTeam(
            @PathVariable UUID teamId) {
        return ResponseEntity
                .status(200)
                .body(fromModel(teamsService.getTeam(teamId)));
    }

}
