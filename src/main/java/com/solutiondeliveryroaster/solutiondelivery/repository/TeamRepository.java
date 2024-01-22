package com.solutiondeliveryroaster.solutiondelivery.repository;

import com.solutiondeliveryroaster.solutiondelivery.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByTeamName(String teamName);

}
