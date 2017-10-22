package test.com.kcash.util;

import com.kcash.util.CityHash;
import com.kcash.util.MyByte;
import org.junit.Test;

public class CityHashTest {

  @Test
  public void test() {
    byte[] password = MyByte.builder().copyByteString(
        "95836531154f5b8cff3f4af7729baa3740b4695814dafb38dc542ec0cf78f7648f94549c66811f29f2c2f9386e55c0c81014b216c0127a6a42ac0d213c852189")
                            .getData();
    System.out.println(" data: " + MyByte.toHex(password));
    long[] cityHash = CityHash.cityHash128(password, 0, password.length);
    byte[] testCityHash = MyByte.builder().copy(cityHash[1]).copy(cityHash[0]).getData();
    System.out.println("   my: " + MyByte.toHex(testCityHash));
    System.out.println("right: 9a32cf9ace5d95c412ed3eae59d5aa56");
  }
}
