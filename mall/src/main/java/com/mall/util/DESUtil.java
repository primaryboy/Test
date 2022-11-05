package com.mall.util;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;
import java.util.Random;


/**
 * @projectName: mall
 * @package: com.mall.util
 * @className: DESUtil
 * @author: beili
 * @description: TODO
 * @date: 2022/5/21 13:11
 * @version: 1.0
 */
public class DESUtil {
    //public static Random random=new Random();
    //public static  String getpas() {
        /*//密码，长度要是8的倍数
        StringBuilder str = new StringBuilder();//定义变长字符串
        Random random = new Random();
        //随机生成数字，并添加到字符串
        for (int i = 0; i < 8 ; i++) {
            str.append(random.nextInt(10));
        }
        str.append("tqmwbsc");*/
        //String str="tqmwbs";
        //eturn str;
    //}
//测试
/*public static void main(String args[]) {
        //待加密内容
        String s = "测试内容";
        String result = DESUtil.encrypt(s.getBytes(),getpas());
        System.out.println("加密后："+new String(result));
        //直接将如上内容解密
        try {
            byte[] decryResult = DESUtil.decrypt(result, getpas());
            System.out.println("解密后："+new String(decryResult));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }*/
    /**
     * 加密
     * @param datasource byte[]
     * @param password String
     * @return byte[]
     */
    public static  String encrypt(byte[] datasource, String password) {
        int i=password.length()%8;
        if(i!=0){
            for (int j=0;j<8-i;j++)
            {
                Random random = new Random();
                password=password+j;
            }
        }
        System.out.println(password);
        try{
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            //创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            //Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            //用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            //现在，获取数据并加密
            //正式执行加密操作
             cipher.doFinal(datasource);
             String jg= Hex.encodeHexString(cipher.doFinal(datasource));
            System.out.println("加密后："+jg);
             return jg;
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
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
    /**
     * 解密
     * @param src byte[]
     * @param password String
     * @return byte[]
     * @throws Exception
     */
    public static byte[] decrypt(String src, String password) throws Exception {
        byte[] by=toByteArray(src);
        int i=password.length()%8;
        if(i!=0){
            for (int j=0;j<i;j++)
            {
                Random random = new Random();
                password=password+j;
            }
        }
        System.out.println(password);
        // DES算法要求有一个可信任的随机数源
        SecureRandom random = new SecureRandom();
        // 创建一个DESKeySpec对象
        DESKeySpec desKey = new DESKeySpec(password.getBytes());
        // 创建一个密匙工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        // 将DESKeySpec对象转换成SecretKey对象
        SecretKey securekey = keyFactory.generateSecret(desKey);
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        // 真正开始解密操作
        return cipher.doFinal(by);
    }
}
