package jmdb.oidc.api;

import jmdb.oidc.platform.HttpTestBase;
import jmdb.oidc.platform.http.client.HttpResponse;
import jmdb.oidc.platform.http.client.JsonResponseHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static jmdb.oidc.platform.http.client.Http.addBasicAuthzHeader;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestOidcApi extends HttpTestBase {
    private final String realmName = "test-realm";

    public TestOidcApi() {
        super("http://localhost:8080/auth");
    }

    @Test
    public void can_initiate_auth_sequence() throws Exception {
        HttpGet get_wellKnown = new HttpGet(url("/realms/%s/.well-known/openid-configuration", realmName));

        HttpResponse wellknownResponse = http.execute(get_wellKnown);

        assertThat(wellknownResponse.statusCode(), is(SC_OK));
        System.out.println(wellknownResponse);

        String token_endpoint = wellknownResponse.stringValue("token_endpoint");

        HttpPost post_token = new HttpPost(token_endpoint);

        List<NameValuePair> form_data = new ArrayList<>();
        form_data.add(new BasicNameValuePair("client_id", "test-client"));
        form_data.add(new BasicNameValuePair("client_secret", "6b0f7d59-283d-4afe-b13e-747158d84086"));
        form_data.add(new BasicNameValuePair("username", "test_user"));
        form_data.add(new BasicNameValuePair("password", "foo"));
        form_data.add(new BasicNameValuePair("grant_type", "password"));
        post_token.setEntity(new UrlEncodedFormEntity(form_data));

        HttpResponse tokenResponse = http.execute(post_token);

        assertThat(tokenResponse.statusCode(), is(SC_OK));
        System.out.println(tokenResponse);
    }
}
