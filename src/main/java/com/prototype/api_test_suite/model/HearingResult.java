package com.prototype.api_test_suite.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hearing_result")
public class HearingResult {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "offence_id", columnDefinition = "UUID")
    private UUID offenceId;

    @Column(name = "case_id", columnDefinition = "UUID")
    private UUID caseId;

    @Column(name = "result_level")
    private String resultLevel;

    @Column(name = "result_label")
    private String resultLabel;

}
