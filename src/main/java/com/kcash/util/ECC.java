package com.kcash.util;

import com.kcash.data.ACTPrivateKey;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

public class ECC {
  private static final ECDomainParameters CURVE;
  private static final ECParameterSpec EC_PARAMS_SPEC;
  private static final SecureRandom SECURE_RANDOM;
  private static final KeyFactory KEY_FACTORY;
  private static final KeyAgreement KEY_AGREEMENT;
  private static final KeyPairGenerator KEY_PAIR_GENERATOR;

  static {
    String ECDSAParam = "secp256k1";
    String ECDSAType = "EC";
    String agreement = "ECDH";
    Provider provider = new BouncyCastleProvider();
    X9ECParameters params = SECNamedCurves.getByName(ECDSAParam);

    CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    EC_PARAMS_SPEC = ECNamedCurveTable.getParameterSpec(ECDSAParam);
    SECURE_RANDOM = new SecureRandom();
    try {
      KEY_FACTORY = KeyFactory.getInstance(ECDSAType, provider);
      KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance(ECDSAType, provider);
      KEY_PAIR_GENERATOR.initialize(new ECGenParameterSpec(ECDSAParam), SECURE_RANDOM);
      KEY_AGREEMENT = KeyAgreement.getInstance(agreement, provider);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] signCompact(ACTPrivateKey prv, byte[] data) {
    BigInteger[] sign = _signCore(prv.getD(), data);
    return MyByte.builder()
                 .copy((byte) (generateSignV(prv.getPublicKey(false), data, sign[0], sign[1]) + 31))
                 .copy(MyByte.copyBytesR(sign[0].toByteArray(), 32))
                 .copy(MyByte.copyBytesR(sign[1].toByteArray(), 32))
                 .getData();
  }

  public static byte[] sign(BigInteger prv, byte[] data) {
    BigInteger[] sign = _signCore(prv, data);
    BigInteger r = sign[0];
    BigInteger s = sign[1];
    try {
      ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
      DERSequenceGenerator derGen = new DERSequenceGenerator(bAOS);
      derGen.addObject(new ASN1Integer(r));
      derGen.addObject(new ASN1Integer(s));
      derGen.close();
      return bAOS.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static BigInteger[] _signCore(BigInteger prv, byte[] data) {
    synchronized (CURVE) {
      ECDSASigner signer = new ECDSASigner();
      signer.init(true, new ParametersWithRandom(new ECPrivateKeyParameters(prv, CURVE), SECURE_RANDOM));
      return signer.generateSignature(data);
    }
  }

  public boolean verify(byte[] pub, byte[] data, byte[] signature) {
    try {
      ASN1InputStream decoder = new ASN1InputStream(signature);
      DLSequence seq = (DLSequence) decoder.readObject();
      ASN1Integer r = (ASN1Integer) seq.getObjectAt(0);
      ASN1Integer s = (ASN1Integer) seq.getObjectAt(1);
      decoder.close();
      return _verifyCore(data, pub, r.getValue(), s.getValue());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean _verifyCore(byte[] data, byte[] pub, BigInteger... sign) {
    synchronized (CURVE) {
      ECDSASigner signer = new ECDSASigner();
      ECPublicKeyParameters params = new ECPublicKeyParameters(
          CURVE.getCurve().decodePoint(pub), CURVE);
      signer.init(false, params);
      return signer.verifySignature(data, sign[0], sign[1]);
    }
  }

  public static ECPublicKey loadPublicKey(byte[] data) {
    synchronized (EC_PARAMS_SPEC) {
      try {
        return (ECPublicKey) KEY_FACTORY.generatePublic(
            new ECPublicKeySpec(EC_PARAMS_SPEC.getCurve().decodePoint(data), EC_PARAMS_SPEC));
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException(e);
      }
      /*new X509EncodedKeySpec(data)*/
    }
  }

  public static ECPrivateKey loadPrivateKey(byte[] data) {
    synchronized (EC_PARAMS_SPEC) {
      try {
        return (ECPrivateKey) KEY_FACTORY.generatePrivate(
            new ECPrivateKeySpec(new BigInteger(data), EC_PARAMS_SPEC));
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException(e);
      }
      /*new PKCS8EncodedKeySpec(data)*/
    }
  }

  public static byte[] calculatePublicKey(BigInteger prv, boolean compressed) {
    synchronized (CURVE) {
      return CURVE.getG().multiply(prv).getEncoded(compressed);
    }
  }

  public static byte[] generateSharedSecret(PrivateKey privateKey, PublicKey publicKey) {
    synchronized (KEY_AGREEMENT) {
      try {
        KEY_AGREEMENT.init(privateKey);
        KEY_AGREEMENT.doPhase(publicKey, true);
      } catch (InvalidKeyException e) {
        throw new RuntimeException(e);
      }
      return KEY_AGREEMENT.generateSecret();
    }
  }

  public static KeyPair generate() {
    synchronized (KEY_PAIR_GENERATOR) {
      return KEY_PAIR_GENERATOR.generateKeyPair();
    }
  }

  public static int generateSignV(byte[] pub, byte[] data, BigInteger r, BigInteger s) {
    for (int i = 0; i < 4; i++) {
      byte[] k = recoverPubBytesFromSignature(i, data, r, s);
      if (k != null && Arrays.equals(k, pub)) {
        return i;
      }
    }
    throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
  }

  public static byte[] recoverPubBytesFromSignature(int recId, byte[] data, BigInteger r, BigInteger s) {
    synchronized (CURVE) {
      BigInteger n = CURVE.getN();
      BigInteger i = BigInteger.valueOf(recId / 2);
      BigInteger x = r.add(i.multiply(n));
      ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
      BigInteger prime = curve.getQ();
      if (x.compareTo(prime) >= 0) {
        return null;
      }
      ECPoint R = decompressKey(x, (recId & 1) == 1);
      if (!R.multiply(n).isInfinity()) {
        return null;
      }
      BigInteger e = new BigInteger(1, data);
      BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
      BigInteger rInv = r.modInverse(n);
      BigInteger srInv = rInv.multiply(s).mod(n);
      BigInteger eInvRInv = rInv.multiply(eInv).mod(n);
      ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvRInv, R, srInv);
      return q.getEncoded(/* compressed */ false);
    }
  }

  private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
    synchronized (CURVE) {
      X9IntegerConverter x9 = new X9IntegerConverter();
      byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
      compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
      return CURVE.getCurve().decodePoint(compEnc);
    }
  }


  public static void main(String[] args) throws NoSuchAlgorithmException {
    byte[] otherPubKey =
        MyByte.builder().copyByteString("02a1bbab1a1fad23c1a0a40464d37f41e62694b19b66f032dc8248b982f4a93bae").getData();
    System.out.println("other public key: " + MyByte.bytesToHex(otherPubKey));
    ACTPrivateKey privateKey = new ACTPrivateKey(MyByte.builder().copyByteString(
        "6f38d0d1b8d44b90a361c43d8b03b54a6676788272aedf54509d0e3c0e608711").getData());
    byte[] myPubKey = privateKey.getPublicKey(true);
    System.out.println("   my public key: " + MyByte.bytesToHex(myPubKey));
    byte[] password1 = generateSharedSecret(
        privateKey.getECPrivateKey(),
        loadPublicKey(otherPubKey));
    System.out.println("   my shared secret: " + MyByte.bytesToHex(password1));
    password1 = SHA._512hash(
        MyByte.builder().copy(password1, 1, 31)
              .copy(otherPubKey, 32)
              .getData());
    System.out.println("   my shared secret: " + MyByte.bytesToHex(password1));

    byte[] password = MyByte.builder().copyByteString(
        "95836531154f5b8cff3f4af7729baa3740b4695814dafb38dc542ec0cf78f7648f94549c66811f29f2c2f9386e55c0c81014b216c0127a6a42ac0d213c852189")
                            .getData();
    System.out.println("right shared secret: " + MyByte.bytesToHex(password));
    byte[] hashed = SHA._256hash(password);
    System.out.println("正确密钥: " + MyByte.bytesToHex(hashed));
    byte[] content = new byte[]{49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54};
//    byte[] iv = new byte[]{48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 49, 49, 49, 49};
    long[] cityHash = CityHash.cityHash128(password, 0, password.length);
    byte[] testCityHash = MyByte.builder().copy(cityHash[1]).copy(cityHash[0]).getData();
    System.out.println("   my IV: " + MyByte.bytesToHex(testCityHash));
    byte[] iv = MyByte.builder().copyByteString("9a32cf9ace5d95c412ed3eae59d5aa56").getData();
    System.out.println("right IV: " + MyByte.bytesToHex(iv));
    byte[] encoded = AES.encode(hashed, content, iv);
    System.out.println("我的密文: " + MyByte.bytesToHex(encoded));
    encoded = new byte[]{-118, -60, -89, 31, -121, -61, -41, 118, 102, -78, -86, -80, 21, -107, 26, 57,};
    System.out.println("正确密文: " + MyByte.bytesToHex(encoded));
    byte[] decoded = AES.decode(hashed, encoded, iv);
    System.out.println("我的还原: " + MyByte.bytesToHex(decoded));
    System.out.println("正确原文: " + MyByte.bytesToHex(content));
    System.out.println("正确原文字符: " + new String(content));
  }
}