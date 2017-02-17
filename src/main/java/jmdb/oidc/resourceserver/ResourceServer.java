package jmdb.oidc.resourceserver;

import jmdb.oidc.platform.http.server.HttpServer;
import jmdb.oidc.platform.logging.LogbackConfiguration;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.INFO;
import static jmdb.oidc.platform.logging.LogbackConfiguration.STANDARD_OPS_FORMAT;

public class ResourceServer {

    public static void main(String[] args) {
        LogbackConfiguration.initialiseConsoleLogging(INFO, STANDARD_OPS_FORMAT);
        HttpServer server = new HttpServer("resource-server", "localhost", "web", 8081, "/", new ResourceServlet());

        server.start();
    }
}
