package com.github.xdnw.gpt;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.xdnw.gpt.JavaMethodExtractor.MethodInfo;
import com.google.common.base.Function;

import copilot.CopilotApi;
import copilot.CopilotAuthentication;
import copilot.CopilotConfiguration;
import copilot.CopilotDeviceAuthenticationData;
import copilot.CopilotParameters;
import copilot.FileDataStore;
import copilot.HttpClientWrapper;
import copilot.ICopilotApi;

public class CopilotHandler {
    private CopilotApi copilotApi;
    public CopilotHandler() {
        HttpClient httpClient = HttpClient.newBuilder()
//                .authenticator()
                .connectTimeout(Duration.ofSeconds(50))
                .build();
        HttpClientWrapper wrapper = new HttpClientWrapper(httpClient);

        var copilotConfiguration = new CopilotConfiguration();
        // Should be where your application stores files. You can also implement CopilotDev.NET.Api.Contract.IDataStore instead to store it in your own storage (e.g. database).
        var dataStore = new FileDataStore("tokens.json");
        var copilotAuthentication = new CopilotAuthentication(copilotConfiguration, dataStore, wrapper) {
            @Override
            public void OnEnterDeviceCode(CopilotDeviceAuthenticationData data) {
                System.out.println("Open URL " + data.Url + " to enter the device code: " + data.UserCode);
            }
        };

        // Use this ICopilotApi instance in your application e.g. Dependency Injection of ICopilotApi
        this.copilotApi = new CopilotApi(copilotConfiguration, copilotAuthentication, wrapper);
    }

    public String getResponse(String prompt, int tokens, float temperature, String... stop) throws JsonProcessingException, InterruptedException, ExecutionException {
        CopilotParameters parameters = new CopilotParameters();
        
        {
            parameters.Prompt = prompt;
            parameters.MaxTokens = tokens;
            parameters.Temperature = temperature;
            parameters.Stop = stop;
        }

        var completions2 = copilotApi.GetCompletionsAsync(parameters).get();
        return String.join("", completions2.stream().map(f -> f.choices[0].Text).toList());
    }

}
