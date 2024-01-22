package com.solutiondeliveryroaster.solutiondelivery.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponseDTO {
    private Long id;
    private String teamName;
    private Set<String> teamMembers = new HashSet<>();
}
