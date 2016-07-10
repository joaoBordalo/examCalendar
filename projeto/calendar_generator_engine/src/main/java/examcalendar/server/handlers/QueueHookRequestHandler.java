package examcalendar.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import examcalendar.server.Server;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Gustavo on 06/07/2016.
 */
public class QueueHookRequestHandler extends AbstractRequestHandler {
    private Server server;

    public QueueHookRequestHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if (method.equals("POST")) {
                this.sendSuccessResponse(exchange, JSONObject.NULL, 200);
                server.notifyEvent(Server.Event.ENQUEUEING);
            } else {
                // Method not allowed
                JSONObject data = new JSONObject();
                data.put("method", "Method \"" + method + "\" not allowed.");
                exchange.getResponseHeaders().add("Allow", "POST");
                this.sendFailResponse(exchange, data, 405);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
