package test.com.kcash.util;

import com.kcash.data.ACTAddress;
import com.kcash.data.ACTAddress.Type;
import com.kcash.data.ACTPrivateKey;
import com.kcash.data.Contract;
import com.kcash.data.Transaction;
import com.kcash.util.RPC;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class RPCTest {

  @Test
  public void testNetworkBroadcastTransaction() throws Exception {
    Transaction trx = Transaction.callContract(
        new ACTPrivateKey("5K415WATniqAy9pidj3vXjUudLSwkrjKTtGjUQQ1aqRLxhgoqYG"),
//        Contract.USC.issueApply("50000"),
        Contract.BIYONG.transferTo("ACTp9SZPY891UWZvVGBnhxqAUzHewJ1fLgb", 100000 * 200),
        3000L
    );
    broadcastToBrowser(trx.toJSONString());
  }

  @Test
  public void testInfo() {
    System.out.println(RPC.INFO.call());
  }

  @Test
  public void batchSend() throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(
        new FileInputStream(new File("/Users/zhouyang/IdeaProjects/kcash-trs-server/src/test/java/a.txt")), "UTF-8"));
    Map<String, Long> address = new HashMap<>();
    String lineTxt;
    while ((lineTxt = br.readLine()) != null) {
      String[] a = lineTxt.split(",");
      ACTAddress actAddress = new ACTAddress(a[0].substring(3), Type.ADDRESS);
      if (address.get(a[0]) != null) {
        System.out.println(a[0] + "," + address.get(a[0]));
      }
      address.put(a[0], Long.valueOf(a[1]));
    }
    br.close();
    ACTPrivateKey actPrivateKey = new ACTPrivateKey("");
    address.forEach((k, v) -> {
      try {
        Transaction trx = Transaction.callContract(
            actPrivateKey,
            Contract.BIYONG.transferTo(k, 100000 * v),
            3000L
        );
        broadcastToBrowser(trx.toJSONString());
        Thread.sleep(1500);
        System.out.println("发放成功:" + k + ":" + v);
      } catch (Exception e) {
        System.out.println("发放失败:" + k + ":" + v);
      }
    });
  }


  public String broadcastToBrowser(String broadcastJson) {
    HttpPost httppost = null;
    String result = null;
    try {
      SSLContext sslcontext = createIgnoreVerifySSL();
      // 设置协议http和https对应的处理socket链接工厂的对象
      Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
          .register("http", PlainConnectionSocketFactory.INSTANCE)
          .register("https", new SSLConnectionSocketFactory(sslcontext))
          .build();
      PoolingHttpClientConnectionManager
          connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
      HttpClients.custom().setConnectionManager(connManager);
      CloseableHttpClient httpclients = HttpClients.custom().setConnectionManager(connManager).build();
      httppost = new HttpPost("https://api.achain.com/wallets/api/wallet/act/network_broadcast_transaction");
      httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
      List<NameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("message", broadcastJson));
      //设置参数到请求对象中
      httppost.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));
      CloseableHttpResponse response = httpclients.execute(httppost);
      if (null != response) {
        try {
          result = EntityUtils.toString(response.getEntity(), "UTF-8");
          if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RuntimeException();
          }
        } finally {
          response.close();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException();
    } finally {
      try {
        if (null != httppost) {
          httppost.releaseConnection();
        }
      } catch (Exception ignored) {
      }
    }
    if (result == null || result.length() == 0) {
      throw new RuntimeException();
    }
    return result;
  }

  public static SSLContext createIgnoreVerifySSL() throws Exception {
    SSLContext sc = SSLContext.getInstance("SSLv3");

    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
    X509TrustManager trustManager = new X509TrustManager() {
      public void checkClientTrusted(
          java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
          String paramString) {
      }

      public void checkServerTrusted(
          java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
          String paramString) {
      }

      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }
    };

    sc.init(null, new TrustManager[]{trustManager}, null);
    return sc;
  }
}
