package com.jegerkatten.waxexpresstrade.utils;

import java.util.UUID;

public class RandomUtils {

    public static String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
