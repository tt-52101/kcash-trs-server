package com.kcash;

import com.kcash.data.ACTPrivateKey;
import com.kcash.data.Transaction;
import com.kcash.util.RPC;

public class Application {

  public static void main(String[] args) {
    Transaction trx = new Transaction(
        new ACTPrivateKey("5Jjxz2UYLfBoWkPgs2tDnC2XPEVfdxyFzACZoYWC7EXPyXG7z3P"),
        1L,
        "ACTCd7GRUr3HpGTXBBpW2cWp4mRi38kZnhEo",
        null
    );
    System.out.println(trx.toJSONString());
    RPC.Response response = RPC.NETWORK_BROADCAST_TRANSACTION.call(trx.toJSONString());
    System.out.println(response);
//    TrxMessage message = new TrxMessage(trx.toBytes());
//    Node.sendMessage(message);
  }

}