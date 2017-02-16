package jmdb.oidc.platform.http.client;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.Map;

public interface ResponseHandler {
    Map processResponseBody(CloseableHttpResponse response, StringBuffer responseBody) throws IOException;
}
