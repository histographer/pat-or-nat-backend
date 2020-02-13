package no.digipat.patornat.mongodb.listener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Handles mongodb singleton connection
 */
@WebListener
public class MongoDBContextListener implements ServletContextListener {


    /**
     * Initialize a mongoclient and make it available for servlets to use
     * @param servletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            ServletContext context = servletContextEvent.getServletContext();
            String host = System.getenv("PATORNAT_MONGODB_HOST");
            String portString = System.getenv("PATORNAT_MONGODB_PORT");
            int port = Integer.parseInt(portString);
            String username = System.getenv("PATORNAT_MONGODB_USERNAME");
            String password = System.getenv("PATORNAT_MONGODB_PASSWORD");
            String database = System.getenv("PATORNAT_MONGODB_DATABASE");
            MongoClientURI  MONGO_URI = new MongoClientURI("mongodb://"+username+":"+password+"@"+host+":"+port+"/"+database);
            
            
            MongoClient client = new MongoClient(MONGO_URI);
            context.log("Mongoclient connected successfully at "+host+":"+port);
            context.setAttribute("MONGO_CLIENT", client);
        } catch(Exception error) {
            throw new RuntimeException("Mongoclient initialization failed", error);
        }
    }


    /**
     * When the context is destroyed, terminate the mongodb connection
     * @param servletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        MongoClient client = (MongoClient) context.getAttribute(("MONGO_CLIENT"));
        client.close();
        context.log("Mongo connection terminated");
    }
}