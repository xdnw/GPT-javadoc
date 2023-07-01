package com.github.xdnw.embedding;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import ai.djl.modality.nlp.DefaultVocabulary;
import ai.djl.modality.nlp.Vocabulary;
import ai.djl.modality.nlp.bert.BertToken;
import ai.djl.modality.nlp.bert.BertTokenizer;
import ai.djl.modality.nlp.qa.QAInput;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class BertTranslator implements Translator<QAInput, String> {
    private List<String> tokens;
    private Vocabulary vocabulary;
    private BertTokenizer tokenizer;

    @Override
    public void prepare(TranslatorContext ctx) throws IOException {
        Path path = Paths.get("app/src/main/resources/bertqa/vocab.txt");
        if (!path.toFile().exists()) {
            throw new IllegalStateException("File not found: " + path);
        }
        vocabulary = DefaultVocabulary.builder()
                    .optMinFrequency(1)
                    .addFromTextFile(path)
                    .optUnknownToken("[UNK]")
                    .build();
        tokenizer = new BertTokenizer();
    }

    @Override
    public NDList processInput(TranslatorContext ctx, QAInput input) {
        BertToken token =
                tokenizer.encode(
                        input.getQuestion().toLowerCase(),
                        input.getParagraph().toLowerCase());
        // get the encoded tokens that would be used in precessOutput
        tokens = token.getTokens();
        NDManager manager = ctx.getNDManager();
        // map the tokens(String) to indices(long)
        long[] indices = tokens.stream().mapToLong(vocabulary::getIndex).toArray();
        long[] attentionMask = token.getAttentionMask().stream().mapToLong(i -> i).toArray();
        long[] tokenType = token.getTokenTypes().stream().mapToLong(i -> i).toArray();
        NDArray indicesArray = manager.create(indices);
        NDArray attentionMaskArray =
               manager.create(attentionMask);
        NDArray tokenTypeArray = manager.create(tokenType);
        // The order matters
        return new NDList(indicesArray, attentionMaskArray, tokenTypeArray);
    }

    @Override
    public String processOutput(TranslatorContext ctx, NDList list) {
        NDArray startLogits = list.get(0);
        NDArray endLogits = list.get(1);
        int startIdx = (int) startLogits.argMax().getLong();
        int endIdx = (int) endLogits.argMax().getLong();
        return tokens.subList(startIdx, endIdx + 1).toString();
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}
