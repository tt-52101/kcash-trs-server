package com.kcash.data;

import com.kcash.util.MyByte;
import com.kcash.util.Ripemd160;
import com.kcash.util.SHA;

import org.bouncycastle.util.encoders.Hex;

public class WithdrawCondition {
  private int assetId; // fc::singed_int/int
  private int slateId; //
  private WithdrawConditionType type; // uint8_t
  private WithdrawBalanceType balanceType; // uint8_t
  private byte[] data; // vector<char>
  //
  private byte[] id;
  private byte[] bytes;

  public WithdrawCondition(ACTAddress address) {
    this.assetId = 0; // ACT
    this.slateId = 0; // ？？？
    this.type = WithdrawConditionType.WITHDRAW_SIGNATURE_TYPE;
    this.balanceType = WithdrawBalanceType.WITHDRAW_COMMON_TYPE;
    this.data = address.getAddress21();
  }

  public byte[] toBytes() {
    if (bytes == null) {
      bytes = MyByte.builder()
                    .padding()
                    .copy(assetId)
                    .copy(slateId)
                    .copy(type._byte)
                    .copy(balanceType._byte)
                    .copyVector(data)
                    .getData();
    }
    return bytes;
  }

  public byte[] getBalanceId() {
    if (id == null) {
      id = Ripemd160.hash(SHA._512hash(toBytes()));
    }
    return id;
  }

  public int getAssetId() {
    return assetId;
  }

  public int getSlateId() {
    return slateId;
  }

  public WithdrawConditionType getType() {
    return type;
  }

  public WithdrawBalanceType getBalanceType() {
    return balanceType;
  }

  enum WithdrawConditionType {
    WITHDRAW_NULL_TYPE(0),
    WITHDRAW_SIGNATURE_TYPE(1),
    WITHDRAW_MULTISIG_TYPE(3),
    WITHDRAW_ESCROW_TYPE(6),;

    private byte _byte;

    WithdrawConditionType(int _byte) {
      this._byte = (byte) _byte;
    }
  }

  enum WithdrawBalanceType {
    WITHDRAW_COMMON_TYPE(0),
    WITHDRAW_CONTRACT_TYPE(1),
    WITHDRAW_MARGIN_TYPE(2),;

    private byte _byte;

    WithdrawBalanceType(int _byte) {
      this._byte = (byte) _byte;
    }
  }


  public static void main(String[] args) throws Exception {
    ACTAddress address = new ACTAddress("Cd7GRUr3HpGTXBBpW2cWp4mRi38kZnhEo");
    WithdrawCondition withdrawCondition = new WithdrawCondition(address);
    Hex.encode(withdrawCondition.getBalanceId(), System.out);
    System.out.println(" ->测试摘要算法");
    Thread.sleep(10);
    int[] b = {0xbf, 0x02, 0xb4, 0x4c,
               0xfe, 0x04, 0x96, 0xc0,
               0x58, 0xec, 0xee, 0x82,
               0x30, 0x7f, 0x8a, 0xa9,
               0x0c, 0xdb, 0xf7, 0x83,};
    byte[] c = new byte[b.length];
    for (int i = 0; i < b.length; i++) {
      c[i] = (byte) b[i];
    }
    Hex.encode(c, System.err);
    System.err.println(" ->正确结果");
//    System.out.println(" " + Arrays.toString(c));
  }

}
