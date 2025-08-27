package com.misim.mitube_v1;

public record ProgressUpsert(Long userId, Long videoId, Long positionMs, Long durationMs,
                             Long clientTs) {

}
