package com.github.xdnw.commit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import com.github.javaparser.utils.StringEscapeUtils;
import com.github.xdnw.javadoc.CopilotHandler;
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

    public static String getGitDiff(File repoDir, boolean useCached) throws IOException {
        String[] commands = useCached ? new String[]{"git", "--no-pager", "diff", "--cached"} : new String[]{"git", "--no-pager", "diff"};
        return executeCommand(repoDir, commands);
    }

    public static void setCommitMessage(File repoDir, String message) throws IOException {
        String[] commands = new String[]{"git", "commit", "--amend", "-m", message};
        executeCommand(repoDir, commands);
    }

    public static List<String> getCommitExamples(File repoDir, String userDescription, int numOptions) throws IOException, InterruptedException, ExecutionException {
        String gitDiff = CommandLineUtil.getGitDiff(repoDir, true).trim();
        if (gitDiff.isEmpty()) {
            gitDiff = CommandLineUtil.getGitDiff(repoDir, false).trim();
        }
        if (gitDiff.isEmpty()) {
            System.out.println("No changes to commit");
            return null;
        }

        // break into chunks of 6000
        List<String> chunks = GPTUtil.getChunks(gitDiff, ModelType.GPT_3_5_TURBO, 6000);

        CopilotHandler handler = new CopilotHandler();

        String prompt = """
            ```diff
            {git_diff}
            ``` 

            Please write a commit message summarizing the functional and behavioral changes above.

            Commit Message:
            ```markdown
            {user_description}""";

        prompt = prompt.replace("{user_description}", userDescription);

        int maxTokens = 8193 - 1;

        List<String> commitMessages = new ArrayList<>();

        float temperature = 0.7f;
        // go from 0.7 -> 0.3
        float increment = (0.7f - 0.3f) / (numOptions - 1);
        for (int i = 0; i < numOptions; i++) {
            StringBuilder commitMessage = new StringBuilder();
            for (String chunk : chunks) {
                String finalPrompt = prompt.replace("{git_diff}", chunk);
                int currentTokens = GPTUtil.getTokens(finalPrompt, ModelType.GPT_3_5_TURBO);
                int remaining = maxTokens - currentTokens;

                System.out.println("prompt " + finalPrompt);

                String response = handler.getResponse(finalPrompt, remaining, temperature, "\n\n", "```");
                commitMessage.append(response).append("\n");

                temperature -= increment;
            }
            commitMessages.add(commitMessage.toString());

            // if 1 and 2 match, return early
            if (i == 1 && commitMessages.get(0).equalsIgnoreCase(commitMessages.get(1))) {
                return commitMessages;
            }
        }
        return commitMessages;
    }

    public static void generateCommitMessage() throws IOException, InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Please enter the path to the Git repository directory: ");
        String repoPath = scanner.nextLine();
        File repoDir = new File(repoPath);

        if (!repoDir.isDirectory()) {
            System.out.println("Invalid directory path. Please enter a valid path to a Git repository directory.");
            return;
        }
        // ensure there is a .git folder too
        File gitDir = new File(repoDir, ".git");
        if (!gitDir.isDirectory()) {
            System.out.println("Invalid directory path. Please enter a valid path to a Git repository directory. (no `.git` folder found)");
            return;
        }

        System.out.print("Please enter a brief description of the changes made:\n> ");
        String userDescription = scanner.nextLine();

        System.out.print("Please enter a number of commit message options to generate:\n> ");
        int numOptions = scanner.nextInt();

        List<String> commitMessages = CommandLineUtil.getCommitExamples(repoDir, userDescription, numOptions);
        if (commitMessages == null) {
            return;
        }

        System.out.println("Please choose a commit message option or write your own:");

        for (int i = 0; i < commitMessages.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, commitMessages.get(i));
        }

        System.out.print("> ");

        scanner.nextLine();
        String input = scanner.nextLine();

        int choice;

        try {
            choice = Integer.parseInt(input);
            if (choice < 1 || choice > commitMessages.size()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice. Please enter a number between 1 and " + commitMessages.size() + " or write your own commit message.");
            return;
        }

        String commitMessage = commitMessages.get(choice - 1);

        if (commitMessage.isEmpty()) {
            System.out.println("Empty commit message. Please write your own commit message.");
            return;
        }

        System.out.printf("Setting commit message to: %s%n", commitMessage);

        // git add .
        executeCommand(repoDir, new String[]{"git", "add", "."});
        // commit with message: git commit -m "message"
        executeCommand(repoDir, new String[]{"git", "commit", "-m", commitMessage});
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        generateCommitMessage();
        // // K:\Github\locutus3
        // File repoDir = new File("K:\\Github\\locutus3");
        // boolean cached = false;
        // String diff = CommandLineUtil.getGitDiff(repoDir, cached).trim();
        // System.out.println(diff);
    }
}

