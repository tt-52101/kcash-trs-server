package com.kcash.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
  private final static Cipher CIPHER;

  static {
    try {
      CIPHER = Cipher.getInstance("AES/CBC/NoPadding", new BouncyCastleProvider());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] encode(byte[] keyEncoded, byte[] content, byte[] iv) {
    try {
      synchronized (CIPHER) {
        CIPHER.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(keyEncoded, "AES"),
                    new IvParameterSpec(iv));
        return CIPHER.doFinal(content);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] decode(byte[] keyEncoded, byte[] content, byte[] iv) {
    try {
      synchronized (CIPHER) {
        CIPHER.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyEncoded, "AES"),
                    new IvParameterSpec(iv));
        return CIPHER.doFinal(content);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
