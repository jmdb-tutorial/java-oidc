package jmdb.oidc.platform;

import jmdb.oidc.platform.http.client.Http;
import org.junit.After;
import org.junit.Before;

import static java.lang.String.format;

/**
 * Created by jmdb on 21/08/2015.
 */
public abstract class HttpTestBase {

    protected final Http http = new Http();
    protected final String baseUrl;

    public HttpTestBase(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Before
    public void init_http() {
        http.init();
    }

    @After
    public void close_http() throws Exception {
        http.destroy();
    }

    protected String url(String path, Object... parameters) {
        String parametersSubstituted = format(path, parameters);
        return format("%s%s", baseUrl, parametersSubstituted).replaceAll("\\+", "%20");
    }
}
