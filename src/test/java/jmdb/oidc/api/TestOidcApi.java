package jmdb.oidc.api;

import jmdb.oidc.platform.HttpTestBase;
import jmdb.oidc.platform.http.client.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

import static jmdb.oidc.platform.http.client.Http.addBasicAuthzHeader;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestOidcApi extends HttpTestBase {
    private final String realmName = "test-realm";

    public TestOidcApi() {
        super("http://localhost:8080/auth");
    }

    @Test
    public void can_initiate_auth_sequence() {
        HttpGet get_wellKown = new HttpGet(url("/realms/%s/.well-known/openid-configuration", realmName));

        HttpResponse welknownResponse = http.execute(get_wellKown);

        assertThat(welknownResponse.statusCode(), is(HttpStatus.SC_OK));
        System.out.println(welknownResponse);

    }
}
