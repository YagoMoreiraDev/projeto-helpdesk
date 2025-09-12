package com.cloud.yagodev.helpdesk.dtos.stats;

import java.util.UUID;

public record CountByTech(UUID tecnicoId, String tecnicoNome, long total) {
}
