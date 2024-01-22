package com.solutiondeliveryroaster.solutiondelivery.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "teams")
public class Team {

    //xkxpafoizgstwbaw
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String teamName;

    @ElementCollection
    private Set<String> teamMembers = new LinkedHashSet<>();
}
