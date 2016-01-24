package myst.maven.extensions.eventspy.broadcaster.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;

/**
 * a java native implementation, to avoid cluttering event spy classpath with external dependencies  
 * */
public enum Protocol {

  HTTP {
    @Override
    public void send(String url, byte[] data, int timeout, boolean isJson) throws IOException {
      // by default HTTP POST verb would be used
      send(url, data, timeout, isJson, Protocol.RequestMethod.POST);
    }

    @Override
    public void send(String url, byte[] data, int timeout, boolean isJson, String requestMethod) throws IOException {
      System.out.println("sending request to endpoint");	
      // TODO : need to introduce http client instead of using Java native constructs
      final URL targetUrl = new URL(url);
      if (!targetUrl.getProtocol().startsWith("http")) {
        throw new IllegalArgumentException("Not an http(s) url: " + url);
      }

      // Verifying if the HTTP_PROXY is available
      final String httpProxyUrl = System.getenv().get("http_proxy");
      URL proxyUrl = null;
      if (httpProxyUrl != null && httpProxyUrl.length() > 0) {
        proxyUrl = new URL(httpProxyUrl);
        if (!proxyUrl.getProtocol().startsWith("http")) {
          throw new IllegalArgumentException("Not an http(s) url: " + httpProxyUrl);
        }
      }

      HttpURLConnection connection = null;
      if (proxyUrl == null) {
        connection = (HttpURLConnection) targetUrl.openConnection();

      } else {
        // Proxy connection to the address provided
        final int proxyPort = proxyUrl.getPort() > 0 ? proxyUrl.getPort() : 80;
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyPort));
        connection = (HttpURLConnection) targetUrl.openConnection(proxy);
      }

      connection.setRequestProperty("Content-Type", String.format("application/%s;charset=UTF-8", isJson ? "json" : "xml"));
      final String userInfo = targetUrl.getUserInfo();
      if (null != userInfo) {
        final String b64UserInfo = DatatypeConverter.printBase64Binary(userInfo.getBytes());
        final String authorizationHeader = "Basic " + b64UserInfo;
        connection.setRequestProperty("Authorization", authorizationHeader);
      }
      if (null != data && data.length > 0) {
    	System.out.println("data is " + data);  
        connection.setFixedLengthStreamingMode(data.length);
      }
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      // TODO : change this hard coding to accomodate other methods
      connection.setRequestMethod(requestMethod);
      connection.connect();      
      try {
        final OutputStream output = connection.getOutputStream();
        try {
          output.write(data);
          output.flush();
        } catch (final Exception e) {
          throw new RuntimeException(e);
        } finally {
          output.close();
        }
      } finally {
        if (connection.getResponseCode() > 204) {
          // this signifies API has responded anything but acceptable codes
          // close connection gracefully
          final int statusCode = connection.getResponseCode();
          connection.disconnect();
          throw new IOException("[MyST Studio] Received following response code from server: " + statusCode);
        }
        connection.disconnect();
      }
    }

    @Override
    public void validateUrl(String url) {
      try {
        // noinspection ResultOfObjectAllocationIgnored
        new URL(url);
      } catch (final MalformedURLException e) {
        throw new RuntimeException(String.format("%sUse http://hostname:port/path for endpoint URL", isEmpty(url) ? "" : "Invalid URL '" + url + "'. "));
      }
    }
  },
  TCP {
    @Override
    public void send(String url, byte[] data, int timeout, boolean isJson) throws IOException {
      final HostnamePort hostnamePort = HostnamePort.parseUrl(url);
      final SocketAddress endpoint = new InetSocketAddress(InetAddress.getByName(hostnamePort.hostname), hostnamePort.port);
      final Socket socket = new Socket();
      socket.setSoTimeout(timeout);
      socket.connect(endpoint, timeout);
      final OutputStream output = socket.getOutputStream();
      output.write(data);
      output.flush();
      output.close();
    }
  },
  UDP {
    @Override
    public void send(String url, byte[] data, int timeout, boolean isJson) throws IOException {
      final HostnamePort hostnamePort = HostnamePort.parseUrl(url);
      final DatagramSocket socket = new DatagramSocket();
      final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(hostnamePort.hostname), hostnamePort.port);
      socket.send(packet);
    }
  };

  public interface RequestMethod {
    String POST = "POST";
    String PUT = "PUT";
    String GET = "GET";
    String DELETE = "DELETE";
    String PATCH = "PATCH";
  }

  private static boolean isEmpty(String s) {
    return ((s == null) || (s.trim().length() < 1));
  }

  public abstract void send(String url, byte[] data, int timeout, boolean isJson) throws IOException;

  public void send(String url, byte[] data, int timeout, boolean isJson, String requestMethod) throws IOException {
    // default implementation
    throw new UnsupportedOperationException("Not supported except for HTTP protocol");
  }

  public void validateUrl(String url) {
    try {
      final HostnamePort hnp = HostnamePort.parseUrl(url);
      if (hnp == null) {
        throw new Exception();
      }
    } catch (final Exception e) {
      throw new RuntimeException(String.format("%sUse hostname:port for endpoint URL", isEmpty(url) ? "" : "Invalid URL '" + url + "'. "));
    }
  }
}
