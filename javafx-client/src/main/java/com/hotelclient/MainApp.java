package com.hotelclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.rmi.server.LogStream.log;

public class MainApp extends Application {

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // liste des chambres
    private ListView<String> roomListView = new ListView<>();
    // affichage des erreurs
    private TextArea logArea = new TextArea();

    @Override
    public void start(Stage stage) {

        // zone de recherche
        TextField checkInField = new TextField("2026-10-01");
        TextField checkOutField = new TextField("2026-10-03");
        Button searchBtn = new Button("Chercher les chambres disponibles");
        searchBtn.setOnAction(e -> searchRooms(checkInField.getText(), checkOutField.getText()));

        HBox searchRow = new HBox(10, new Label("Check-in:"), checkInField, new Label("Check-out:"), checkOutField, searchBtn);
        searchRow.setPadding(new Insets(10));

        // zone de réservation
        TextField roomIdField = new TextField();
        TextField guestField = new TextField();
        Button bookBtn = new Button("Réserver une chambre");
        bookBtn.setOnAction(e -> bookRoom(roomIdField.getText(), guestField.getText(), checkInField.getText(), checkOutField.getText()));

        HBox bookRow = new HBox(10, new Label("Room ID:"), roomIdField, new Label("Guest:"), guestField, bookBtn);
        bookRow.setPadding(new Insets(10));

        // zone d'annulation
        TextField bookingIdField = new TextField();
        Button cancelBtn = new Button("Annuler réservation");
        cancelBtn.setOnAction(e -> cancelBooking(bookingIdField.getText()));

        HBox cancelRow = new HBox(10, new Label("Booking ID:"), bookingIdField, cancelBtn);
        cancelRow.setPadding(new Insets(10));

        logArea.setEditable(false);
        logArea.setPrefHeight(150);

        VBox root = new VBox(10, searchRow, roomListView, bookRow, cancelRow, new Label("Log:"), logArea);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 700, 500));
        stage.setTitle("Hotel Booking");
        stage.show();
    }

    private void searchRooms(String checkIn, String checkOut) {
        try {
            String url = BASE_URL + "/rooms/available?checkIn=" + checkIn + "&checkOut=" + checkOut;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log("Erreur serveur (" + response.statusCode() + "): " + response.body());
                return;
            }

            roomListView.getItems().clear();
            JsonNode rooms = mapper.readTree(response.body());

            if (!rooms.isArray()) {
                log("Réponse inattendue: " + response.body());
                return;
            }

            for (JsonNode room : rooms) {
                roomListView.getItems().add("Room " + room.get("id").asText()
                        + " - " + room.get("type").asText()
                        + " - " + room.get("price").asText() + "€");
            }
            log("Recherche OK: " + rooms.size() + " chambre(s) trouvée(s)");
        } catch (Exception ex) {
            log("Erreur recherche: " + ex.getMessage());
        }
    }

    private void bookRoom(String roomId, String guest, String checkIn, String checkOut) {
        try {
            String json = String.format(
                    "{\"roomId\":%s,\"guestName\":\"%s\",\"checkIn\":\"%s\",\"checkOut\":\"%s\"}",
                    roomId, guest, checkIn, checkOut);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/bookings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                log("Réservation créée: " + response.body());
            } else {
                log("Erreur (" + response.statusCode() + "): " + response.body());
            }
        } catch (Exception ex) {
            log("Erreur réservation: " + ex.getMessage());
        }
    }


    private void cancelBooking(String bookingId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/bookings/" + bookingId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log("Annulation (" + response.statusCode() + ")");
        } catch (Exception ex) {
            log("Erreur annulation: " + ex.getMessage());
        }
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
