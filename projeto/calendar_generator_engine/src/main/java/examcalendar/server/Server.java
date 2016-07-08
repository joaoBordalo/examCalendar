package examcalendar.server;

import com.sun.net.httpserver.HttpServer;
import examcalendar.server.handlers.ParseRequestHandler;
import examcalendar.server.handlers.QueueHookRequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Created by Gustavo on 03/07/2016.
 */
public class Server {
    public static final int MAX_PARALLEL_SCHEDULING = 3;
    private LinkedBlockingQueue<Object> events;
    private Connection conn;
    private boolean running;
    private List<Scheduler> runningSchedulers;
    public static void main(String[] args) throws IOException, SQLException {
        Server server = new Server();
        server.start();
    }

    public Server() {

    }

    public void start() throws IOException, SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost/test?serverTimezone=UTC", "root", ""); // TODO (hardcoded)
        events = new LinkedBlockingQueue<Object>();
        runningSchedulers = new ArrayList<Scheduler>();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/parse", new ParseRequestHandler());
        server.createContext("/queuehook", new QueueHookRequestHandler());
        server.setExecutor(null);
        running = true;
        server.start();
        dispatcher();
    }

    private void dispatcher() {
        while (running) {
            try {
                attendNextRequest();

                // Wait for a new event
                do {
                    synchronized (events) {
                        events.wait();
                    }
                    Object event = events.poll();
                    if (event instanceof Scheduler) {
                        runningSchedulers.remove(event);
                    }
                } while (runningSchedulers.size() >= MAX_PARALLEL_SCHEDULING && running); // Keep waiting for an event if too many schedulers are already running
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     *
     * @return true if a request has been attended, false otherwise
     * @throws SQLException
     */
    private boolean attendNextRequest() throws SQLException {
        // Fetch requests from the database (in order)
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM requests" +
                " WHERE enqueueingTime IS NOT NULL AND startTime IS NULL AND endTime IS NULL ORDER BY enqueueingTime DESC LIMIT 1");
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int requestID = rs.getInt("id");
            runningSchedulers.add(new Scheduler(this, requestID));
            return true;
        }
        return false;
    }

    public void stop() {
        running = false;
        synchronized (events) {
            events.notify();
        }
    }
}
