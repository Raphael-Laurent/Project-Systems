
package com.bookingservice.booking.grpc;

import com.hotel.roominventory.grpc.RoomInventoryGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${room-inventory.grpc.host}")
    private String host;

    @Value("${room-inventory.grpc.port}")
    private int port;

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel roomInventoryChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public RoomInventoryGrpc.RoomInventoryBlockingStub roomInventoryStub(ManagedChannel roomInventoryChannel) {
        return RoomInventoryGrpc.newBlockingStub(roomInventoryChannel);
    }
}