package com.solutiondeliveryroaster.solutiondelivery.DTO;

import jakarta.persistence.ElementCollection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequestDTO {

    @NotBlank(message = "Team name is required")
    @NotNull(message = "Team name can not be empty")
    private String teamName;

    @ElementCollection
    @NotNull(message = "Member name cannot be empty")
    @NotEmpty(message = "Team members cannot be empty")
    private Set<String> teamMembers = new HashSet<>();

}
