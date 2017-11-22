package zoomdata.util;

/**
 * Created by qcluu on 4/18/15.
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class ZoomdataWebsocketClient extends WebSocketClient {
    private final static Logger logger = Logger.getLogger(ZoomdataWebsocketClient.class);
    // final String rcvMessage = null;
    final Map<String,JSONObject> mapPendingRequests = new HashMap<>();
    boolean done = false;
    int count = 0;
    JSONObject jsonResponseData = null;
    String requestMessage = null;
    final StringBuilder messageBuffer = new StringBuilder();
   //  private URI serverUri;
    public ZoomdataWebsocketClient(URI serverUri, Draft draft) {
        super( serverUri, draft );
    }

    public ZoomdataWebsocketClient(URI serverURI) {
        super( serverURI );
        // this.serverUri = serverURI;
    }

    public ZoomdataWebsocketClient(String serverURI) throws Exception {
        super(new URI(serverURI));

    }

    public void sendHandshake() {

    }
    public void connectAndSend (final String message) {
        super.connect();
        logger.info("connectAndSend");
        requestMessage = message;
    }

    public JSONObject getJsonResponseData() {
        return jsonResponseData;
    }

    public static ZoomdataWebsocketClient mainRun(String inUrl) throws URISyntaxException {
        // Start a new thread.
        final String defaultUrl = "ws://localhost:8887";
        String url = defaultUrl;
        if (StringUtils.isEmpty(inUrl) == false) {
            url = "wss://echo.websocket.org";
        }
        url = "ws://echo.websocket.org";
        url = "wss://dev.zoomdata.com/zoomdata/websocket?key=5535290060b209053232a0b1";
        url = "ws://dev.zoomdata.com:8080/zoomdata/websocket?key=55353a4560b209053233703c";
        // url = "wss://52.5.35.247:8443/zoomdata/websocket?key=55380306e4b059df0c5266bc";

        // ZoomdataWebsocketClient c = new ZoomdataWebsocketClient( new URI( url), new Draft_10() ); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        final ZoomdataWebsocketClient client = new ZoomdataWebsocketClient(new URI(url));
        client.connect();
        return client;
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public void onOpen( ServerHandshake handshakedata ) {
        System.out.println( "opened connection" );
        done = false;
        if (requestMessage != null) {
            send(requestMessage);
        } else {
            try {
                final String jsonRequestData = "{\"type\":\"START_VIS\",\"cid\":\"f997fb5e486d52c2b889d3b9f7ec7314\",\"request\":{\"streamSourceId\":\"55251b4660b2742518058717\",\"reflines\":[],\"axis\":[{\"axis\":\"X Axis\",\"metrics_name\":\"Volume\",\"from\":null,\"to\":null,\"label\":\"\",\"logScaleEnabled\":false,\"gridEnabled\":true,\"step\":null}],\"tz\":\"America/New_York\",\"speed\":1,\"cfg\":{\"pauseAfterRead\":false,\"player\":\"OFF\",\"group\":{\"fields\":[{\"name\":\"state\",\"limit\":50,\"sort\":{\"name\":\"count\",\"dir\":\"desc\"}}],\"metrics\":[]}}}}";
                final JSONObject jsonRequest = new JSONObject(jsonRequestData);

                final String cid = jsonRequest.getString("cid");
                mapPendingRequests.put(cid, jsonRequest);
                send(jsonRequest.toString());
                // send("{\"type\":\"START_VIS\",\"cid\":\"f997fb5e486d52c2b889d3b9f7ec7314\",\"request\":{\"streamSourceId\":\"55251b4660b2742518058717\",\"reflines\":[],\"axis\":[{\"axis\":\"X Axis\",\"metrics_name\":\"Volume\",\"from\":null,\"to\":null,\"label\":\"\",\"logScaleEnabled\":false,\"gridEnabled\":true,\"step\":null}],\"tz\":\"America/New_York\",\"speed\":1,\"cfg\":{\"pauseAfterRead\":false,\"player\":\"OFF\",\"group\":{\"fields\":[{\"name\":\"state\",\"limit\":50,\"sort\":{\"name\":\"count\",\"dir\":\"desc\"}}],\"metrics\":[]}}}}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void send(String message) {
        super.send(message);
        done = false;
    }
    @Override
    public void onMessage( String message ) {
        logger.info("got message: " + message);
        if (logger.isDebugEnabled()) {
            logger.debug(Thread.currentThread() + ":" + ": received: " + message);
        }
        try {
            final JSONObject jsonResponse = new JSONObject(message);
            final String cid = jsonResponse.getString("cid");
            final JSONObject jsonRequest = mapPendingRequests.get(cid);
            if (jsonRequest != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("*** Found matching json request cid=" + cid);
                }
            }
             String status;

            try {
                final String error = jsonResponse.getString("error");
                if (StringUtils.isEmpty(error) == false) {
                    // Got error. Declare done.
                    logger.error("Error: Got error from source. Details: " +jsonResponse + ". Request=" + jsonRequest);
                    done = true;
                    return;
                }
            } catch (Exception e) {
                // No error. It's ok.
            }

            try {
                status = jsonResponse.getString("status");
                if (status.equals("NO_DATA_FOUND") == true) {
                    logger.info("*** NO_DATA_FOUND ***");
                    done = true;
                    jsonResponseData = jsonResponse;
                }
            } catch (Exception e) {
                status = "";
                messageBuffer.append(message);
                logger.warn("Warn: missing status field. It's ok. Move on! Details: " + e.getMessage());
               //  e.printStackTrace();
            }

            if (status.equals("NOT_DIRTY_DATA") == true) {
                if (logger.isDebugEnabled()) {
                    logger.debug("got not dirty data. response completed. messageBuffer len=" + messageBuffer.length());
                }
                jsonResponseData = new JSONObject(messageBuffer.toString());
                jsonResponseData.put("status", "OK");
                mapPendingRequests.put(cid, jsonResponseData);
                logger.info("Processed json response OK");
                // Process response here and return to client.
                done = true;
            } else {
                logger.info("Continue waiting for data ...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFragment( Framedata fragment ) {
        messageBuffer.append(new String(fragment.getPayloadData().array() ));
        // System.out.println("received fragment: " + new String(fragment.getPayloadData().array() ) );
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
        done = true;
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println( Thread.currentThread() +": Connection closed by " + ( remote ? "remote peer" : "us" ) );
    }

    @Override
    public void onError( Exception ex ) {
        ex.printStackTrace();
        done = true;
        // if the error is fatal then onClose will be called additionally
    }
}