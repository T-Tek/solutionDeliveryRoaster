package com.solutiondeliveryroaster.solutiondelivery.controller;

import com.solutiondeliveryroaster.solutiondelivery.DTO.TeamRequestDTO;
import com.solutiondeliveryroaster.solutiondelivery.DTO.TeamResponseDTO;
import com.solutiondeliveryroaster.solutiondelivery.entity.Team;
import com.solutiondeliveryroaster.solutiondelivery.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/all-teams")
    @Operation(summary = "Get all teams", description = "Retrieve all teams")
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams() {
        List<TeamResponseDTO> teams = teamService.getAllTeams();
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> getTeamById(@PathVariable Long id) {
        TeamResponseDTO team = teamService.getTeamById(id);
        return new ResponseEntity<>(team, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<TeamResponseDTO>> getAllTeamsPaginated(Pageable pageable) {
        //localhost:8080/teams/page?page=0&size=2&sort=teamName,asc
        Page<TeamResponseDTO> teams = teamService.getAllTeamsPaginated(pageable);
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }

    @GetMapping("/monthly-roster")
    public  Map<LocalDate, Set<Team>> generateMonthlyRoster(){
        Map<LocalDate, Set<Team>> roster = teamService.generateMonthlyRoaster();
        return new ResponseEntity<>(roster, HttpStatus.OK).getBody();
    }

    @PostMapping
    public ResponseEntity<TeamResponseDTO> createTeam(@RequestBody @Valid TeamRequestDTO requestDTO) {
        TeamResponseDTO createdTeam = teamService.createTeam(requestDTO);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> updateTeam(
            @PathVariable Long id,
            @RequestBody @Valid TeamRequestDTO requestDTO
    ) {
        TeamResponseDTO updatedTeam = teamService.updateTeam(id, requestDTO);
        return new ResponseEntity<>(updatedTeam, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
