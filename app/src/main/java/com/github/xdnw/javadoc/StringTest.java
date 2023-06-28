package com.github.xdnw.javadoc;

import java.util.ArrayList;
import java.util.List;

public class StringTest {
    public static void main(String[] args) {
        String command = """
            @Locutus#7602 !grant Borg {
            "infra_needed": 2500,
            "imp_total": 50,
            "imp_coalpower": 0,
            "imp_oilpower": 0,
            "imp_windpower": 0,
            "imp_nuclearpower": 2,
            "imp_coalmine": 0,
            "imp_oilwell": 0,
            "imp_uramine": 0,
            "imp_leadmine": 0,
            "imp_ironmine": 0,
            "imp_bauxitemine": 10,
            "imp_farm": 0,
            "imp_gasrefinery": 0,
            "imp_aluminumrefinery": 5,
            "imp_munitionsfactory": 5,
            "imp_steelmill": 0,
            "imp_policestation": 1,
            "imp_hospital": 5,
            "imp_recyclingcenter": 3,
            "imp_subway": 1,
            "imp_supermarket": 0,
            "imp_bank": 4,
            "imp_mall": 4,
            "imp_stadium": 3,
            "imp_barracks": 0,
            "imp_factory": 2,
            "imp_hangars": 5,
            "imp_drydock": 0
            } 1""";
            int i = 0;
            for (String arg : split(command, ' ')) {
                System.out.println((i++) + ". " + arg);
            }
    }

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
