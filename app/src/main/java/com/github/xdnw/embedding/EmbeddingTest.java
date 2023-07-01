package com.github.xdnw.embedding;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.ModelException;
import ai.djl.huggingface.translator.TextClassificationTranslatorFactory;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.nlp.preprocess.SimpleTokenizer;
import ai.djl.modality.nlp.preprocess.TextProcessor;
import ai.djl.modality.nlp.preprocess.UnicodeNormalizer;
import ai.djl.modality.nlp.qa.QAInput;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.DownloadUtils;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.util.Platform;
import ai.djl.util.Utils;

public class EmbeddingTest {
 // https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/tree/main   
 

    private static final List<String> LABELS = Arrays.asList("question", "order", "statement");

    public static void main(String[] args) throws Exception {
        ClassifyTest();
    }

    public static void ClassifyTest() throws IOException, ModelException, TranslateException {
        String text = "If 1=1, DJL is the best, else its the worst.";

        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .setTypes(String.class, Classifications.class)
                        .optModelUrls(
                                "djl://ai.djl.huggingface.pytorch/distilbert-base-uncased-finetuned-sst-2-english")
                        .optEngine("PyTorch")
                        .optTranslatorFactory(new TextClassificationTranslatorFactory())
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, Classifications> model = criteria.loadModel();
                Predictor<String, Classifications> predictor = model.newPredictor()) {
            Classifications res = predictor.predict(text);
            System.out.println(res);
        }
    }

    public static void EmbeddingTest() throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        Platform platform = Platform.detectPlatform("pytorch");
        String overrideVersion = Utils.getEnvOrSystemProperty("PYTORCH_VERSION");
        if (overrideVersion != null
                && !overrideVersion.isEmpty()
                && !platform.getVersion().startsWith(overrideVersion)) {
            // platform.version can be 1.8.1-20210421
            platform = Platform.detectPlatform("pytorch", overrideVersion);
            // print
        }
        long start = System.currentTimeMillis();
        System.out.println("platform version: " + platform.getVersion() + " | " + platform.getFlavor());
        String text = "This is an example sentence";

        // multi-qa-MiniLM-L6-cos-v1 for embeddings


        Criteria<String, float[]> criteria = Criteria.builder()
            .setTypes(String.class, float[].class)
            .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
            .optEngine("PyTorch")
            .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
            .build();

        String[] sentences = {
            "The quick brown fox jumps over the lazy dog.",
            "I like to eat pizza for dinner.",
            "The capital of France is Paris.",
            "The Earth is the third planet from the sun.",
            "The Mona Lisa is a famous painting by Leonardo da Vinci.",
            "The United States of America has 50 states.",
            "The mitochondria is the powerhouse of the cell.",
            "The Great Wall of China is the longest wall in the world.",
            "The quadratic formula is used to solve quadratic equations.",
            "The human body has 206 bones.",
            "The Declaration of Independence was signed in 1776.",
            "The Eiffel Tower is located in Paris, France.",
            "The mitochondria is the powerhouse of the cell.",
            "The Great Barrier Reef is the largest coral reef system in the world.",
            "The Pythagorean theorem is used to calculate the length of the sides of a right triangle.",
            "The Amazon rainforest is the largest rainforest in the world.",
            "The Statue of Liberty was a gift from France to the United States.",
            "The human brain has about 100 billion neurons.",
            "The periodic table lists all known chemical elements.",
            "The Great Pyramid of Giza is one of the Seven Wonders of the Ancient World.",
            "The speed of light is approximately 299,792,458 meters per second.",
            "The theory of relativity was developed by Albert Einstein.",
            "The Roman Empire was one of the largest empires in history.",
            "The human heart beats about 100,000 times per day.",
            "The moon is approximately 238,855 miles away from Earth.",
            "The first successful airplane flight was made by the Wright brothers in 1903.",
            "The human eye can distinguish about 10 million different colors.",
            "The Panama Canal connects the Atlantic and Pacific Oceans.",
            "The Great Depression was a severe worldwide economic depression that lasted from 1929 to 1939.",
            "The human ear can detect sounds ranging from 20 Hz to 20,000 Hz.",
            "The Titanic was a British passenger liner that sank in the North Atlantic Ocean in 1912."
        };

        try (ZooModel<String, float[]> model = criteria.loadModel();
                Predictor<String, float[]> predictor = model.newPredictor()) {
            System.out.println("Model loaded in " + (System.currentTimeMillis() - start) + "ms.");
            for (String sentence : sentences) {
                start = System.currentTimeMillis();
                float[] res = predictor.predict(sentence);
                System.out.println("Embedding `" + sentence + "` in " + (System.currentTimeMillis() - start) + "ms.");
            }
        }
        start = System.currentTimeMillis();
        try (ZooModel<String, float[]> model = criteria.loadModel();
                Predictor<String, float[]> predictor = model.newPredictor()) {
            System.out.println("Model 2 loaded in " + (System.currentTimeMillis() - start) + "ms.");
            for (String sentence : sentences) {
                start = System.currentTimeMillis();
                float[] res = predictor.predict(sentence);
                System.out.println("Embedding `" + sentence + "` in " + (System.currentTimeMillis() - start) + "ms.");
            }
        }
    }

    public static void BertTranslaterTest() throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        DownloadUtils.download("https://djl-ai.s3.amazonaws.com/mlrepo/model/nlp/question_answer/ai/djl/pytorch/bertqa/0.0.1/bert-base-uncased-vocab.txt.gz", "app/src/main/resources/bertqa/vocab.txt", new ProgressBar());
        DownloadUtils.download("https://djl-ai.s3.amazonaws.com/mlrepo/model/nlp/question_answer/ai/djl/pytorch/bertqa/0.0.1/trace_bertqa.pt.gz", "app/src/main/resources/bertqa/bertqa.pt", new ProgressBar());

        BertTranslator translator = new BertTranslator();
        
        Criteria<QAInput, String> criteria = Criteria.builder()
        .setTypes(QAInput.class, String.class)
        .optModelPath(Paths.get("app/src/main/resources/bertqa/")) // search in local folder
        .optOption("mapLocation", "true")
        .optDevice(Device.cpu())
        .optTranslator(translator)
        .optProgress(new ProgressBar()).build();

        ZooModel model = criteria.loadModel();

        // -------------

        var question = "What command should I use to search for a tennis string?";
        var resourceDocument = """
                You are Foo, an AI that recommends tennis equipment for a specific player
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
                3. Write to file: "write_to_file", args: "file": "<file>", "text": "<text>"
                4. Read file: "read_file", args: "file": "<file>"
                5. Append to file: "append_to_file", args: "file": "<file>", "text": "<text>"
                6. Delete file: "delete_file", args: "file": "<file>"
                7. Search Files: "search_files", args: "directory": "<directory>"
                8. Do Nothing: "do_nothing", args: 
                9. Task Complete (Shutdown): "task_complete", args: "reason": "<reason>"

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
                Response Format: 
                {
                    "thoughts": {
                        "text": "thought",
                        "reasoning": "reasoning",
                        "plan": "- short bulleted\n- list that conveys\n- long-term plan",
                        "criticism": "constructive self-criticism",
                        "speak": "thoughts summary to say to user"
                    },
                    "command": {
                        "name": "command name",
                        "args": {
                            "arg name": "value"
                        }
                    }
                } 
                Ensure the response can be parsed by Python json.loads
                """;;

            

        String predictResult = null;

        QAInput input = new QAInput(question, resourceDocument);

        // Create a Predictor and use it to predict the output
        try (Predictor<QAInput, String> predictor = model.newPredictor(translator)) {
            predictResult = predictor.predict(input);
        }

        System.out.println(question);
        System.out.println(predictResult);
    }

    // public static void main(String[] args) throws IOException {
    //     String question = "When did BBC Japan start broadcasting?";
    //     String resourceDocument = 
    //     "BBC Japan was a general entertainment Channel.\n" + 
    //     "Which operated between December 2004 and April 2006.\n" + 
    //     "It ceased operations after its Japanese distributor folded.";
    //     QAInput input = new QAInput(question, resourceDocument);

    //     BertTokenizer tokenizer = new BertTokenizer();
    //     List<String> tokenQ = tokenizer.tokenize(question.toLowerCase());
    //     List<String> tokenA = tokenizer.tokenize(resourceDocument.toLowerCase());

    //     System.out.println("tokenQ: " + tokenQ);
    //     System.out.println("tokenA: " + tokenA);

    //     BertToken token = tokenizer.encode(question.toLowerCase(), resourceDocument.toLowerCase());
    //     List<String> tokens = token.getTokens();
    //     List<Long> tokenTypes = token.getTokenTypes();
    //     List<Long> attentionMask = token.getAttentionMask();

    //     System.out.println("tokens: " + tokens);
    //     System.out.println("toeknTypes: " + tokenTypes);
    //     System.out.println("attentionMask: " + attentionMask);

    //     test1();
    // }

    // public static void test1() throws IOException {
    //     Path file = Paths.get("/app/src/main/resources/bert/vocab.txt");
    //     // ensure file exists
    //     if (!file.toFile().exists()) {
    //         throw new IllegalStateException("File not found: " + file);
    //     }

    //     Vocabulary vocabulary = DefaultVocabulary.builder()
    //                             .optMinFrequency(1)
    //                             .addFromTextFile(file)
    //                             .optUnknownToken("[UNK]")
    //                             .build();

    //     // index: 2482
    //     long index = vocabulary.getIndex("car");

    //     // token: car
    //     String token = vocabulary.getToken(2482);
    // }
}
