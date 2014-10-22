package com.mxh.ftp.encryption;

import java.security.Key;
import java.security.SecureRandom;  

import javax.crypto.Cipher;  
import javax.crypto.KeyGenerator;  
import javax.crypto.SecretKey;  
  
public class JceProvider {  
    private final static String DES = "DES";  
      
    /** 
     * 加密 
     * @param data 数据源 
     * @param 密钥，长度任意 
     * @return 返回加密后的数据 
     */  
    public static String encrypt(String data, String password) {  
        try {  
            return byte2hex(encrypt(data.getBytes(), password.getBytes()));  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
    }  
      
    /** 
     * 解密 
     * @param data 
     * @param 密钥，长度任意 
     * @return 返回加密后的数据 
     */  
    public static String decrypt(String data, String password) {  
        try {  
            return new String(decrypt(hex2byte(data), password.getBytes()));  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
    }  
  
    /** 
     * DES加密 
     *  
     * @param src 
     *            数据源 
     * @param key 
     *            密钥，长度任意 
     * @return 返回加密后的数据 
     * @throws Exception 
     */  
    private static byte[] encrypt(byte[] src, byte[] key) throws Exception {  
        // DES算法要求有一个可信任的随机数源  
        SecureRandom sr = new SecureRandom();
//        SecureRandom
          
        // 从原始密匙数据创建一个SecretKey对象  
        KeyGenerator kgen = KeyGenerator.getInstance(DES);  
                  SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");  
                  secureRandom.setSeed(key);   
         kgen.init(secureRandom);
         Key secretKey=kgen.generateKey();
        // Cipher对象实际完成加密操作  
        Cipher cipher = Cipher.getInstance(DES);  
        // 用密匙初始化Cipher对象  
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);  
        // 现在，获取数据并加密  
        // 正式执行加密操作  
        return cipher.doFinal(src);  
    }  
  
    /** 
     * DES解密 
     *  
     * @param src 
     *            数据源 
     * @param key 
     *            密钥，长度任意 
     * @return 返回解密后的原始数据 
     * @throws Exception 
     */  
    private static byte[] decrypt(byte[] src, byte[] key) throws Exception {  
        // DES算法要求有一个可信任的随机数源  
        SecureRandom sr = new SecureRandom();  
  
        // 从原始密匙数据创建一个SecretKey对象  
        KeyGenerator kgen = KeyGenerator.getInstance(DES);  
                  SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");  
                  secureRandom.setSeed(key);   
        kgen.init(secureRandom);
        Key secretKey=kgen.generateKey();
        // Cipher对象实际完成解密操作  
        Cipher cipher = Cipher.getInstance(DES);  
        // 用密匙初始化Cipher对象  
        cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);  
        // 现在，获取数据并解密  
        // 正式执行解密操作  
        return cipher.doFinal(src);  
    }  
  
    /** 
     * 将二进制转化为16进制字符串 
     *  
     * @param b 
     *            二进制字节数组 
     * @return String 
     */  
    private static String byte2hex(byte[] b) {  
        String hs = "";  
        String stmp = "";  
        for (int n = 0; n < b.length; n++) {  
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));  
            if (stmp.length() == 1) {  
                hs = hs + "0" + stmp;  
            } else {  
                hs = hs + stmp;  
            }  
        }  
        return hs.toUpperCase();  
    }  
  
    /** 
     * 十六进制字符串转化为2进制 
     *  
     * @param hex 
     * @return 
     */  
    private static byte[] hex2byte(String hex) {  
        byte[] tmp = hex.getBytes();  
        byte[] ret = new byte[tmp.length / 2];  
        for (int i = 0; i < ret.length; i++) {  
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);  
        }  
        return ret;  
    }  
      
    /**   
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF   
     *    
     * @param src0   
     *            byte   
     * @param src1   
     *            byte   
     * @return byte   
     */    
    private static byte uniteBytes(byte src0, byte src1) {     
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))     
                .byteValue();     
        _b0 = (byte) (_b0 << 4);     
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))     
                .byteValue();     
        byte ret = (byte) (_b0 ^ _b1);     
        return ret;     
    }  
  
    /** 
     * @param args 
     */  
    public static void main(String[] args) throws Exception {  
        String source = "This is for DES test";  
        String temp = JceProvider.encrypt(source, "lfalkflakljmlajklfdalksjoie");  
        System.out.println(temp + ":" + temp.length());  
        String result2 = JceProvider.decrypt(temp, "lfalkflakljmlajklfdalksjoie");  
        System.out.println(result2 + ":" + result2.length());  
        System.out.println(source.equals(result2));  
    }  
  
} 