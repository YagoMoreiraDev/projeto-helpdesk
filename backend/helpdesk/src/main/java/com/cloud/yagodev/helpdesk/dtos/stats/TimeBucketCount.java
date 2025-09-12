package com.cloud.yagodev.helpdesk.dtos.stats;

import java.time.Instant;

public interface TimeBucketCount {
    Instant getBucket(); // virá do date_trunc(...)
    Long getTotal();
}
