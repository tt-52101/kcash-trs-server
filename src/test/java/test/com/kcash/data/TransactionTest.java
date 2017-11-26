package test.com.kcash.data;

import com.kcash.data.ACTPrivateKey;
import com.kcash.data.CONTRACT;
import com.kcash.data.Transaction;
import org.junit.Test;

public class TransactionTest {

  @Test
  public void testTransfer() {
    Transaction trx = Transaction.normal(
        new ACTPrivateKey("5JaW9VUrSFtk4ZurSgS7Be4PvF8n1FNqfjhdyHi4DyA8MoC5wqG"),
        100L,
        "ACTLRQRkdAx83WyiD5gRFwRkYx3kvKg3U1CZ",
//        "ACTCd7GRUr3HpGTXBBpW2cWp4mRi38kZnhEofffffffffffffffffffffffffffffff1",
        ""
    );
    System.out.println(trx.toJSONString());
  }

  @Test
  public void testContractTransfer() {
    Transaction trx = Transaction.callContractTransferTo(
        new ACTPrivateKey("5Jjxz2UYLfBoWkPgs2tDnC2XPEVfdxyFzACZoYWC7EXPyXG7z3P"),
        CONTRACT.SMC_t,
        "ACT3hzHVhrekqbhdGrC9quUW28nU4r2gBuGm",
        1L,
        1000L
    );
    System.out.println(trx.toJSONString());
  }

  @Test
  public void testTransferToContract() {
    Transaction trx = Transaction.toContract(
        new ACTPrivateKey("5Jjxz2UYLfBoWkPgs2tDnC2XPEVfdxyFzACZoYWC7EXPyXG7z3P"),
        CONTRACT.SMC_t,
        1L,
        1000L
    );
    System.out.println(trx.toJSONString());
  }
}
