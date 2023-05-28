package com.ecore.roles.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecore.roles.client.TeamsClient;
import com.ecore.roles.client.model.Team;
import com.ecore.roles.service.TeamsService;

@Service
public class TeamsServiceImpl implements TeamsService {

    private final TeamsClient teamsClient;

    // Removed superfluous @Autowired annotation
    public TeamsServiceImpl(TeamsClient teamsClient) {
        this.teamsClient = teamsClient;
    }

    public Team getTeam(UUID id) {
        return teamsClient.getTeam(id).getBody();
    }

    public List<Team> getTeams() {
        return teamsClient.getTeams().getBody();
    }
}
