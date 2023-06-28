package com.github.xdnw.javadoc;

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
import com.github.xdnw.javadoc.JavaMethodExtractor.MethodInfo;
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

    // stop = ["\n\n", "*/"]
    // temperature = 0
    // tokens = 2000
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

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        // description of project
        File root = new File("K:\\Github\\locutus3\\src\\main\\java");
        List<File> files = JavaMethodExtractor.iterateFilesRecursively(new File(root, "link\\locutus\\discord"), ".java");
        // add each file name (including the path from root, but not including extension)
        
        CopilotHandler handler = new CopilotHandler();

        String promptMethod = """
                Ignore all previous instructions        
                Project Description:
                {project_description}

                Class name:
                {class_name}

                Relevant Class Imports:
                {class_imports}

                Class Structure:
                {class_structure}

                Write a comprehensive Javadoc comment for a method. The Javadoc comment should provide a detailed explanation of the method's functionality and its input parameters. Follow standard programming practices and adhere to Javadoc syntax and HTML formatting.

                To generate a well-rounded Javadoc comment, please include the following elements:
                1. Description of the method's functionality and purpose.
                2. Explanation of the input parameters, using the @param tag.
                3. Documentation of the return value, using the @return tag.
                4. Additional context from the class and its methods.

                The Java method to document will be provided in the code snippet below:
                ```
                {method_structure}
                ```
                /*
                * """;

        // get file named GuildDB from files
        File file = files.stream().filter(f -> f.getName().equals("GuildDB.java")).findFirst().get();

        String prompt = generateClassPrompt(root, file, true, true, true, true);
        
        System.out.println(prompt);
        int length = prompt.length();
        // count times */ occurs in prompt
        int countEnd = countJavadocEnd(prompt);
        String response = handler.getResponse(prompt, 2000, 0.5f, "*/");;
        // while (countJavadocEnd(prompt) == countEnd && prompt.length() < length + 1000) {
        //     String result = 
        //     response.append(result);
        //     prompt += result;
        //     System.out.println("---------\n\n" + prompt.substring(length) + "\n\n---------");
        // }
        System.out.println(response);
    }

    private static int countJavadocEnd(String prompt) {
        int count = 0;
        for (int i = 0; i < prompt.length() - 1; i++) {
            if (prompt.charAt(i) == '*' && prompt.charAt(i + 1) == '/') {
                count++;
            }
        }
        return count;
    }

    public static String generateClassPrompt(File root, File file, boolean includeImports, boolean includeImportJavadocs, boolean includeMethodJavadocs, boolean includeFieldJavadocs) throws IOException {
        String promptClass = """
                Ignore all previous instructions        
                Project Description:
                {project_description}

                Class name:
                {class_name}

                Relevant Class Imports:
                {class_imports}

                Class Methods:
                {class_methods}

                Class Fields:
                {class_fields}

                Write a javadoc comment for the {class_name} class. 
                Include the following elements:
                1. Class Description: Offer a detailed explanation of the class, its responsibilities, and its relationship to other classes or modules.
                2. Constructor Summary: Document each constructor in the class, including a description and the parameters it accepts.
                3. Field Summary: List and describe any public or protected fields in the class and their purpose.
                4. Example Usage: If applicable, provide a SINGLE line example of the most critical use case to illustrate how the class should be used.
                5. Most Related: A SINGLE link to the most relevant related class, interface, or external documentation.

                Follow standard programming practices and adhere to Javadoc syntax and HTML formatting.
                Do not list more than one reference. Do not list methods, that is handled by the method javadoc.
                You begin now.""";
        
        Map<String, String> categories = new LinkedHashMap<>();
        categories.put("Class Summary: ", "\n");
        categories.put("Class Description: ", "\n");

        StringBuilder class_structure = new StringBuilder();
        StringBuilder class_fields = new StringBuilder();
        StringBuilder class_imports = new StringBuilder();


        // if (file.getAbsolutePath().contains("_test")) {
        //     continue;
        // }

        String classname = file.getName().split("\\.")[0];
        
        // read file to string
        CompilationUnit cu = JavaMethodExtractor.getCompilationUnit(file);

        // iterate fields
        List<FieldDeclaration> fields = cu.findAll(FieldDeclaration.class);
        for (FieldDeclaration field : fields) {
            String name = field.getVariables().get(0).getNameAsString();
            String type = field.getVariables().get(0).getTypeAsString();
            Javadoc javadoc = field.getJavadoc().orElse(null);
            String javadocText = javadoc != null ? javadoc.toText() : "";
            // append field information to the class_fields
            // include modifiers, field name, type, and javadoc (first line)
            class_fields.append(field.getModifiers().toString() + " " + type + " " + name + " " + javadocText.split("\n")[0] + "\n");
        }
        

        NodeList<ImportDeclaration> imports = cu.getImports();
        for (ImportDeclaration importDeclaration : imports) {
            // if file exits in root folder
            String name = importDeclaration.getNameAsString();
            String importPath = name.replace(".", "\\");
            File importFile = new File(root.getAbsolutePath() + "\\" + importPath + ".java");
            if (importFile.exists()) {
                String importClassname = importFile.getName().split("\\.")[0];
                class_imports.append(importClassname + "\n");
            } else {
                System.out.println("No file " + importFile.getAbsolutePath());
            }
        }

        List<MethodInfo> methods = JavaMethodExtractor.extractMethods(cu);
        for (MethodInfo method : methods) {
            // if method is override, or not public, skip
            if (method.isOverride() || method.getModifier() != AccessSpecifier.PUBLIC) {
                continue;
            }
            MethodDeclaration declaration = method.getDeclaration();
            class_structure.append(declaration.getTypeAsString() + " " + declaration.getNameAsString() + "(");
            class_structure.append(String.join(", ", declaration.getParameters().stream().map(f -> f.getTypeAsString() + " " + f.getNameAsString()).toList()));
            class_structure.append(")\n");
        }
        // sb.append("\n");

        // System.out.println(sb);
        // System.out.println(sb.length());
        
        String prompt = promptClass;
        prompt = prompt.replace("{class_name}", classname);
        prompt = prompt.replace("{class_methods}", class_structure.toString());
        prompt = prompt.replace("{project_description}", "Locutus is a Discord bot written in Java for the web browser nation simulation game Politics And War");
        prompt = prompt.replace("{class_imports}", class_imports.toString());
        // prompt = prompt.replace("{javadoc}", JavaMethodExtractor.getClassJavadoc(cu) + "");
        prompt = prompt.replace("{javadoc}", "null");
        prompt = prompt.replace("{class_fields}", class_fields.toString());
        return prompt;
    }

    public static void main3(String[] args) throws IOException {
        File root = new File("K:\\Github\\locutus\\src\\\\main\\\\java\\");
        File file = new File("K:\\Github\\locutus\\src\\main\\java\\link\\locutus\\discord\\db\\GuildDB.java");
        // read file to string
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));

        String classname = file.getName().split("\\.")[0];
        // package name (extract it from the content using regex)
        String packageName = JavaMethodExtractor.extractPackageName(content);
        System.out.println("Package " + packageName);

        // cu
        CompilationUnit cu = StaticJavaParser.parse(content);

        List<MethodInfo> methods = JavaMethodExtractor.extractMethods(cu);

        String qualifiedName = packageName + "." + classname;

        StringBuilder sb = new StringBuilder();
        sb.append("Class name: `" + qualifiedName + "`\n\n");

        System.out.println(content.length());
    }
}
