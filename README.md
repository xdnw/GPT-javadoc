## GitHub Copilot Javadoc
GitHub Copilot Javadoc is a Java project that uses GitHub Copilot to generate commit messages for git diffs and Javadocs for Java classes and methods.

### Usage
To generate a commit message using GitHub Copilot, run the `generateCommitMessage` method in the `AutoCommit` class. This method is the default entry point for the Gradle project, so you can simply run `./gradlew run` to start the program. The program will prompt you for the path to the Git repository directory, a brief description of the changes made, and how many commit message options to generate. It will then use GitHub Copilot to generate a list of commit message options, and prompt you to choose a commit message or write your own. The chosen message will be set as the commit message for the repository.

Note:
- You must have a valid copilot subscription (you will be prompted to authorize this application when it is run)
- The quality of the generated commit messages may vary, as GitHub Copilot is an inferior model at this task than GPT-3.5-turbo

### Work in Progress
The Javadoc generation feature is still a work in progress. I am working to improve the accuracy and reliability of this feature before running it on a whole project.

### To Do
I am always looking for ways to improve. Some ideas for future improvements include:

Generating more accurate and reliable commit messages using other language models.
Improving the Javadoc generation feature to handle more complex Java projects.

If you have any suggestions or feedback, please let me know!
