package com.github.xdnw.embedding;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.DefaultVocabulary;
import ai.djl.modality.nlp.Vocabulary;
import ai.djl.modality.nlp.bert.BertToken;
import ai.djl.modality.nlp.bert.BertTokenizer;
import ai.djl.modality.nlp.qa.QAInput;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.DownloadUtils;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Platform;
import ai.djl.util.Utils;

public class EmbeddingTest {
 // https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/tree/main   
 

    public static void main(String[] args) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        EmbeddingTest();
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
        System.out.println("platform version: " + platform.getVersion() + " | " + platform.getFlavor());
        String text = "This is an example sentence";

        Criteria<String, float[]> criteria = Criteria.builder()
            .setTypes(String.class, float[].class)
            .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
            .optEngine("PyTorch")
            .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
            .build();

            try (ZooModel<String, float[]> model = criteria.loadModel();
                    Predictor<String, float[]> predictor = model.newPredictor()) {
                float[] res = predictor.predict(text);
                System.out.println("Embedding: " + Arrays.toString(res));
            }

            // todo test multi-qa-MiniLM-L6-dot-v1 for text completion
    }

    public static void BertTranslaterTest() throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        DownloadUtils.download("https://djl-ai.s3.amazonaws.com/mlrepo/model/nlp/question_answer/ai/djl/pytorch/bertqa/0.0.1/bert-base-uncased-vocab.txt.gz", "app/src/main/resources/bert/vocab.txt", new ProgressBar());
        DownloadUtils.download("https://djl-ai.s3.amazonaws.com/mlrepo/model/nlp/question_answer/ai/djl/pytorch/bertqa/0.0.1/trace_bertqa.pt.gz", "app/src/main/resources/bert/bertqa.pt", new ProgressBar());

        BertTranslator translator = new BertTranslator();

        Criteria<QAInput, String> criteria = Criteria.builder()
        .setTypes(QAInput.class, String.class)
        .optModelPath(Paths.get("app/src/main/resources/bert/")) // search in local folder
        .optTranslator(translator)
        .optProgress(new ProgressBar()).build();

        ZooModel model = criteria.loadModel();

        // -------------

        var question = "When did BBC Japan start broadcasting?";
        var resourceDocument = "BBC Japan was a general entertainment Channel.\n" +
            "Which operated between December 2004 and April 2006.\n" +
            "It ceased operations after its Japanese distributor folded.";

            

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
