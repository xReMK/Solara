package com.solara.quest_llm.models.lmstudio;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LmDownloadStatusResponse(
        @JsonProperty("job_id") String jobId,
        String status,
        @JsonProperty("total_size_bytes") Long totalSize,
        @JsonProperty("downloaded_bytes") Long downloadedBytes
) {}
