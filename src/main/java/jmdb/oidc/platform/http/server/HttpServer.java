package jmdb.oidc.platform.http.server;

import jmdb.oidc.platform.collections.Maps;
import jmdb.oidc.platform.http.server.redirection.Redirection;
import jmdb.oidc.platform.jmdb.oidc.platform.io.SystemProcess;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.jetbrains.annotations.NotNull;
import org.keycloak.adapters.jetty.KeycloakJettyAuthenticator;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.net.InetAddress.getLocalHost;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Stream.of;
import static jmdb.oidc.platform.collections.Maps.entriesToMap;
import static jmdb.oidc.platform.collections.Maps.entry;
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

        ConstraintSecurityHandler securityHandler = createSecurityHandler(this.serverName, authenticator(), loginService());

        securityHandler.setHandler(handlerList);

        SessionHandler sessionHandler = createSessionHandler();
        sessionHandler.setHandler(securityHandler);

        return sessionHandler;
    }

    @NotNull
    private SessionHandler createSessionHandler() {
        return new SessionHandler();
    }

    /**
     * You get these variables from the installation config in your client config in the
     * keycloak admin app
     */
    private Authenticator authenticator() {
        KeycloakJettyAuthenticator authenticator =  new KeycloakJettyAuthenticator();
        AdapterConfig config = new AdapterConfig();
        config.setRealm("test-realm");
        config.setAuthServerUrl("http://localhost:8080/auth");
        config.setSslRequired("external");
        config.setResource("test-client");

        config.setCredentials(unmodifiableMap(of(
            entry("secret", "6b0f7d59-283d-4afe-b13e-747158d84086")).collect(entriesToMap())));

        authenticator.setAdapterConfig(config);
        return authenticator;
    }

    private LoginService loginService() {
        return null;
    }


    private Handler redirectionHandler() {
        return new RedirectionHandler(redirections);
    }

    private Handler resourceHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(webrootDir);

        resourceHandler.setDirectoriesListed(true);
        ContextHandler contextHandler = new ContextHandler("/");
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

    private static ConstraintSecurityHandler createSecurityHandler(String serverName,
                                                                   Authenticator authenticator,
                                                                   LoginService loginService) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"sysadmin"});


        ConstraintMapping rootConstraint = mapConstraintTo(constraint, "/sysadmin/*");

        ConstraintSecurityHandler handler = new ConstraintSecurityHandler();
        handler.setRealmName(serverName);
        handler.setAuthenticator(authenticator);
        handler.setConstraintMappings(asList(rootConstraint));
        handler.setLoginService(loginService);
        return handler;
    }

    private static ConstraintMapping mapConstraintTo(Constraint constraint, String path) {
        ConstraintMapping cm = new ConstraintMapping();
        cm.setPathSpec(path);
        cm.setConstraint(constraint);
        return cm;
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