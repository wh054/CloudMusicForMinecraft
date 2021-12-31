package team.info.ncmfm.utils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class EncryptUtil {

    public static void main(String[] args) {
        System.out.println(MD5("13342690797"));
    }

    // md5加密
    public static String MD5(String inputText) {
        return encrypt(inputText, "MD5").toUpperCase();
    }

    /**
     * @param inputText     要加密的内容
     * @param algorithmName 加密算法名称
     * @return
     */
    private static String encrypt(String inputText, String algorithmName) {
        if (inputText == null || "".equals(inputText.trim())) {
            throw new IllegalArgumentException("请输入要加密的内容");
        }
        if (algorithmName == null || "".equals(algorithmName.trim())) {
            algorithmName = "MD5";
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithmName);
            messageDigest.update(inputText.getBytes("UTF8"));
            byte s[] = messageDigest.digest();
            return hex(s);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 返回十六进制字符串
    private static String hex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte anArr : arr) {
            sb.append(Integer.toHexString((anArr & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }
}


