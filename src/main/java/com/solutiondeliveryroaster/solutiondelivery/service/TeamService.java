package com.solutiondeliveryroaster.solutiondelivery.service;

import com.solutiondeliveryroaster.solutiondelivery.DTO.TeamRequestDTO;
import com.solutiondeliveryroaster.solutiondelivery.DTO.TeamResponseDTO;
import com.solutiondeliveryroaster.solutiondelivery.entity.Team;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TeamService {
    List<TeamResponseDTO> getAllTeams();
    TeamResponseDTO getTeamById(Long id);
    TeamResponseDTO createTeam(@Valid TeamRequestDTO requestDTO);
    TeamResponseDTO addTeamMemberToTeam(Long teamId, String teamMember);
    TeamResponseDTO updateTeam(Long id, TeamRequestDTO requestDTO);
    void deleteTeam(Long id);
    Page<TeamResponseDTO> getAllTeamsPaginated(Pageable pageable);
    Map<LocalDate, Set<Team>> generateMonthlyRoaster() ;

}

