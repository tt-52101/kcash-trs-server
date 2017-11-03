package com.kcash.data;

import com.kcash.util.Base58;
import com.kcash.util.MyByte;
import com.kcash.util.RIPEMD160;

public class ACTAddress {
  private String addressStr;
  private byte[] encoded;

  private static final int checksum_len = 4;

  public enum Type {
    ADDRESS(20),
    CONTRACT(20),
    BALANCE_ID(20),
    PUBLIC_KEY(33),;
    private int length;

    Type(int length) {
      this.length = length;
    }

    public boolean checkLen(int len) {
      return this.length == len;
    }
  }

  /**
   * 字符串转地址
   */
  public ACTAddress(String addressStr, Type type) {
    byte[] addressDecode = Base58.decode(addressStr);
    if (!check(addressDecode, type)) {
      throw new RuntimeException("地址错误");
    }
    this.encoded = MyByte.copyBytes(addressDecode, addressDecode.length - checksum_len);
    this.addressStr = addressStr;
  }

  public ACTAddress(byte[] encoded, Type type) {
    if (!type.checkLen(encoded.length)) {
      throw new RuntimeException("地址错误");
    }
    this.encoded = encoded;
  }

  private static boolean check(byte[] addressDecode, Type type) {
    int coreLen = addressDecode.length - checksum_len;
    if (!type.checkLen(coreLen)) {
      return false;
    }
    byte[] checksum = RIPEMD160.hash(addressDecode, coreLen);
    for (int i = 0; i < checksum_len; i++) {
      if (addressDecode[coreLen + i] != checksum[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean check(String address, Type type) {
    if (type.equals(Type.ADDRESS) && address.length() >= 60) {
      return checkAlpSubAddress(address.substring(address.length() - 32)) &&
             check(Base58.decode(address.substring(0, address.length() - 32)), Type.ADDRESS);
    } else {
      return check(Base58.decode(address), type);
    }
  }

  private static boolean checkAlpSubAddress(String subAddress) {
    return subAddress.matches("[0-9a-f]{32}");
  }

  public String getAddressStr() {
    if (addressStr == null) {
      addressStr = Base58.encode(
          MyByte.builder()
                .copy(encoded)
                .copy(RIPEMD160.hash(encoded), checksum_len)
                .getData());
    }
    return addressStr;
  }

  public String getAddressStrStartWithSymbol() {
    return Transaction.ACT_SYMBOL + getAddressStr();
  }

  public String getContractStrStartWithSymbol() {
    return Transaction.CONTRACT_SYMBOL + getAddressStr();
  }

  public byte[] getEncoded() {
    return encoded;
  }
}
