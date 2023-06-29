package com.github.xdnw.gpt;

import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IGPTHandler {
    String getResponse(String prompt, int tokens, float temperature, String... stop);
}
