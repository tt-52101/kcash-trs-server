package test.com.kcash.data;

import com.kcash.data.ACTAddress;
import com.kcash.data.ACTAddress.Type;
import org.junit.Test;

public class AddressTest {

  @Test
  public void testCheck() {
    System.out.println(ACTAddress.check("3hzHVhrekqbhdGrC9quUW28nU4r2gBuGm", Type.ADDRESS));
    System.out.println(ACTAddress.check("3hzHVhrekqbhdGrC9quUW28nU4r2gBuGmffffffffffffffffffffffffffffgfff", Type.ADDRESS));
  }
}
