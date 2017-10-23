package test.com.kcash.util;

import com.kcash.data.ACTPrivateKey;
import com.kcash.util.ECC;
import com.kcash.util.MyByte;
import com.kcash.util.SHA;
import org.junit.Test;

public class ECCTest {

  @Test
  public void testGenerateSharedSecret() {
    byte[] prv1byte = MyByte.builder()
                            .copyByteString("b65c7f7035841b6386088c65599375bd61ac1fd7b16b1c2a3d0a1a3e8266f5df")
                            .getData();
    ACTPrivateKey prv1 = new ACTPrivateKey(prv1byte);
    System.out.println(MyByte.toHex(prv1.getEncoded()) + " -> prv1");
//    System.out.println(MyByte.toHex(prv1.getPublicKey(true)) + " -> pub1");
    byte[] pub2 =
        MyByte.builder()
              .copyByteString("033a7410123fb6963067df64246d8c977e38ba001e458cc4dee8aa9b34312154bc")
              .getData();
    System.out.println(MyByte.toHex(pub2) + " -> pub2");
    byte[] sharedSecret = ECC.generateSharedSecret(
        ECC.loadPrivateKey(prv1.getEncoded()),
//        prv1.getECPrivateKey(),
        ECC.loadPublicKey(pub2));

    System.out.println(MyByte.toHex(sharedSecret) + " -> sharedSecret");
    // 不一致
    // 622d5513e546120e9d5a24f70181adcb51fb6e70efe5a957bb372208bb3317bd
    // 一致
    // d0ee66f93b75bc1c08047cee802223a45197da38a54af9cb22671d6376dec4d2
    // 2a66843dd6bbaca6c74506b565d45302ddfc28d168a7123ec98b3c2a60a161e7

    byte[] _ss = SHA._512hash(sharedSecret);
    System.out.println(MyByte.toHex(_ss) + " -> my");
    System.out.println("640004b5466aed4b3fa0750e9f736bd738c911c0154561225437966ac50f868d122b4347103bc26118641f9bc2a8771286b3fd14ca668331e8cb2813b8395098");
  }
}
