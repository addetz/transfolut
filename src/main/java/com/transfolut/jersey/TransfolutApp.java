package com.transfolut.jersey;

import com.transfolut.TransfolutPaths;
import com.transfolut.bank.BankService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.IOException;
import java.net.URI;

/**
 * Main App class of our Transfolut Bank Transfer Service.
 *
 * @author addetz
 */
public class TransfolutApp {

    public static String getGreeting() {
        return String.format(" Welcome to the Transfolut Application! Listening at "
                + "%s\nHit enter to stop it...", TransfolutPaths.BASE_URI);
    }

    /**
     * Main method that starts the Grizzly server.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println(new TransfolutApp().getGreeting());
        final HttpServer server = startServer();
        System.in.read();
        server.shutdownNow();
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * https://stackoverflow.com/questions/31992461/how-to-run-jersey-server-webservice-server-without-using-tomcat
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        final ResourceConfig rc = new ResourceConfig().packages("com.transfolut.jersey");


        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TransfolutPaths.BASE_URI), rc);
    }
}
