package com.corp.concepts.multicast.sender;

import com.github.javafaker.Faker;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.stream.IntStream;

public class SenderVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(SenderVerticle.class);
    private static final Faker FAKER = new Faker();

    private DatagramSocket socket;

    /**
     * Sends message over socket
     *
     * @param host      UDP receiver host
     * @param port      UDP receiver port
     * @param message   Message
     * @param messageNo Message number
     * @param socket    The UDP socket object
     */
    private static void sendMessage(String host, int port, String message, int messageNo, DatagramSocket socket) {
        Buffer data = Buffer.buffer(message);

        socket.send(data, port, host, asyncResult -> {
            if (LOGGER.isDebugEnabled() && asyncResult.succeeded()) {
                LOGGER.debug(String.format("Message sent: %s", new String(data.getBytes(), StandardCharsets.UTF_8)));
            } else {
                LOGGER.error("Error sending test data:", asyncResult.cause());
            }
        });
    }

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
        JsonObject senderConfig = config.getJsonObject("sender");

        String multicastHost = multicastConfig.getString("host");
        int multicastPort = multicastConfig.getInteger("port");

        int maxNoOfMessages = senderConfig.getInteger("maxNoOfMessages");
        boolean logUdpActivity = senderConfig.getBoolean("logUdpActivity");
        int messageSizeInBytes = senderConfig.getInteger("messageSizeInBytes");

        DatagramSocketOptions options = new DatagramSocketOptions();
        options.setLogActivity(logUdpActivity);

        int actualBufferSize = messageSizeInBytes + new StringBuilder("[" + maxNoOfMessages + "] ").length();
        options.setSendBufferSize(actualBufferSize);

        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        displayNetworkAdapters();

        socket = vertx.createDatagramSocket(options);

        super.start();

        // To send messages in sequential order use blocking execution
        vertx.executeBlocking(f -> {
            LOGGER.debug("Sending {} messages.", maxNoOfMessages);
            IntStream.range(1, maxNoOfMessages + 1).forEach(messageNo -> {
                StringBuilder sb = new StringBuilder();
                sb.append("[").append(messageNo).append("] ").append(FAKER.lorem().characters(1, messageSizeInBytes));
                sendMessage(multicastHost, multicastPort, sb.toString(), messageNo, socket);
            });
            sendMessage(multicastHost, multicastPort, "STOP", -1, socket);
            vertx.close();
        }, true);
    }

    @Override
    public void stop() throws Exception {
        if (socket != null) {
            socket.close(asyncResult -> {
                if (asyncResult.succeeded()) {
                    LOGGER.info("Closed socket");
                } else {
                    LOGGER.error("Error while stopping verticle:", asyncResult.cause());
                }
            });
        }
    }

}
