package com.github.xdnw.gpt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import com.github.javaparser.utils.StringEscapeUtils;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.ModelType;

public class CommandLineUtil {
    public static String executeCommand(File workingDir, String... command) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command, null, workingDir);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        StringBuilder output = new StringBuilder();
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            output.append(s).append("\n");
        }

        // Read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            System.err.println(s);
        }

        return output.toString();
    }

    
}

