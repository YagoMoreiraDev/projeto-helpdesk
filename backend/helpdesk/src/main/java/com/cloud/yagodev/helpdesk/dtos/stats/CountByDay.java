package com.cloud.yagodev.helpdesk.dtos.stats;

import java.time.LocalDate;

public record CountByDay(LocalDate day, long total) {
}
