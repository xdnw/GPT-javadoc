package com.github.xdnw.gpt;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.xdnw.commit.AutoCommit;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        // AutoCommit.generateCommitMessage();

        ChatGPTWebHandler handler = new ChatGPTWebHandler();
        String token = "";
        String cf_clearance = "";
        String user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.51";
        handler.login(token, cf_clearance, user_agent);

        String prompt = "Hello, how are you?";
        String response = handler.getResponse(prompt, 2000, 1f);
    }
    
}
