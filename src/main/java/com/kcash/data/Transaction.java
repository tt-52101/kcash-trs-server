package com.kcash.data;

import com.kcash.util.ECC;
import com.kcash.util.JSON;
import com.kcash.util.MyByte;
import com.kcash.util.SHA;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;


public class Transaction {
  private long expiration;
  private String alpAccount; // 子地址
  private Asset alpInportAsset; // 子地址资产类型
  private List<Operation> operations;
  private ResultTransactionType resultTrxType;
  private VoteType voteType;
  private List<byte[]> signatures;
  //
  private byte[] bytes;
  private byte[] toSignBytes;
  private ACTPrivateKey actPrivateKey;
  private ACTAddress toAddress;
  private String jsonStr;
  //
  public static final String ACT_SYMBOL = "ACT";
  public static final String CONTRACT_SYMBOL = "CON";
  private static final byte[] CHAIN_ID =
      MyByte.builder()
            .copyByteString("6701c01c6042098645e0ed939fa78649bd10c2877af609fa0cad12da62690f97") // 测试链ID
//      .copyByteString("6a1cb528f6e797e58913bff7a45cdd4709be75114ccd1ccb0e611b808f4d1b75") // 正式链ID
            .getData();
  public static final Long requiredFees = 1000L; // 转账手续费 0.01 * 100000
  private static final Long _transactionExpiration = 3_600_000L; // 3,600,000ms / 1h

  public String toJSONString() {
    if (jsonStr == null) {
      jsonStr = JSON.build()
                    .add("expiration", new Date(expiration))
                    .add("alp_account", alpAccount)
                    .add("alp_inport_asset", alpInportAsset.toJSON())
                    .add("operations", operations.stream().map(Operation::toJSON).collect(toList()))
                    .add("signatures", signatures.stream().map(MyByte::toHex).collect(toList()))
                    .get().toJSONString();
    }
    return jsonStr;
  }

  public byte[] toBytes() {
    if (bytes == null) {
      bytes = MyByte.builder()
                    .copy(toSign(), toSign().length - CHAIN_ID.length)
                    .copy(signatures)
                    .getData();
    }
    return bytes;
  }

  private byte[] toSign() {
    if (toSignBytes == null) {
      toSignBytes = MyByte.builder()
                          .copy(expiration / 1000L, 4)
//                          .copy(1508401006, 4) //test expiration
                          .copy(0, 1) // reserved optional<uint64_t> wtf?
                          .copy(alpAccount)
                          .copy(alpInportAsset.toBytes())
                          .copy(operations.stream().map(Operation::toBytes).collect(toList()))
                          .copy(resultTrxType._byte)
                          .copy(0, 20)
                          .copy(CHAIN_ID)
                          .getData();
    }
    return toSignBytes;
  }

  public Transaction(
      ACTPrivateKey actPrivateKey, // 转出者的私钥
      Long amount,           // 转出数额 * 100000
      String toAddressStr,   // 目标地址
      String remark) {
    this(actPrivateKey, amount, toAddressStr);
    this.setTransferOperations(amount, remark);
    this.sign();
  }

  public Transaction(
      ACTPrivateKey actPrivateKey,
      CONTRACT contract,
      String toAddressStr,
      long amount,
      long maxCallContractCost) {
    this(actPrivateKey, amount, toAddressStr);
    this.setContractTransferOperations(contract, toAddressStr, amount, maxCallContractCost);
    this.sign();
  }

  private Transaction(
      ACTPrivateKey actPrivateKey, // 转出者的私钥
      Long amount,          // 转出数额 * 100000
      String toAddressStr   /* 目标地址*/) {
    this.checkArguments(actPrivateKey, amount, toAddressStr);
    this.actPrivateKey = actPrivateKey;
    this.voteType = VoteType.VOTE_NONE; // 最简方式
    this.expiration = System.currentTimeMillis() + _transactionExpiration;
    this.resultTrxType = ResultTransactionType.ORIGIN_TRANSACTION;
    this.setAddress(toAddressStr, amount);
  }

  private void sign() {
    this.signatures = new ArrayList<>();
    this.signatures.add(ECC.signCompact(actPrivateKey, SHA._256hash(toSign())));
  }

  private void checkArguments(ACTPrivateKey actPrivateKey, long amount, String toAddressStr) {
    if (actPrivateKey == null) {
      throw new RuntimeException("param actPrivateKey is not present");
    } else if (amount <= 0) {
      throw new RuntimeException("param amount is less than or equal to 0");
    } else if (toAddressStr == null) {
      throw new RuntimeException("param toAddressStr is not present");
    }
  }

  private void setAddress(String toAddressStr, Long amount) {
    if (toAddressStr.startsWith(ACT_SYMBOL)) {
      toAddressStr = toAddressStr.substring(3);
    } else {
      throw new RuntimeException("地址错误");
    }
    if (toAddressStr.length() >= 64) { //获取子地址
      String sub = toAddressStr.substring(toAddressStr.length() - 32);
      if (sub.equals("ffffffffffffffffffffffffffffffff")) {
        setNoneAlp();
      } else {
        alpAccount = ACT_SYMBOL + toAddressStr;
        alpInportAsset = new Asset(amount);
      }
      toAddressStr = toAddressStr.substring(0, toAddressStr.length() - 32);
    } else {
      setNoneAlp();
    }
    this.toAddress = new ACTAddress(toAddressStr);
  }

  private void setNoneAlp() {
    alpAccount = "";
    alpInportAsset = new Asset(0L);
  }

  private void setTransferOperations(long amount, String remark) {
    operations = new ArrayList<>();
    operations.add(Operation.createWithdraw(actPrivateKey, amount + requiredFees));
    operations.add(Operation.createDeposit(toAddress, amount));
    if (remark != null && remark.length() > 0) {
      operations.add(Operation.createIMessage(remark));
    }
  }

  private void setContractTransferOperations(
      CONTRACT contract,
      String toAddress,
      long amount,
      long maxCallContractCost) {
    setCallContractOperations(
        contract,
        CONTRACT.TRANSFER_METHOD,
        CONTRACT.makeTransferArgs(toAddress, amount),
        new Asset(maxCallContractCost));
  }

  private void setCallContractOperations(
      CONTRACT contract,
      String method,
      String args,
      Asset costLimit) {
    operations = new ArrayList<>();
    operations.add(Operation.createCallContract(actPrivateKey, contract, method, args, costLimit));
  }

  private enum ResultTransactionType {
    ORIGIN_TRANSACTION(0),
    COMPLETE_RESULT_TRANSACTION(1),
    INCOMPLETE_RESULT_TRANSACTION(2),;

    private byte _byte;

    ResultTransactionType(int _byte) {
      this._byte = (byte) _byte;
    }
  }

  public enum VoteType {
    // 不投票
    VOTE_NONE("vote_none"),
    // 投所有人最多108人
    VOTE_ALL("vote_all"),
    // 随机投票，从支持者中随机选取一定的人数进行投票最多不超过36人
    VOTE_RANDOM("vote_radom"),
    // 根据已经选择的投票人进行投票，如果选择的投票人的publish_data中有其他投票策略加入到自己的投票策略中
    VORE_RECOMMENDED("vote_recommended"),;

    private String value;

    VoteType(String value) {
      this.value = value;
    }
  }
}