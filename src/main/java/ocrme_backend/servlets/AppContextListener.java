package ocrme_backend.servlets;

import com.google.appengine.api.ThreadManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by iuliia on 7/2/17.
 */

public class AppContextListener implements ServletContextListener {
    private static final int THREAD_NUMB = 2;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        ThreadFactory factory = ThreadManager.currentRequestThreadFactory();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMB, factory);
        final ServletContext servletContext = servletContextEvent.getServletContext();
        servletContext.setAttribute("threadPoolAlias", executor);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // following code is just to free resources occupied by thread pool when web application is undeployed
        final ExecutorService threadPool = (ExecutorService) servletContextEvent.getServletContext().getAttribute("threadPoolAlias");
        threadPool.shutdown();
    }
}
