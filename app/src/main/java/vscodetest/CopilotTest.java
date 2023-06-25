package vscodetest;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;

import copilot.CopilotApi;
import copilot.CopilotAuthentication;
import copilot.CopilotConfiguration;
import copilot.CopilotDeviceAuthenticationData;
import copilot.CopilotParameters;
import copilot.FileDataStore;
import copilot.HttpClientWrapper;
import copilot.ICopilotApi;

public class CopilotTest {
    public static void main2(String[] args) throws InterruptedException, ExecutionException, IOException {
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
        ICopilotApi copilotApi = new CopilotApi(copilotConfiguration, copilotAuthentication, wrapper);

//        // Sample Usage 1
//        List<String> completions1 = copilotApi.GetStringCompletionsAsync("public class Em").get();
//        System.out.println("Completions for 'public class Em':");
//        System.out.println(String.join("",completions1));
//        System.out.println("---");

        // Sample Usage 2
        CopilotParameters parameters = new CopilotParameters();
        
        if (false)
        {
            // Q: How do I get specific programming language suggestions? A: Unknown, however you can always add more context to your prompt like 'in Java'.
            parameters.Prompt = """
                public static void writeYaml(String filename, Object object) throws IOException {
                    Yaml yaml = new Yaml();
                    try (FileWriter writer = new FileWriter(filename)) {
                        yaml.dump(object, writer);
                    }
                }

                Write a javadoc for the above java method. Make sure to provide a comprehensive description of the functionality:
                /**
                 * """;
                parameters.MaxTokens = 2000;
                parameters.Temperature = 0;
                parameters.Stop = new String[]{"*/", "\n\n"};
        }
        if (true)
        {
            // Q: How do I get specific programming language suggestions? A: Unknown, however you can always add more context to your prompt like 'in Java'.
            parameters.Prompt = """
                You have been asked to write a Javadoc comment for the following Java method:

                ```java
                public static void writeYaml(String filename, Object object) throws IOException {
                    Yaml yaml = new Yaml();
                    try (FileWriter writer = new FileWriter(filename)) {
                        yaml.dump(object, writer);
                    }
                }
                ```
                Write a comprehensive Javadoc comment for this method that describes what it does, what parameters it takes, and what it returns. Use the @param tag to document the input parameters, and the @return tag to document the return value. Be sure to use proper HTML formatting for the Javadoc comment.
                /**
                 * """;
                parameters.MaxTokens = 2000;
                parameters.Temperature = 0;
                parameters.Stop = new String[]{"*/", "\n\n"};
        }

        var completions2 = copilotApi.GetCompletionsAsync(parameters).get();
        System.out.println("Completions for a method description:");
        System.out.println(String.join("", completions2.stream().map(f -> f.choices[0].Text).toList()));
        System.in.read();
    }
    
}
