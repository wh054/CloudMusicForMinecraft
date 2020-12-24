---
description: RSA加解密 DEMO
---

# RSA加解密



RSAUtil.java

```java
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtil {
    /**
     * 将Base64编码后的公钥转换成PublicKey对象
     * @Author: FOXCELL
     * @Date: 2020/11/25 10:19
     */
    public static PublicKey string2PublicKey(String pubStr) throws Exception{
        byte[] keyBytes = base642Byte(pubStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 将Base64编码后的私钥转换成PrivateKey对象
     * @Author: FOXCELL
     * @Date: 2020/11/25 10:19
     */
    public static PrivateKey string2PrivateKey(String priStr) throws Exception{
        byte[] keyBytes = base642Byte(priStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * 公钥加密
     * @Author: FOXCELL
     * @Date: 2020/11/25 10:19
     */
    public static byte[] publicEncrypt(byte[] content, PublicKey publicKey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    /**
     * 私钥加密
     * @Author: FOXCELL
     * @Date: 2020/11/25 10:19
     */
    public static byte[] privateEncrypt(byte[] content, PrivateKey privateKey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    /**
     * 私钥解密
     * @Author: FOXCELL
     * @Date: 2020/11/25 10:19
     */
    public static byte[] privateDecrypt(byte[] content, PrivateKey privateKey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    /**
     * 字节数组转Base64编码
     * @Author: FOXCELL
     * @Date: 2020/11/25 10:19
     */
    public static String byte2Base64(byte[] bytes){
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bytes);
    }

    /**
     * Base64编码转字节数组
     * @Author: FOXCELL
     * @Date: 2020/11/25 10:19
     */
    public static byte[] base642Byte(String base64Key) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(base64Key);
    }
}

```

App.java

```java
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final String RsaEncryptPUBLICKEY="";
    public static final String RsaEncryptPRIVATEKEY="";

    public static void main( String[] args ) throws Exception {
        String content="nmsl";
        String es= RsaBase64PublicKeyEnencrypt(content);
        System.out.println(es);
        String ds= RsaBase64PrivateKeyDecrypt(es);
        System.out.println(ds);
    }



    /**
     * @date 2020/8/13 17:55
     * @describe RSA公钥加密
     */
    private static String RsaBase64PublicKeyEnencrypt(String str) throws Exception{
        PublicKey publicKey = RSAUtil.string2PublicKey(RsaEncryptPUBLICKEY);
        //用公钥加密
        byte[] publicEncrypt = RSAUtil.publicEncrypt(str.getBytes(), publicKey);
        //加密后的内容Base64编码
        String byte2Base64 = RSAUtil.byte2Base64(publicEncrypt);
        return byte2Base64;
    }

    /**
     * @date 2020/8/13 17:55
     * @describe RSA私钥加密
     */
    private static String RsaBase64PrivateKeyEnencrypt(String str) throws Exception{
        PrivateKey privateKey = RSAUtil.string2PrivateKey(RsaEncryptPRIVATEKEY);
        byte[] privateEncrypt = RSAUtil.privateEncrypt(str.getBytes(), privateKey);
        //加密后的内容Base64编码
        String byte2Base64 = RSAUtil.byte2Base64(privateEncrypt);
        return byte2Base64;
    }

    /**
     * @date 2020/8/13 17:55
     * @describe RSA私钥解密
     */
    private static String RsaBase64PrivateKeyDecrypt(String str) throws Exception{
        PrivateKey privateKey = RSAUtil.string2PrivateKey(RsaEncryptPRIVATEKEY);
        byte[] decryptBytes=RSAUtil.privateDecrypt(RSAUtil.base642Byte(str),RSAUtil.string2PrivateKey(RsaEncryptPRIVATEKEY));;
        return new String(decryptBytes);
    }
}
```

