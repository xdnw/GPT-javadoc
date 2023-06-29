package com.github.xdnw.gpt;

import java.util.ArrayList;
import java.util.List;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;

public class GPTUtil {

    public static int getTokens(String input, ModelType type) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncodingForModel(type);
        return enc.encode(input).size();
    }
    public static List<String> getChunks(String input, ModelType type, int tokenSizeCap) {
        List<String> result = new ArrayList<>();

        String[] lines = input.split("\n");

        // get tokenizer
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncodingForModel(type);

        int chunkSize = 6000;

        // get the tokens count for each line
        List<Integer> tokensCount = new ArrayList<>();
        for (String line : lines) {
            int size = enc.encode(line).size();
            if (size > chunkSize) {
                throw new IllegalArgumentException("Line exceeds token limit of " + tokenSizeCap);
            }
            tokensCount.add(size);
        }

        // iterate over lines in chunks of 6000 tokens
        int currentChunkSize = 0;
        StringBuilder currentChunk = new StringBuilder();
        for (String line : lines) {
            int lineTokens = tokensCount.get(0);
            if (currentChunkSize + lineTokens > tokenSizeCap) {
                // process current chunk
                result.add(currentChunk.toString());
                // start new chunk
                currentChunk = new StringBuilder();
                currentChunkSize = 0;
            }
            currentChunk.append(line).append("\n");
            currentChunkSize += lineTokens;
            tokensCount.remove(0);
        }
        // process last chunk
        result.add(currentChunk.toString());

        return result;
    }
}
