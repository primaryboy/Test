package com.mall.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class SHAUtil {

    public static byte[] enSHA(byte[] data, String shaN) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(shaN);
        sha.update(data);
        return sha.digest();
    }

    public static String encode(String str) {
        byte[] outputData = new byte[0];
        try {
            outputData = enSHA((str+"tqmwbsc").getBytes(), "SHA-256");
            return new BigInteger(1, outputData).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}






