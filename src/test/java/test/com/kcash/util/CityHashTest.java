package test.com.kcash.util;

import com.kcash.util.CityHash;
import com.kcash.util.MyByte;
import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Test;

public class CityHashTest {

  @Test
  public void test() {
    byte[] password = MyByte.builder().copyByteString(
        "640004b5466aed4b3fa0750e9f736bd738c911c0154561225437966ac50f868d122b4347103bc26118641f9bc2a8771286b3fd14ca668331e8cb2813b8395098")
                            .getData();
    password = MyByte.builder()
                     .copy(password, 0, 8)
                     .getData();
    System.out.println(Arrays.toString(password));
    System.out.println(" data: " + MyByte.toHex(password));
    long[] cityHash = CityHash.cityHash128(password, 0, password.length);
    byte[] testCityHash = MyByte.builder().copy(cityHash[1]).copy(cityHash[0]).getData();
    System.out.println("   my: " + MyByte.toHex(testCityHash));
//    System.out.println("right: 9a32cf9ace5d95c412ed3eae59d5aa56");
    System.out.println(new BigInteger(1, MyByte.reverse(MyByte.builder().copy(cityHash[0]).getData())));
    System.out.println(new BigInteger(1, MyByte.reverse(MyByte.builder().copy(cityHash[1]).getData())));
  }
}
