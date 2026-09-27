package com.inventoryservice.inventory.grpc;

import com.hotel.roominventory.grpc.DateRange;
import com.hotel.roominventory.grpc.RequestResponse;
import com.hotel.roominventory.grpc.RoomInventoryGrpc;
import com.hotel.roominventory.grpc.RoomList;
import com.hotel.roominventory.grpc.RoomRequest;
import com.inventoryservice.inventory.entity.Room;
import com.inventoryservice.inventory.exception.ReservationNotFoundException;
import com.inventoryservice.inventory.exception.RoomNotFoundException;
import com.inventoryservice.inventory.exception.RoomUnavailableException;
import com.inventoryservice.inventory.service.RoomInventoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

// Couche "endpoint" gRPC : traduit proto <-> Java et délègue au service
@Component
public class RoomInventoryGrpcService extends RoomInventoryGrpc.RoomInventoryImplBase {

    private static final Logger logger = LoggerFactory.getLogger(RoomInventoryGrpcService.class);

    private final RoomInventoryService roomInventoryService;

    @Autowired
    public RoomInventoryGrpcService(RoomInventoryService roomInventoryService) {
        this.roomInventoryService = roomInventoryService;
    }

    @Override
    public void listAvailableRooms(DateRange request, StreamObserver<RoomList> responseObserver) {
        logger.info("gRPC ListAvailableRooms {} -> {}", request.getCheckIn(), request.getCheckOut());
        try {
            LocalDate checkIn = LocalDate.parse(request.getCheckIn());
            LocalDate checkOut = LocalDate.parse(request.getCheckOut());

            List<Room> rooms = roomInventoryService.findAvailableRooms(checkIn, checkOut);

            RoomList.Builder builder = RoomList.newBuilder();
            for (Room room : rooms) {
                builder.addRooms(com.hotel.roominventory.grpc.Room.newBuilder()
                        .setId(room.getId())
                        .setType(room.getType().getName())
                        .setPrice(roomInventoryService.computePrice(room, checkIn, checkOut))
                        .build());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException | DateTimeParseException e) {
            logger.warn("Invalid ListAvailableRooms request: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void reserveRoom(RoomRequest request, StreamObserver<RequestResponse> responseObserver) {
        logger.info("gRPC ReserveRoom room={}", request.getRoomId());
        try {
            LocalDate checkIn = LocalDate.parse(request.getDateRange().getCheckIn());
            LocalDate checkOut = LocalDate.parse(request.getDateRange().getCheckOut());
            roomInventoryService.reserveRoom(request.getRoomId(), checkIn, checkOut);
            reply(responseObserver, true, "Room " + request.getRoomId() + " reserved");
        } catch (RoomNotFoundException | RoomUnavailableException | IllegalArgumentException | DateTimeParseException e) {
            logger.warn("ReserveRoom refused: {}", e.getMessage());
            reply(responseObserver, false, e.getMessage());
        }
    }

    @Override
    public void releaseRoom(RoomRequest request, StreamObserver<RequestResponse> responseObserver) {
        logger.info("gRPC ReleaseRoom room={}", request.getRoomId());
        try {
            LocalDate checkIn = LocalDate.parse(request.getDateRange().getCheckIn());
            LocalDate checkOut = LocalDate.parse(request.getDateRange().getCheckOut());
            roomInventoryService.releaseRoom(request.getRoomId(), checkIn, checkOut);
            reply(responseObserver, true, "Room " + request.getRoomId() + " released");
        } catch (ReservationNotFoundException | DateTimeParseException e) {
            logger.warn("ReleaseRoom refused: {}", e.getMessage());
            reply(responseObserver, false, e.getMessage());
        }
    }

    private void reply(StreamObserver<RequestResponse> responseObserver, boolean success, String message) {
        responseObserver.onNext(RequestResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build());
        responseObserver.onCompleted();
    }
}