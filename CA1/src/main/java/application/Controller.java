package application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import exceptions.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    final private MizDooni mizdooni;
    final private ObjectMapper objectMapper;
    final private DateTimeFormatter datetimeFormatter;

    public Controller(MizDooni mizdooni) {
        this.mizdooni = mizdooni;
        this.objectMapper = new ObjectMapper();
        this.datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    private JsonNode stringToJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private JsonNode createResultJson(boolean success, JsonNode data) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("success", success);
        node.set("data", data);
        return node;
    }

    public JsonNode addUser(String json) {
        JsonNode node = stringToJson(json);
        String username = node.get("username").asText();
        String password = node.get("password").asText();
        String email = node.get("email").asText();
        Address address;
        User.Role role;

        try {
            String country = node.get("address").get("country").asText();
            String city = node.get("address").get("city").asText();
            address = new Address(country, city, null);
        } catch (NullPointerException ex) {
            return createResultJson(false, TextNode.valueOf("Address is not complete."));
        }

        try {
            role = User.Role.valueOf(node.get("role").asText());
        } catch (IllegalArgumentException ex) {
            return createResultJson(false, TextNode.valueOf("Role is not valid."));
        }

        boolean success = true;
        String data = "User added successfully.";

        try {
            mizdooni.addUser(username, password, email, address, role);
        } catch (InvalidUsernameFormat | InvalidEmailFormat | DuplicatedUsernameEmail ex) {
            success = false;
            data = ex.getMessage();
        }

        return createResultJson(success, TextNode.valueOf(data));
    }

    public JsonNode addRestaurant(String json) {
        JsonNode node = stringToJson(json);
        String name = node.get("name").asText();
        String manager = node.get("managerUsername").asText();
        String type = node.get("type").asText();
        String description = node.get("description").asText();
        Address address;

        LocalTime startTime = LocalTime.parse(node.get("startTime").asText());
        LocalTime endTime = LocalTime.parse(node.get("endTime").asText());

        try {
            String country = node.get("address").get("country").asText();
            String city = node.get("address").get("city").asText();
            String street = node.get("address").get("street").asText();
            address = new Address(country, city, street);
        } catch (NullPointerException ex) {
            return createResultJson(false, TextNode.valueOf("Address is not complete."));
        }

        boolean success = true;
        String data = "Restaurant added successfully.";

        try {
            mizdooni.addRestaurant(name, manager, type, startTime, endTime, description, address);
        } catch (DuplicatedRestaurantName | ManagerNotFound | InvalidWorkingTime ex) {
            success = false;
            data = ex.getMessage();
        }

        return createResultJson(success, TextNode.valueOf(data));
    }

    public JsonNode addTable(String json) {
        JsonNode node = stringToJson(json);
        int tableNumber = node.get("tableNumber").asInt();
        String restaurantName = node.get("restaurantName").asText();
        String manager = node.get("managerUsername").asText();
        String seatsNumber = node.get("seatsNumber").asText();

        boolean success = true;
        String data = "Table added successfully.";

        try {
            mizdooni.addTable(tableNumber, restaurantName, manager, seatsNumber);
        } catch (DuplicatedTableNumber | InvalidSeatsNumber | RestaurantNotFound | ManagerNotFound |
                 InvalidManagerRestaurant ex) {
            success = false;
            data = ex.getMessage();
        }

        return createResultJson(success, TextNode.valueOf(data));
    }

    public JsonNode reserveTable(String json) {
        JsonNode node = stringToJson(json);
        String username = node.get("username").asText();
        String restaurantName = node.get("restaurantName").asText();
        int tableNumber = node.get("tableNumber").asInt();
        String datetimeString = node.get("datetime").asText();

        boolean success;
        JsonNode data;

        try {
            LocalDateTime datetime = LocalDateTime.parse(datetimeString, datetimeFormatter);
            Reservation reservation = mizdooni.reserveTable(username, restaurantName, tableNumber, datetime);
            success = true;
            data = objectMapper.createObjectNode().put("reservationNumber", reservation.getReservationNumber());
        } catch (DateTimeParseException ex) {
            success = false;
            data = TextNode.valueOf("Invalid datetime format");
        } catch (UserNotFound | ManagerReservationNotAllowed | InvalidWorkingTime | RestaurantNotFound | TableNotFound |
                 InvalidDateTime | ReservationNotInOpenTimes | TableAlreadyReserved ex) {
            success = false;
            data = TextNode.valueOf(ex.getMessage());
        }

        return createResultJson(success, data);
    }

    public JsonNode cancelReservation(String json) {
        JsonNode node = stringToJson(json);
        String username = node.get("username").asText();
        int reservationNumber = node.get("reservationNumber").asInt();

        boolean success;
        JsonNode data;

        try {
            mizdooni.cancelReservation(username, reservationNumber);
            success = true;
            data = TextNode.valueOf("Reservation cancelled successfully.");
        } catch (UserNotFound | ReservationNotFound | ReservationCannotBeCancelled ex) {
            success = false;
            data = TextNode.valueOf(ex.getMessage());
        }

        return createResultJson(success, data);
    }
    }

    public JsonNode searchRestaurantsByName(String json) {
        JsonNode node = stringToJson(json);
        String restaurantName = node.get("name").asText();

        boolean success;
        JsonNode data;

        try {
            Restaurant restaurant = mizdooni.searchRestaurantsByName(restaurantName);
            List<JsonNode> result = new ArrayList<>();
            result.add(restaurant.toJson());
            success = true;
            data = objectMapper.createObjectNode().set("restaurants", objectMapper.valueToTree(result));
        } catch (RestaurantNotFound ex) {
            success = false;
            data = TextNode.valueOf("No restaurant found.");
        }

        return createResultJson(success, data);
    }

    public JsonNode searchRestaurantsByType(String json) {
        JsonNode node = stringToJson(json);
        String restaurantType = node.get("type").asText();

        boolean success;
        JsonNode data;

        try {
            List<Restaurant> restaurants = mizdooni.searchRestaurantsByType(restaurantType);
            List<JsonNode> restaurantJsons = new ArrayList<>();
            for (Restaurant r : restaurants) {
                restaurantJsons.add(r.toJson());
            }
            success = true;
            data = objectMapper.createObjectNode().set("restaurants", objectMapper.valueToTree(restaurantJsons));

        } catch (RestaurantNotFound ex) {
            success = false;
            data = TextNode.valueOf("No restaurant found.");
        }

        return createResultJson(success, data);
    }

    public void showAvailableTables() {

    }

    public void addReview() {

    }
}
