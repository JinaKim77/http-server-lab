import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebServer {

    //to define paths
    public static final String TASK_ENDPOINT="/task";
    public static final String STATUS_ENDPOINT="/status";

    private final int port;
    private HttpServer server;

    //constructor
    public WebServer(int port){
        this.port=port;
    }

    public static void main(String[] args){
        int serverPort=8000;

        if(args.length==1){
            serverPort=Integer.parseInt(args[0]);
        }

        WebServer webServer=new WebServer(serverPort);
        webServer.startServer();

        System.out.println("Server is listening on port "+serverPort);

    }


    public void startServer(){
        try {
            this.server=HttpServer.create(new InetSocketAddress(port),0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpContext statusContext=server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext=server.createContext(TASK_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("get")){
            exchange.close();
            return;
        }

        System.out.println("Called status check endpoint \n");

        String responseMessage="Server is alive\n";
        sendResponse(responseMessage.getBytes(),exchange);

    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }

    //to "only" handle post request
    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("post")){
            exchange.close();
            return;
        }

        System.out.println("Called task endpoint \n");

        Headers headers = exchange.getRequestHeaders();
        if(headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")){
            String dummyResponse="123\n";
            sendResponse(dummyResponse.getBytes(), exchange);
            return;
        }

        boolean isDebugMode = false;

        if(headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")){
            isDebugMode=true;
        }

        long startTime = System.nanoTime();

        //This allows us to pull all the bytes that are in the request
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();

        byte[] responseBytes = calculateResponse(requestBytes);

        long endTime = System.nanoTime();
        if(isDebugMode){
            String debugMessage = String.format("Operation took %d na\n", endTime-startTime);

            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(responseBytes,exchange);

    }

    private byte[] calculateResponse(byte[] requestBytes) {
        String bodyString = new String(requestBytes);

        String[] stringNumbers = bodyString.split(",");

        BigInteger result = BigInteger.ONE;

        for(String number:stringNumbers){
            BigInteger integer = new BigInteger(number);
            result=result.multiply(integer);
        }

        return String.format("Result of multiplication is: %s\n",result).getBytes();
    }

    // Used for tests, don't remove
    public void shutdown() {
        System.out.println("Stopping server");
        server.stop(0);
    } 
}
