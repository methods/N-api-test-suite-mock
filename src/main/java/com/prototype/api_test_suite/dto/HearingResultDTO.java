package com.prototype.api_test_suite.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class HearingResultDTO {
    UUID id;
    UUID offenceId;
    UUID caseId;
    String resultLevel;
    String resultLabel;
}
