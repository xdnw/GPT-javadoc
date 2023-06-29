package com.github.xdnw.javadoc;

import java.util.ArrayList;
import java.util.List;

public class StringTest {
    public static boolean isQuote(char c) {
        switch (c) {
            case '\'':
            case '"':
            case '\u201C':
            case '\u201D':
                return true;
            default:
                return false;
        }
    }

    public static List<String> split(String input, char delim) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int bracket = 0;
        boolean inQuotes = false;
        char quoteChar = 0;
        char lastChar = ' ';
        for (int current = 0; current < input.length(); current++) {
            char currentChar = input.charAt(current);
            if (currentChar == '\u201C') currentChar = '\u201D';
            boolean atLastChar = current == input.length() - 1;
            switch (currentChar) {
                case '{':
                    bracket++;
                    break;
                case '}':
                    bracket--;
                    break;
            }
            if (!atLastChar && bracket > 0) {
                continue;
            }
            if (atLastChar) {
                String toAdd;
                if (inQuotes) {
                    toAdd = input.substring(start + 1, input.length() - 1);
                } else {
                    toAdd = input.substring(start);
                }
                if (!toAdd.isEmpty()) result.add(toAdd);
                continue;
            }
            if (isQuote(currentChar)) {
                if (quoteChar == 0 || (isQuote(quoteChar) && isQuote(currentChar) && currentChar == quoteChar)) {
                    inQuotes = !inQuotes;
                    quoteChar = inQuotes ? currentChar : 0;
                }
            }

            if (currentChar == delim && !inQuotes) {
                String toAdd = input.substring(start, current);
                if (!toAdd.isEmpty()) {
                    switch (toAdd.charAt(0)) {
                        case '\'':
                        case '\"':
                        case '\u201C':
                        case '\u201D':
                            toAdd = toAdd.substring(1, toAdd.length() - 1);
                    }
                    if (!toAdd.trim().isEmpty()) result.add(toAdd);
                } else if (inQuotes) {
                    result.add("");
                }
                start = current + 1;
            }
        }
        return result;
    }
}
