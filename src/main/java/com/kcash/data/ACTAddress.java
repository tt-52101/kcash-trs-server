package com.kcash.data;

import com.kcash.util.Base58;
import com.kcash.util.MyByte;
import com.kcash.util.Ripemd160;

import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

public class ACTAddress {
  private String addressStr;
  private byte[] addressDecode;

  private byte[] address21;

  /**
   * 字符串转地址
   */
  public ACTAddress(String addressStr) {
    addressDecode = Base58.decode(addressStr);
    if (!check(addressDecode)) {
      throw new RuntimeException("无效地址");
    }
    this.addressStr = addressStr;
  }

  public ACTAddress(byte[] id) {
    byte[] rip160Hash = Ripemd160.hash(id);
    addressDecode = MyByte.builder()
                          .copy(id)
                          .copy(rip160Hash, 4)
                          .getData();
    addressStr = Base58.encode(addressDecode);
  }

  private static boolean check(byte[] addressDecode) {
    if (addressDecode.length != 24) {
      return false;
    }
    byte[] checksum = Ripemd160.hash(addressDecode, 20);
    for (int i = 0; i < 4; i++) {
      if (addressDecode[20 + i] != checksum[i]) {
        return false;
      }
    }
    return true;
  }

  private static boolean check(String address) {
    return check(Base58.decode(address));
  }

  public String getAddressStr() {
    return addressStr;
  }

  public String getAddressStrStartWithSymbol() {
    return Transaction.ASSET_SYMBOL + addressStr;
  }

  public byte[] getAddressDecode() {
    return addressDecode;
  }

  public byte[] getAddress21() {
    if (address21 == null) {
      address21 = MyByte.builder().copy(addressDecode, 20).padding().getData();
    }
    return address21;
  }

  public static void main(String[] args) throws IOException {
    ACTAddress a = new ACTAddress("Cd7GRUr3HpGTXBBpW2cWp4mRi38kZnhEo");
    Hex.encode(a.addressDecode, System.out);
    System.out.println(" ->address decode");
    Hex.encode(Ripemd160.hash(a.addressDecode, 20), System.out);
    System.out.println(" ->address hash");
  }
}
