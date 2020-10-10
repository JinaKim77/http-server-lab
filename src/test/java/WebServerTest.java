import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebServerTest {
    private static WebServer webServer;

    @BeforeAll
    public static void startServer() {
        webServer = new WebServer(8099);
        webServer.startServer();
    }


    @Test
    void testStatusCheck() {
        HttpUriRequest request = new HttpGet("http://localhost:8099/status");

        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpClientBuilder.create().build().execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    void testTask() throws IOException {
        HttpPost request = getHttpPost("500,4");

        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpClientBuilder.create().build().execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String result = new String(httpResponse.getEntity().getContent().readAllBytes());
        assert(result.contains("2000"));
    }

    @Test
    void testHeaderTestTrue() throws IOException {
        HttpPost request = getHttpPost("300,6");

        request.setHeader("X-Test", "true");
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String result = new String(response.getEntity().getContent().readAllBytes());
        assertEquals(result, "123\n");
    }

    @Test
    void testHeaderTestDebug() throws IOException {
        HttpPost request = getHttpPost("300,6");
        final String debugHeader = "X-Debug";

        request.setHeader(debugHeader, "true");
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Header[] headers = response.getAllHeaders();
        System.out.println(Arrays.asList(headers));
        assertEquals(true,Arrays.asList(headers).toString().toLowerCase().contains("x-debug-info"));
    }

    private HttpPost getHttpPost(String data) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost("http://localhost:8099/task");

        request.setEntity(new StringEntity(data));
        return request;
    }

    @AfterAll
    public static void shutDown() {
        webServer.shutdown();
    }
}