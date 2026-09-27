package com.inventoryservice.inventory.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServerRunner {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerRunner.class);

    @Value("${grpc.server.port}")
    private int port;

    private final RoomInventoryGrpcService roomInventoryGrpcService;
    private Server server;

    @Autowired
    public GrpcServerRunner(RoomInventoryGrpcService roomInventoryGrpcService) {
        this.roomInventoryGrpcService = roomInventoryGrpcService;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(roomInventoryGrpcService)
                .build()
                .start();
        logger.info("gRPC server started on port {}", port);

        Thread awaitThread = new Thread(() -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "grpc-server");
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            logger.info("Stopping gRPC server");
            server.shutdown();
        }
    }
}