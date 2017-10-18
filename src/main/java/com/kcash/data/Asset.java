package com.kcash.data;

import com.kcash.util.MyByte;

import lombok.Data;

public class Asset {
  private long amount; // c++ __int64 8bit
  private int assetId; // c++ int 4bit
  //
  private byte[] toBytes;

  public byte[] toBytes() {
    if (toBytes == null) {
      toBytes = MyByte.builder()
                      .copy(amount)
                      .copy(assetId, 1)
                      .getData();
    }
    return toBytes;
  }

  public Asset(long amount) {
    this.amount = amount;
    this.assetId = 0; // ACT assetId
  }

  public long getAmount() {
    return amount;
  }

  public int getAssetId() {
    return assetId;
  }
}
