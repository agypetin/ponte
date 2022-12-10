package hu.ponte.hr.services;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
public class SignService {

    public String sign(byte[] bytes) throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream("/config/keys/key.private");
        if (resourceAsStream != null) {
            byte[] keyBytes = resourceAsStream.readAllBytes();
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(bytes);
            byte[] digitalSignature = signature.sign();
            return Base64.encodeBase64String(digitalSignature);
        }
        return null;
    }

    public boolean verify(String sign, byte[] bytes) throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream("/config/keys/key.pub");
        if (resourceAsStream != null) {
            byte[] keyBytes = resourceAsStream.readAllBytes();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(bytes);
            return signature.verify(Base64.decodeBase64(sign));
        }
        return false;
    }
}
