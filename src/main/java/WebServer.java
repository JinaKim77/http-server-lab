import com.sun.net.httpserver.HttpServer;

public class WebServer {

    private HttpServer server;

    // Used for tests, don't remove
    public void shutdown() {
        System.out.println("Stopping server");
        server.stop(0);
    } 
}
