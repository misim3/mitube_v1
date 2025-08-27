package com.misim.mitube_v1.api;

public record PutProgressRequest(Long positionMs, Long durationMs, Long clientTsMs) {

}
