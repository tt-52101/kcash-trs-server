package com.kcash.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyByte {
  private byte[] data;
  private int writeIndex;

  public MyByte(int l) {
    data = new byte[l];
    writeIndex = 0;
  }

  public MyByte(byte[] bytes) {
    data = bytes;
    writeIndex = bytes.length - 1;
  }

  public MyByte copy(byte[] src) {
    return copy(src, src.length);
  }

  public MyByte copy(byte[] src, int l) {
    return copy(src, 0, l);
  }

  public MyByte copy(byte[] src, int s, int l) {
    System.arraycopy(src, s, data, writeIndex, l);
    writeIndex += l - s;
    return this;
  }

  public byte[] getData() {
    return data;
  }

  private static byte[] convert(long d, int l) {
    byte[] tar = new byte[l];
    for (int i = 0; i < l; i++) {
      tar[i] = (byte) (i < 8 ? ((d >>> i * 8) & 0xff) : 0);
    }
    return tar;
  }

  public static byte[] trunk(byte[] src) {
    return trunk(src, src.length);
  }

  public static byte[] trunk(byte[] src, int d) {
    int i = 0;
    for (; i < d; i++) {
      if (src[i] != 0) {
        break;
      }
    }
    return copyBytes(src, i, src.length - i);
  }

  public static byte[] copyBytes(byte[] src) {
    return copyBytes(src, 0, src.length);
  }

  public static byte[] copyBytes(byte[] src, int l) {
    return copyBytes(src, 0, l);
  }

  public static byte[] copyBytes(byte[] src, int s, int l) {
    return new MyByte(l).copy(src, s, l).getData();
  }

  public static byte[] copyBytesR(byte[] src, int l) {
    return copyBytes(src, src.length - l, l);
  }

  public static byte[] paddingHeadAndTail(byte[] src) {
    return builder().padding().copy(src).padding().getData();
  }

  public static MyByte builder(int l) {
    return new MyByte(l);
  }

  public static BuildList builder() {
    return new BuildList();
  }


  final protected static char[] hexArray = "0123456789abcdef".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static class BuildList {
    List<byte[]> list = new ArrayList<>();

    public MyByte finish() {
      int l = 0;
      for (byte[] bytes : list) {
        l += bytes.length;
      }
      MyByte myByte = new MyByte(l);
      list.forEach(myByte::copy);
      return myByte;
    }

    public byte[] getData() {
      return finish().getData();
    }

    public BuildList padding() {
      return copy((byte) 0);
    }

    public BuildList copy(int d) {
      return copy(convert(d, 4));
    }

    public BuildList copy(long d) {
      return copy(d, 8);
    }

    public BuildList copy(long d, int l) {
      return copy(convert(d, l));
    }

    public BuildList copy(byte[] src) {
      return copy(src, src.length);
    }

    public BuildList copy(BigInteger src) {
      return copy(src.toByteArray());
    }

    public BuildList copy(List<byte[]> src) {
      copySize(src.size());
      src.forEach(this::copy);
      return this;
    }

    public BuildList copy(Map<byte[], Long> src) {
      copySize(src.size());
      src.forEach((k, v) -> copy(k).copy(v));
      return this;
    }

    public BuildList copy(byte[] src, int l) {
      return copy(src, 0, l);
    }

    public BuildList copy(byte[] src, int s, int l) {
      byte[] tar = new byte[l];
      System.arraycopy(src, s, tar, 0, l);
      list.add(tar);
      return this;
    }

    public BuildList copy(byte d) {
      list.add(new byte[]{d});
      return this;
    }

    public BuildList copy(String src) {
      if (src == null || src.length() == 0) {
        return padding();
      } else {
        return copyVector(src.getBytes());
      }
    }

    public BuildList copyByteString(String s) {
      int l = s.length();
      byte[] bytes = new byte[l / 2];
      for (int i = 0; i < l; i += 2) {
        bytes[i / 2] = (byte) (int) Integer.valueOf(s.substring(i, i + 2), 16);
      }
      list.add(bytes);
      return this;
    }

    public BuildList copyVector(byte[] src) {
      return copySize(src.length).copy(src, src.length);
    }

    public BuildList copySize(long l) {
      do {
        int b = (int) l & 0x7f;
        l >>= 7;
        b |= (l > 0 ? 1 : 0) << 7;
        copy((byte) b);
      } while (l > 0);
      return this;
    }
  }
}
