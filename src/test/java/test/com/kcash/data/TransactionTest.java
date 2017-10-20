package test.com.kcash.data;

import com.kcash.data.ACTPrivateKey;
import com.kcash.data.CONTRACT;
import com.kcash.data.Transaction;

import org.junit.Test;

public class TransactionTest {

  @Test
  public void testTransfer() {
    Transaction trx = new Transaction(
        new ACTPrivateKey("5Jjxz2UYLfBoWkPgs2tDnC2XPEVfdxyFzACZoYWC7EXPyXG7z3P"),
        10000L,
        "ACTCd7GRUr3HpGTXBBpW2cWp4mRi38kZnhEo",
//        "ACTCd7GRUr3HpGTXBBpW2cWp4mRi38kZnhEofffffffffffffffffffffffffffffff1",
        "1234567890"
    );
    System.out.println(trx.toJSONString());
  }

  @Test
  public void testContractTransfer() {
    Transaction trx1 = new Transaction(
        new ACTPrivateKey("5Jjxz2UYLfBoWkPgs2tDnC2XPEVfdxyFzACZoYWC7EXPyXG7z3P"),
        CONTRACT.SMC_t,
        "ACT3hzHVhrekqbhdGrC9quUW28nU4r2gBuGm",
        1L,
        1000L
    );
    System.out.println(trx1.toJSONString());
  }
}
