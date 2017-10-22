package com.kcash.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import org.bouncycastle.util.encoders.Base64;

public enum RPC {
  INFO(2),
  NETWORK_BROADCAST_TRANSACTION(2),;
  private int id;

  RPC(int id) {
    this.id = id;
  }

  private static final String url = "http://127.0.0.1:18096/rpc";
  private static final String rpc_user_name = "admin";
  private static final String rpc_password = "123456";
  private static final String auth = "000000" + Base64.toBase64String((rpc_user_name + ":" + rpc_password).getBytes());

  public Response call(String... params) {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization", auth);
      connection.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes("{\"jsonrpc\":\"2.0\",\"params\":" + Arrays.toString(params) +
                    ",\"id\":\"" + id + "\",\"method\":\"" + name().toLowerCase() + "\"}");
      wr.flush();
      wr.close();

      int responseCode = connection.getResponseCode();
      String message = null;
      if (200 == responseCode) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        message = response.toString();
      }
      return new Response(responseCode, message);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class Response {
    private int code;
    private String message;

    public int getCode() {
      return code;
    }

    public String getMessage() {
      return message;
    }

    Response(int code, String message) {
      this.code = code;
      this.message = message;
    }

    @Override
    public String toString() {
      return "RPC.Response(code:" + code + "; message:" + message + ")";
    }
  }
}
