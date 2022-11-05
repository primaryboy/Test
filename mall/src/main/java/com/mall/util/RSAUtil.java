package com.mall.util;

import com.mall.dao.UserMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.annotations.Mapper;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class RSAUtil {
    //private static String src = "sx3";
    @Mapper
    UserMapper userMapper;
    private static KeyPairGenerator keyPairGenerator;
    private static KeyFactory keyFactory;
    private static KeyPair keyPair;


    static {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(512);
            keyPair = keyPairGenerator.generateKeyPair();
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public static byte[] PrivateKey() {
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate();
        System.out.println(rsaPrivateKey.getEncoded());
        return rsaPrivateKey.getEncoded();
    }
    public static String PublicKey()  {
        RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();
        System.out.println(rsaPublicKey.getEncoded());
        return Hex.encodeHexString(rsaPublicKey.getEncoded());

    }
   public static void main(String[] args) {
        String result=RSAsign("sx3");
       RSAcheck(result,"sx3",PublicKey());
    }

    public static String RSAsign(String src) {
        try {
            //2.执行签名
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(toByteArray(Hex.encodeHexString(PrivateKey())));
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(privateKey);
            signature.update(src.getBytes());
            byte[] result = signature.sign();
            String jg= Hex.encodeHexString(result);
            System.out.println(result);
            System.out.println("jdk rsa sign : " + jg);
            return jg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //byte[] dail = new byte[1];
        return "";

    }
    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {// 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }
    /*public static String toHexString(byte[] byteArray) {
        String str = null;
        if (byteArray != null && byteArray.length > 0) {
            StringBuffer stringBuffer = new StringBuffer(byteArray.length);
            for (byte byteChar : byteArray) {
                stringBuffer.append(String.format("%02X", byteChar));
            }
            str = stringBuffer.toString();
        }
        return str;
    }*/

    public static int RSAcheck(String result,String src,String publickey) {
        try {
            byte[] by=toByteArray(result);
            //3.验证签名
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(toByteArray(publickey));
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            keyFactory = KeyFactory.getInstance("RSA");
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(publicKey);
            signature.update(src.getBytes());
            //验证传入的签名
            boolean bool = signature.verify(by);
            System.out.println("jdk rsa verify : " + bool);
            if(bool){return 1;}
            else{return 0;}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
