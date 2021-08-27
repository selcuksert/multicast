package com.corp.concepts.multicast.receiver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class ReceiverVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(ReceiverVerticle.class);

    private DatagramSocket socket;

    private static void displayNetworkAdapters() {
        if (LOGGER.isDebugEnabled()) {
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                networkInterfaces.asIterator().forEachRemaining(nif ->
                {
                    try {
                        LOGGER.info(String.format("Available network interface: %s | multicast: %b", nif.getName(), nif.supportsMulticast()));
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                });
            } catch (SocketException e) {
                LOGGER.error("Error:", e);
            }
        }
    }

    @Override
    public void start() throws Exception {
        JsonObject config = config();

        JsonObject multicastConfig = config.getJsonObject("multicast");
        JsonObject receiverConfig = config.getJsonObject("receiver");
        JsonObject senderConfig = config.getJsonObject("sender");

        String multicastHost = multicastConfig.getString("host");

        String receiverHost = receiverConfig.getString("host");
        int receiverPort = receiverConfig.getInteger("port");
        boolean logUdpActivity = receiverConfig.getBoolean("logUdpActivity");
        String networkInterface = receiverConfig.getString("networkInterface");

        int messageSizeInBytes = senderConfig.getInteger("messageSizeInBytes");
        int maxNoOfMessages = senderConfig.getInteger("maxNoOfMessages");

        DatagramSocketOptions options = new DatagramSocketOptions();
        options.setLogActivity(logUdpActivity);

        int actualBufferSize = messageSizeInBytes + new StringBuilder("[" + maxNoOfMessages + "] ").length();
        options.setReceiveBufferSize(actualBufferSize);

        displayNetworkAdapters();

        socket = vertx.createDatagramSocket(options);
        socket.listen(receiverPort, receiverHost, asyncResult -> {
            if (asyncResult.succeeded()) {
                // join the multicast group
                try {
                    NetworkInterface nif = NetworkInterface.getByName(networkInterface);
                    asyncResult.result().listenMulticastGroup(multicastHost, nif.getName(), null, asyncResultMcast -> {
                        if (asyncResultMcast.succeeded()) {
                            LOGGER.info(String.format("Started listening on %s:%d [%s]", receiverHost, receiverPort, nif.getName()));
                            asyncResult.result().handler(packet -> {
                                String message = new String(packet.data().getBytes(), StandardCharsets.UTF_8);
                                LOGGER.info(String.format("Received message: %s", message));
                                if(message.equals("STOP")) {
                                    vertx.close();
                                }
                            });
                        } else {
                            LOGGER.error("Error:", asyncResult.cause());
                        }
                    });
                } catch (SocketException e) {
                    LOGGER.error("Error:", e);
                }
            } else {
                LOGGER.error("Error:", asyncResult.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        if (socket != null) {
            JsonObject config = config();

            JsonObject multicastConfig = config.getJsonObject("multicast");
            JsonObject receiverConfig = config.getJsonObject("receiver");

            String multicastHost = multicastConfig.getString("host");
            String networkInterface = receiverConfig.getString("networkInterface");

            NetworkInterface nif = NetworkInterface.getByName(networkInterface);

            socket.unlistenMulticastGroup(multicastHost, nif.getName(), null, asyncResultMcast -> {
                if (asyncResultMcast.succeeded()) {
                    socket.close(asyncResult -> {
                        if (asyncResult.succeeded()) {
                            LOGGER.info("Closed socket");
                        } else {
                            LOGGER.error("Error while stopping verticle:", asyncResult.cause());
                        }
                    });
                } else {
                    LOGGER.error("Error while stopping verticle:", asyncResultMcast.cause());
                }
            });
        }
    }

}
