
package com.bookingservice.booking.grpc;

import com.bookingservice.booking.web.dto.RoomDto;
import com.hotel.roominventory.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RoomInventoryClient {

    private static final Logger logger = LoggerFactory.getLogger(RoomInventoryClient.class);

    private final RoomInventoryGrpc.RoomInventoryBlockingStub stub;

    @Autowired
    public RoomInventoryClient(RoomInventoryGrpc.RoomInventoryBlockingStub stub) {
        this.stub = stub;
    }

    public List<RoomDto> listAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        logger.info("gRPC ListAvailableRooms {} -> {}", checkIn, checkOut);
        RoomList roomList = stub().listAvailableRooms(dateRange(checkIn, checkOut));
        return roomList.getRoomsList().stream()
                .map(room -> new RoomDto(room.getId(), room.getType(), room.getPrice()))
                .toList();
    }

    public RequestResponse reserveRoom(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        logger.info("gRPC ReserveRoom room={} {} -> {}", roomId, checkIn, checkOut);
        return stub().reserveRoom(roomRequest(roomId, checkIn, checkOut));
    }

    public RequestResponse releaseRoom(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        logger.info("gRPC ReleaseRoom room={} {} -> {}", roomId, checkIn, checkOut);
        return stub().releaseRoom(roomRequest(roomId, checkIn, checkOut));
    }

    // Timeout de 5 s pour ne pas bloquer si le serveur ne répond pas
    private RoomInventoryGrpc.RoomInventoryBlockingStub stub() {
        return stub.withDeadlineAfter(5, TimeUnit.SECONDS);
    }

    private DateRange dateRange(LocalDate checkIn, LocalDate checkOut) {
        return DateRange.newBuilder()
                .setCheckIn(checkIn.toString())   // format yyyy-MM-dd
                .setCheckOut(checkOut.toString())
                .build();
    }

    private RoomRequest roomRequest(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return RoomRequest.newBuilder()
                .setRoomId(roomId)
                .setDateRange(dateRange(checkIn, checkOut))
                .build();
    }
}