package com.floodrescue.module.team.service;

import com.floodrescue.module.team.dto.request.CreateTeamRequest;
import com.floodrescue.module.team.entity.TeamEntity;

import java.util.List;

public interface TeamService {

    TeamEntity createTeam(CreateTeamRequest request);

    List<TeamEntity> getAllTeams();

    TeamEntity getTeamById(Long id);
}