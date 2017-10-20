package com.kcash.data;

import com.kcash.util.Base58;
import com.kcash.util.MyByte;
import com.kcash.util.Ripemd160;

public class ACTAddress {
  private String addressStr;
  private byte[] addressDecode;

  private byte[] address21;
  private byte[] address20;

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
    addressDecode = MyByte.builder()
                          .copy(id)
                          .copy(Ripemd160.hash(id), 4)
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
    return Transaction.ACT_SYMBOL + addressStr;
  }

  public String getContractStrStartWithSymbol() {
    return Transaction.CONTRACT_SYMBOL + addressStr;
  }

  public byte[] getAddressDecode() {
    return addressDecode;
  }

  public byte[] getAddress20() {
    if (address20 == null) {
      address20 = MyByte.builder().copy(addressDecode, 20).getData();
    }
    return address20;
  }

  public byte[] getAddress21() {
    if (address21 == null) {
      address21 = MyByte.builder().copy(addressDecode, 20).padding().getData();
    }
    return address21;
  }
}
