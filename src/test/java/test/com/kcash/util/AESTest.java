package test.com.kcash.util;

import com.kcash.util.AES;
import com.kcash.util.MyByte;

import org.junit.Test;

public class AESTest {

  @Test
  public void test() {
    byte[] content = new byte[]{49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54};
    byte[] key = MyByte.formHex("abb5871fd97bf7b4b32baecbdda3cd3e9a6b1b88df4204bf13fbae38b9f505ea");
    byte[] iv = MyByte.formHex("9a32cf9ace5d95c412ed3eae59d5aa56");
    byte[] encoded = AES.encode(key, content, iv);
    System.out.println("我的密文: " + MyByte.toHex(encoded));
    System.out.println("正确密文: 8ac4a71f87c3d77666b2aab015951a39");
    byte[] decoded = AES.decode(key, encoded, iv);
    System.out.println("我的还原: " + MyByte.toHex(decoded));
    System.out.println("正确原文: " + MyByte.toHex(content));
    System.out.println("正确原文字符: " + new String(content));
  }
}
