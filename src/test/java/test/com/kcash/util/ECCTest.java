package test.com.kcash.util;

import com.kcash.data.ACTPrivateKey;
import com.kcash.util.CityHash;
import com.kcash.util.ECC;
import com.kcash.util.MyByte;
import com.kcash.util.SHA;
import org.junit.Test;

public class ECCTest {

  @Test
  public void testGenerateSharedSecret() {
    byte[] prv1byte = MyByte.builder()
                            .copyByteString("48a86172d05e8870c1cf913186231f62a2e6f0319bb45032497b95b6143aaddc")
                            .getData();
    ACTPrivateKey prv1 = new ACTPrivateKey(prv1byte);
    System.out.println(MyByte.toHex(prv1.getEncoded()) + " -> prv1");
//    System.out.println(MyByte.toHex(prv1.getPublicKey(true)) + " -> pub1");
    byte[] pub2 =
        MyByte.builder()
              .copyByteString("02d9c92dedc5609fd2e3e08d2099793c7620f455199a1c506ed442fe7541fb631a")
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
    System.out.println(MyByte.toHex(_ss) + "");
    System.out.println(
        "39e10909814ab97f8443461965d42bfd3b7a350797fe74f225602b0fe5899e8469c4c139e2056ce4756475d7d5de51e16b2915e7636eec699c948ebd86a385eb");

    long[] iv = CityHash.cityHash128(_ss, 0, _ss.length);
    System.out.println(
        MyByte.toHex(
            MyByte.builder().copy(iv[0]).copy(iv[1]).getData()));
  }
}
