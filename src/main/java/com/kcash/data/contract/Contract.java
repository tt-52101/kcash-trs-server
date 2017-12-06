package com.kcash.data.contract;

import com.kcash.data.ACTAddress;
import com.kcash.data.ACTAddress.Type;
import java.math.BigDecimal;

public class Contract {

  public static __USC USC = new __USC();

  public static class __USC extends Body {
    String id() {
//      return "C8T55gPGnUALkNpBMLZGgEnGqcczfha6G"; // 测试链
      return "92cJUVM6qS9qp1ihnJB5DJrf1pP9F2fSB"; // 正式链
    }

    String name() {
      return "USD_COIN";
    }

    public Call issueApply(String amount) {
      return call("issue_apply", amount);
    }

    public Call signatureAgree(String applierAddress) {
      return call("signture_agree", applierAddress);
    }
  }


  public static class Call {
    private ACTAddress contractAddress;
    private String method;
    private String args;


    Call(ACTAddress contractAddress, String method, String args) {
      this.contractAddress = contractAddress;
      this.method = method;
      this.args = args;
    }

    public String getMethod() {
      return method;
    }

    public String getArgs() {
      return args;
    }

    public ACTAddress getContractAddress() {
      return contractAddress;
    }

    @Override
    public String toString() {
      return "address:" + contractAddress.getAddressStr() + "|method:" + method + "|args:" + args;
    }
  }

  public static class TransferToCall extends Call {
    private long amount;
    private String toAddress;

    TransferToCall(
        ACTAddress contractAddress,
        String method,
        String args,
        String toAddress,
        long amount) {
      super(contractAddress, method, args);
      this.toAddress = toAddress;
      this.amount = amount;
    }

    public String getToAddress() {
      return toAddress;
    }

    public long getAmount() {
      return amount;
    }
  }

  private static abstract class Body {
    private static final int _scale = 6;
    private static final BigDecimal _2bd = new BigDecimal(Math.pow(10, _scale - 1));
    static ACTAddress actAddress;

    public ACTAddress getActAddress() {
      if (actAddress == null) {
        actAddress = new ACTAddress(id(), Type.CONTRACT);
      }
      return actAddress;
    }

    String makeTransferArgs(String toAddress, long amount) {
      return toAddress + "|" + new BigDecimal(amount).divide(_2bd, _scale, BigDecimal.ROUND_DOWN)
                                                     .stripTrailingZeros();
    }

    public TransferToCall transferTo(String toAddress, long amount) {
      return new TransferToCall(
          getActAddress(),
          "transfer_to",
          makeTransferArgs(toAddress, amount),
          toAddress,
          amount);
    }

    protected Call call(String method, String args) {
      return new Call(getActAddress(), method, args);
    }

    abstract String id();

    abstract String name();
  }
}
