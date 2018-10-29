package com.jegerkatten.waxexpresstrade.utils;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TwoFAUtils {

    //This class is a slightly modified version of https://github.com/airG/java-totp/

    private static String lastCode = "";

    private static boolean shouldUpdate() {
        long value = (System.currentTimeMillis() / 1000) % 30;

        return value < 1 || lastCode.equals("");
    }

    public static String generateTwoFactorCode(String secret) throws GeneralSecurityException {
        if(!shouldUpdate()) {
            return lastCode;
        }

        long time = System.currentTimeMillis();
        byte[] key = decodeBase32(secret);

        byte[] data = new byte[8];
        long value = time / 1000 / 30;
        for (int i = 7; value > 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xF;

        long truncatedHash = 0;
        for (int i = offset; i < offset + 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;

        truncatedHash %= 1000000;

        lastCode = zeroPrepend(truncatedHash, 6);
        return lastCode;
    }

    private static String zeroPrepend(long num, int digits) {
        String hashStr = Long.toString(num);
        if (hashStr.length() >= digits) {
            return hashStr;
        } else {
            StringBuilder sb = new StringBuilder(digits);
            int zeroCount = digits - hashStr.length();
            sb.append("000000", 0, zeroCount);
            sb.append(hashStr);
            return sb.toString();
        }
    }

    private static byte[] decodeBase32(String str) {
        int numBytes = ((str.length() * 5) + 4) / 8;
        byte[] result = new byte[numBytes];
        int resultIndex = 0;
        int which = 0;
        int working = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            int val;
            if (ch >= 'a' && ch <= 'z') {
                val = ch - 'a';
            } else if (ch >= 'A' && ch <= 'Z') {
                val = ch - 'A';
            } else if (ch >= '2' && ch <= '7') {
                val = 26 + (ch - '2');
            } else if (ch == '=') {
                which = 0;
                break;
            } else {
                throw new IllegalArgumentException("Invalid base-32 character: " + ch);
            }
            switch (which) {
                case 0 :
                    working = (val & 0x1F) << 3;
                    which = 1;
                    break;
                case 1 :
                    working |= (val & 0x1C) >> 2;
                    result[resultIndex++] = (byte) working;
                    working = (val & 0x03) << 6;
                    which = 2;
                    break;
                case 2 :
                    working |= (val & 0x1F) << 1;
                    which = 3;
                    break;
                case 3 :
                    working |= (val & 0x10) >> 4;
                    result[resultIndex++] = (byte) working;
                    working = (val & 0x0F) << 4;
                    which = 4;
                    break;
                case 4 :
                    working |= (val & 0x1E) >> 1;
                    result[resultIndex++] = (byte) working;
                    working = (val & 0x01) << 7;
                    which = 5;
                    break;
                case 5 :
                    working |= (val & 0x1F) << 2;
                    which = 6;
                    break;
                case 6 :
                    working |= (val & 0x18) >> 3;
                    result[resultIndex++] = (byte) working;
                    working = (val & 0x07) << 5;
                    which = 7;
                    break;
                case 7 :
                    working |= (val & 0x1F);
                    result[resultIndex++] = (byte) working;
                    which = 0;
                    break;
            }
        }
        if (which != 0) {
            result[resultIndex++] = (byte) working;
        }
        if (resultIndex != result.length) {
            result = Arrays.copyOf(result, resultIndex);
        }
        return result;
    }
}
