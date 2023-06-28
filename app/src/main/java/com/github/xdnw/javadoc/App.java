/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.xdnw.javadoc;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;

import copilot.CopilotApi;
import copilot.CopilotAuthentication;
import copilot.CopilotConfiguration;
import copilot.CopilotDeviceAuthenticationData;
import copilot.FileDataStore;
import copilot.HttpClientWrapper;
import copilot.ICopilotApi;

public class App {

    private CopilotApi copilotApi;


    public App() {
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

    public static void main(String[] args) throws IOException {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        // ../Locutus
        File rootDir = new File("..\\Locutus\\src\\main\\java");
        // ensure dir exists
        if (!rootDir.exists()) {
            System.out.println("Directory does not exist: " + rootDir.getAbsolutePath());
            return;
        }
        // print succes
        System.out.println("Directory exists: " + rootDir.getAbsolutePath());

        // iterate java files
        AtomicLong count = new AtomicLong();
        List<File> files = JavaMethodExtractor.iterateFilesRecursively(rootDir, ".java");
        Map<File, String> fileContents = new HashMap<>();
        Map<File, List<JavaMethodExtractor.MethodInfo>> fileMethods = new HashMap<>();
        Map<File, List<JavaMethodExtractor.MethodInfo>> missingJavaDocs = new HashMap<>();
        Map<String, Integer> mostCalledMethods = new HashMap<>();

        for (File file : files) {
            System.out.println("Processing file: " + file.getAbsolutePath());
            count.incrementAndGet();

            String content = Files.readString(file.toPath());
            fileContents.put(file, content);
            // extract all methods
            // cu
            CompilationUnit cu = StaticJavaParser.parse(content);
            List<JavaMethodExtractor.MethodInfo> methods = JavaMethodExtractor.extractMethods(cu);
            fileMethods.put(file, methods);

            // iterate methods
            Set<String> methodsInClass = methods.stream().map(JavaMethodExtractor.MethodInfo::getName).collect(Collectors.toSet());
            for (JavaMethodExtractor.MethodInfo method : methods) {
                // check if method has javadoc and is public modifier
                if (method.getJavadoc() == null && method.getModifier() == AccessSpecifier.PUBLIC && !method.isOverride()) {
                    // add to missing javadoc
                    missingJavaDocs.putIfAbsent(file, new ArrayList<>());
                    missingJavaDocs.get(file).add(method);
                }

                method.getMethodCalls().forEach(methodCall -> {
                    if (methodsInClass.contains(methodCall)) {
                        return;
                    }
                    mostCalledMethods.putIfAbsent(methodCall, 0);
                    mostCalledMethods.put(methodCall, mostCalledMethods.get(methodCall) + 1);
                });
            }

            break;
        }
        // missingJavaDocs remove if no calls
        missingJavaDocs.values().forEach(methods -> methods.removeIf(method -> !mostCalledMethods.containsKey(method.getName())));


        System.out.println("Total files: " + count.get());
        // print total methods
        System.out.println("Total methods: " + fileMethods.values().stream().mapToInt(List::size).sum());
        // print total files missing javadocs
        System.out.println("Total files missing javadocs: " + missingJavaDocs.size());
        // print methods missing javadocs
        System.out.println("Total methods missing javadocs: " + missingJavaDocs.values().stream().mapToInt(List::size).sum());

        Map<String, Integer> mostCalledMethodRanking = new HashMap<>();
        List<Map.Entry<String, Integer>> sortedMostCalledMethods = new ArrayList<>(mostCalledMethods.entrySet());
        sortedMostCalledMethods.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        for (int i = 0; i < sortedMostCalledMethods.size(); i++) {
            Map.Entry<String, Integer> entry = sortedMostCalledMethods.get(i);
            mostCalledMethodRanking.put(entry.getKey(), i);
        }

        // iterate missingJavaDocs
        System.out.println("Missing javadocs: ");
        for (Map.Entry<File, List<JavaMethodExtractor.MethodInfo>> entry : missingJavaDocs.entrySet()) {
            File file = entry.getKey();
            List<JavaMethodExtractor.MethodInfo> methods = entry.getValue();
            System.out.println("File: " + file.getAbsolutePath());
            for (JavaMethodExtractor.MethodInfo method : methods) {
                int callRanking = mostCalledMethodRanking.getOrDefault(method.getName(), 0);
                int calls = mostCalledMethods.getOrDefault(method.getName(), 0);
                if (calls == 0) {
                    continue;
                }
                System.out.println("Method: " + method.getName() + " calls: " + calls + " ranking: #" + callRanking);
            }
        }
    }


}