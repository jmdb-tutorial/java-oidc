package jmdb.oidc.platform.http.server;

import jmdb.oidc.platform.http.server.redirection.Redirection;
import jmdb.oidc.platform.jmdb.oidc.platform.io.SystemProcess;
import jmdb.oidc.platform.logging.LogbackConfiguration;
import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.net.InetAddress.getLocalHost;
import static java.util.Arrays.asList;
import static jmdb.oidc.platform.logging.LogbackConfiguration.STANDARD_OPS_FORMAT;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private Server server;
    private final String contextPath;
    private Servlet rootServlet;
    private String hostname;
    private String serverName;
    private int httpPort;
    private String webrootDir;
    private List<Redirection> redirections = new ArrayList<Redirection>();
    private ConstraintSecurityHandler securityCloak;
    private String sassRoot;


    public HttpServer(Class serverClass, int port, Servlet rootServlet) {
        this(serverClass.getSimpleName(), "localhost", "web", port, "/", rootServlet);
    }

    public HttpServer(String serverName, String hostname, int port, Servlet rootServlet) {
        this(serverName, hostname, "web", port, "/", rootServlet);
    }

    public HttpServer(String serverName,
                      String hostname,
                      String webrootDir,
                      int httpPort,
                      String contextPath,
                      Servlet rootServlet) {

        this.hostname = hostname;
        this.serverName = serverName;
        this.httpPort = httpPort;
        this.webrootDir = webrootDir;
        this.contextPath = contextPath;
        this.rootServlet = rootServlet;
    }


    public void start() {
        try {
            log.info(format("Starting Http Server [%s] on port [%d]...", serverName, httpPort));
            log.info(format("Serving from http://%s:%d/", hostname, httpPort));
            log.info(format("Running on host [%s]", getLocalHost().getHostName()));

            server = new Server(httpPort);

            server.setHandler(handler());


            server.start();
            new SystemProcess().writeProcessIdToFile(format(".webserver.%s.pid", serverName));

            log.info(format("Http Server Started. Serving using the dispatcher [%s] ", rootServlet.getClass().getName()));
            log.info((format("Static content is from [%s]", new File(webrootDir).getCanonicalPath())));

            server.join();

        } catch (Exception e) {
            throw new HttpServerStartupException(e);
        }
    }

    private Handler handler() {
        HandlerList handlerList = new HandlerList();

        Handler[] handlers = new Handler[]{redirectionHandler(), resourceHandler()};

        handlerList.setHandlers(handlers);


        return handlerList;
    }

    private Handler redirectionHandler() {
        return new RedirectionHandler(redirections);
    }

    private Handler resourceHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(webrootDir);

        resourceHandler.setDirectoriesListed(true);
        ContextHandler contextHandler= new ContextHandler("/");
        contextHandler.setHandler(resourceHandler);


        return contextHandler;
    }

    private ServletContextHandler servletHandler() {
        ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);


        servletContextHandler.setContextPath(contextPath);
        servletContextHandler.setResourceBase(webrootDir);

        ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setServer(server);
        servletContextHandler.setErrorHandler(errorHandler);

        servletContextHandler.addServlet(new ServletHolder(rootServlet), "/*");

        return servletContextHandler;
    }




    private File getCannonicalFileFor(String fileName) {
        try {
            return new File(fileName.replace("~", getProperty("user.home")))
                    .getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}