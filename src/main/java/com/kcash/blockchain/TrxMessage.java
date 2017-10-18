package com.kcash.blockchain;

import com.kcash.util.MyByte;

import lombok.Data;

@Data
public class TrxMessage {
  private int size;
  private int msgType;
  private byte[] data;

  private byte[] bytes;

  public TrxMessage(byte[] data) {
    this.size = data.length;
    this.msgType = 1000;
    this.data = data;
  }

  public byte[] toBytes() {
    if (bytes == null) {
      bytes = MyByte.builder()
                    .copy(size)
                    .copy(msgType)
                    .copy(data)
                    .getData();
    }
    return bytes;
  }
}
