package com.jegerkatten.waxexpresstrade.utils;

public class StringUtils {

    public static int getUserID(String tradeURL) {
        if(tradeURL.startsWith("https://trade.opskins.com/")) {
            tradeURL = tradeURL.substring("https://trade.opskins.com/".length());
        } else if(tradeURL.startsWith("http://trade.opskins.com/")) {
            tradeURL = tradeURL.substring("http://trade.opskins.com/".length());
        } else if(tradeURL.startsWith("trade.opskins.com/")) {
            tradeURL = tradeURL.substring("trade.opskins.com/".length());
        } else {
            return -1;
        }

        if(tradeURL.startsWith("t/")) {
            tradeURL = tradeURL.substring("t/".length());
            if(tradeURL.contains("/")) {
                tradeURL = tradeURL.substring(0, tradeURL.indexOf("/"));
            }
        } else if(tradeURL.startsWith("trade/userid/")) {
            tradeURL = tradeURL.substring("trade/userid/".length());
            if(tradeURL.contains("/")) {
                tradeURL = tradeURL.substring(0, tradeURL.indexOf("/"));
            }
        } else {
            return -1;
        }

        if(tradeURL.matches("^([0-9])+$")) {
            return Integer.parseInt(tradeURL);
        }
        return -1;
    }
}
