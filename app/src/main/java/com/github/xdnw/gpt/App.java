package com.github.xdnw.gpt;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.xdnw.commit.AutoCommit;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        AutoCommit.generateCommitMessage();
    }
    
}
