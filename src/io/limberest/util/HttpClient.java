package io.limberest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public class HttpClient {

    public static final String BASIC_AUTH_HEADER = "Authorization";

    private URL url;
    private HttpURLConnection connection;

    private int responseCode;
    public int getResponseCode() { return responseCode; }

    private String responseMessage;
    public String getResponseMessage() { return responseMessage; }

    private int connectTimeout = -1;
    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int timeout) { this.connectTimeout = timeout; }

    private int readTimeout = -1;
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int timeout) { this.readTimeout = timeout; }

    private Map<String,String> headers;
    public Map<String,String> getHeaders() { return headers; }
    public void setHeaders(Map<String,String> headers) { this.headers = headers; }
    public void setHeader(String name, String value) {
        if (headers == null)
            headers = new HashMap<>();
        headers.put(name, value);
    }

    private long maxBytes = -1;
    public long getMaxBytes() { return maxBytes; }
    public void setMaxBytes(long max) { this.maxBytes = max; }

    private Proxy proxy;
    public Proxy getProxy() { return proxy; }
    public void setProxy(Proxy proxy) { this.proxy = proxy; }

    private String user;
    private String password;

    private byte[] response;
    public byte[] getResponse() {
        return response;
    }

    public HttpClient(URL url) {
        this.url = url;
        if (url.getUserInfo() != null) {
            int colon = url.getUserInfo().indexOf(':');
            if (colon > 0) {
                this.user = url.getUserInfo().substring(0, colon);
                this.password = url.getUserInfo().substring(colon + 1);
            }
        }
    }

    public HttpClient(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public HttpClient(HttpURLConnection connection) {
        this.connection = connection;
    }

    public HttpClient(HttpURLConnection connection, String user, String password) {
        this.connection = connection;
        this.user = user;
        this.password = password;
    }

    /**
     * Perform an HTTP GET request against the URL.
     * @return the string response from the server
     */
    public byte[] get() throws IOException {
        if (connection == null) {
            if (proxy == null)
                connection = (HttpURLConnection) url.openConnection();
            else
                connection = (HttpURLConnection) url.openConnection(proxy);
        }

        prepareConnection(connection);

        connection.setDoOutput(false);
        connection.setRequestMethod("GET");

        HttpURLConnection.setFollowRedirects(true);

        readInput(connection);
        return response;
    }
    
    public String getString() throws IOException {
        return new String(get());
    }

    /**
     * Perform an HTTP POST request to the URL.
     * @param content string containing the content to be posted
     * @return string containing the response from the server
     */
    public byte[] post(byte[] content) throws IOException {

        if (connection == null) {
            if (proxy == null)
                connection = (HttpURLConnection) url.openConnection();
            else
                connection = (HttpURLConnection) url.openConnection(proxy);
        }

        prepareConnection(connection);

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        if (content != null) {
            OutputStream os = connection.getOutputStream();
            os.write(content);
            os.close();
        }

        readInput(connection);
        return response;
    }

    public String post(String content) throws IOException {
        return new String(post(content == null ? null : content.getBytes()));
    }

    /**
     * Perform an HTTP PUT request to the URL.
     * @param content bytes
     * @return string containing the response from the server
     */
    public byte[] put(byte[] content) throws IOException {

        if (connection == null) {
            if (proxy == null)
                connection = (HttpURLConnection) url.openConnection();
            else
                connection = (HttpURLConnection) url.openConnection(proxy);
        }

        prepareConnection(connection);

        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");

        if (content != null) {
            OutputStream os = connection.getOutputStream();
            os.write(content);
            os.close();
        }

        readInput(connection);
        return response;
    }
    
    public String put(String content) throws IOException {
        return new String(put(content == null ? null : content.getBytes()));
    }

    /**
     * Perform an HTTP DELETE request to the URL.
     * @param content bytes (not usually populated)
     * @return string containing the response from the server
     */
    public byte[] delete(byte[] content) throws IOException {

        if (connection == null) {
            if (proxy == null)
                connection = (HttpURLConnection) url.openConnection();
            else
                connection = (HttpURLConnection) url.openConnection(proxy);
        }

        prepareConnection(connection);
        connection.setRequestMethod("DELETE");

        OutputStream os = null;
        if (content != null) {
            connection.setDoOutput(true);
            os = connection.getOutputStream();
            os.write(content);
        }

        readInput(connection);
        if (os != null)
           os.close();
        return response;
    }
    
    public String delete(String content) throws IOException {
        return new String(delete(content == null ? null : content.getBytes()));
    }

    public String delete() throws IOException {
        return new String(delete((byte[])null));
    }
    
    /**
     * Populates the response member.  Closes the connection.
     */
    private void readInput(HttpURLConnection connection) throws IOException {
        InputStream is = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[2048];
            try {
                is = connection.getInputStream();
                while (maxBytes == -1 || baos.size() < maxBytes) {
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1)
                        break;
                    baos.write(buffer, 0, bytesRead);
                }
                response = baos.toByteArray();
            }
            catch (IOException ex) {
                InputStream eis = null;
                try {
                    eis = connection.getErrorStream();
                    while (maxBytes == -1 || baos.size() < maxBytes) {
                        int bytesRead = eis.read(buffer);
                        if (bytesRead == -1)
                            break;
                        baos.write(buffer, 0, bytesRead);
                    }
                    response = baos.toByteArray();
                }
                catch (Exception ex2) {
                    // throw original exception
                }
                finally {
                    if (eis != null) {
                        eis.close();
                    }
                }
                throw ex;
            }
        }
        finally {
            if (is != null)
                is.close();
            connection.disconnect();
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
            headers = new HashMap<String,String>();
            for (String headerKey : connection.getHeaderFields().keySet()) {
                if (headerKey == null)
                    headers.put("HTTP", connection.getHeaderField(headerKey));
                else
                    headers.put(headerKey, connection.getHeaderField(headerKey));
            }
        }
    }

    /**
     * Configures the connection timeout values and headers.
     */
    protected void prepareConnection(HttpURLConnection connection) throws IOException {
        if (readTimeout>=0) connection.setReadTimeout(readTimeout);
        if (connectTimeout>=0) connection.setConnectTimeout(connectTimeout);

        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
        if (user != null) {
            String value = user + ":" + password;
            connection.setRequestProperty(BASIC_AUTH_HEADER, "Basic " + new String(Base64.encodeBase64(value.getBytes())));
        }
    }

    public static String getBasicAuthHeader(String user, String password) {
        String value = user + ":" + password;
        return "Basic " + new String(Base64.encodeBase64(value.getBytes()));
    }

    /**
     * In return array, zeroth element is user and first is password.
     */
    public static String[] extractBasicAuthCredentials(String authHeader) {
        return new String(Base64.decodeBase64(authHeader.substring("Basic ".length()).getBytes())).split(":");
    }
}
