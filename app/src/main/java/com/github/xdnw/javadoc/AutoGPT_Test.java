package com.github.xdnw.javadoc;

import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.xdnw.gpt.CopilotHandler;

public class AutoGPT_Test {
    public static void main(String[] args) throws JsonProcessingException, InterruptedException, ExecutionException {
        String prompt = """
system: You are Foo, an AI that recommends tennis equipment for a specific player
Your decisions must always be made independently without seeking user assistance. Play to your strengths as an LLM and pursue simple strategies with no legal complications.

GOALS:

1. Find the top 3 most suitable tennis strings for a hard hitting baseline player who hits with a lot of topspin
2. Write the tennis strings to output
3. Shut down when you are done


Constraints:
1. ~4000 word limit for short term memory. Your short term memory is short, so immediately save important information to files.
2. If you are unsure how you previously did something or want to recall past events, thinking about similar events will help you remember.
3. No user assistance
4. Exclusively use the commands listed in double quotes e.g. "command name"

Commands:
1. Google Search: "google", args: "input": "<search>"
2. Browse Website: "browse_website", args: "url": "<url>", "question": "<what_you_want_to_find_on_website>"
3. Start GPT Agent: "start_agent", args: "name": "<name>", "task": "<short_task_desc>", "prompt": "<prompt>"
4. Message GPT Agent: "message_agent", args: "key": "<key>", "message": "<message>"
5. List GPT Agents: "list_agents", args: 
6. Delete GPT Agent: "delete_agent", args: "key": "<key>"
7. Clone Repository: "clone_repository", args: "repository_url": "<url>", "clone_path": "<directory>"
8. Write to file: "write_to_file", args: "file": "<file>", "text": "<text>"
9. Read file: "read_file", args: "file": "<file>"
10. Append to file: "append_to_file", args: "file": "<file>", "text": "<text>"
11. Delete file: "delete_file", args: "file": "<file>"
12. Search Files: "search_files", args: "directory": "<directory>"
13. Evaluate Code: "evaluate_code", args: "code": "<full_code_string>"
14. Get Improved Code: "improve_code", args: "suggestions": "<list_of_suggestions>", "code": "<full_code_string>"
15. Write Tests: "write_tests", args: "code": "<full_code_string>", "focus": "<list_of_focus_areas>"
16. Execute Python File: "execute_python_file", args: "file": "<file>"
17. Generate Image: "generate_image", args: "prompt": "<prompt>"
18. Send Tweet: "send_tweet", args: "text": "<text>"
19. Convert Audio to text: "read_audio_from_file", args: "file": "<file>"
20. Do Nothing: "do_nothing", args: 
21. Task Complete (Shutdown): "task_complete", args: "reason": "<reason>"

Resources:
1. Internet access for searches and information gathering.
2. Long Term memory management.
3. GPT-3.5 powered Agents for delegation of simple tasks.
4. File output.

Performance Evaluation:
1. Continuously review and analyze your actions to ensure you are performing to the best of your abilities.
2. Constructively self-criticize your big-picture behavior constantly.
3. Reflect on past decisions and strategies to refine your approach.
4. Every command has a cost, so be smart and efficient. Aim to complete tasks in the least number of steps.

You should only respond in JSON format as described below 
Response Format Example (multiple commands):
google("search qyuer"),google("another search query")

system: The current time and date is Sat Apr 22 01:43:22 2023
system: This reminds you of these events from your past:

user: Determine which next command to use, and respond using the format specified above:
g""";

            CopilotHandler handler = new CopilotHandler();
            String response = handler.getResponse(prompt, 2000, 0.3f, "\n");
            // ---
            System.out.println("Response:");
            System.out.println(response);

            prompt += response;
            response = handler.getResponse(prompt, 2000, 0.3f, "\n");
            // ---
            System.out.println("Response:");
            System.out.println(response);
    }
}
