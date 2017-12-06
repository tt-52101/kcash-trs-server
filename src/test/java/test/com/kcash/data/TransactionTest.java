package test.com.kcash.data;

import com.kcash.data.ACTPrivateKey;
import com.kcash.data.Transaction;
import com.kcash.data.Contract;
import org.junit.Test;

public class TransactionTest {

  @Test
  public void testTransfer() {
    Transaction trx = Transaction.normal(
        new ACTPrivateKey("5KLee5Qc7Zxb6rPmtghNT3Dbqhop5XE6HGPoT9cQEVUt9BMTzcR"),
        100000L,
        "ACTDg2YESvdwqPRbQSV4GDKmdmB1NAHreF7A",
//        "ACTCd7GRUr3HpGTXBBpW2cWp4mRi38kZnhEofffffffffffffffffffffffffffffff1",
        ""
    );
    System.out.println(trx.toJSONString());
  }

  @Test
  public void testContractTransfer() {
    Transaction trx = Transaction.callContractTransferTo(
        new ACTPrivateKey("5Jjxz2UYLfBoWkPgs2tDnC2XPEVfdxyFzACZoYWC7EXPyXG7z3P"),
        Contract.USC.transferTo("ACT3hzHVhrekqbhdGrC9quUW28nU4r2gBuGm", 1L),
        1000L
    );
    System.out.println(trx.toJSONString());
  }

  @Test
  public void testContractCall() {
    Transaction trx = Transaction.callContract(
        new ACTPrivateKey("5JGQyR8FgJtSousVoAQw9bWS94YkKacuQPSGZ4UDkT5TVg919av"),
//        Contract.USC.issueApply("50000"),
        Contract.USC.signatureAgree("ACTHBKFo8jf28QKLQJ5d4iHwubhG9Krde2T8"),
        3000L
    );
    System.out.println(trx.toJSONString());
  }

  @Test
  public void testTransferToContract() {
    Transaction trx = Transaction.toContract(
        new ACTPrivateKey("5Jjxz2UYLfBoWkPgs2tDnC2XPEVfdxyFzACZoYWC7EXPyXG7z3P"),
        Contract.USC.getActAddress(),
        1L,
        1000L
    );
    System.out.println(trx.toJSONString());
  }
}
