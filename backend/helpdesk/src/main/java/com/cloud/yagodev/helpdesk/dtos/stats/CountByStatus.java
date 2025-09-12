package com.cloud.yagodev.helpdesk.dtos.stats;

import com.cloud.yagodev.helpdesk.enums.StatusChamado;

public record CountByStatus(StatusChamado status, long total) {
}
