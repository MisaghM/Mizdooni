package application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Utils {
    static public boolean validateUsername(String username) {
        String usernameFormat = "^\\w+$";
        return username.matches(usernameFormat);
    }

    static public boolean validateEmail(String email) {
        String emailFormat = "^\\w+@\\w+\\.\\w+$";
        return email.matches(emailFormat);
    }

    static public boolean userIsTaken(String username, String email, List<User> users) {
        for (User u : users) {
            if (u.getUsername().equals(username) || u.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

    static public User findUser(String username, List<User> users) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    static public User findManager(String username, List<User> users) {
        for (User u : users) {
            if (u.getUsername().equals(username) && u.getRole() == User.Role.manager) {
                return u;
            }
        }
        return null;
    }

    static public boolean validateWorkingTime(LocalTime time) {
        return time.getMinute() == 0;
    }

    static public Restaurant findRestaurantByName(String name, List<Restaurant> restaurants) {
        for (Restaurant r : restaurants) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }

    static public boolean validateSeatsNumber(String seatsNumber) {
        String seatsNumberFormat = "^[1-9][0-9]*$";
        return seatsNumber.matches(seatsNumberFormat);
    }

    static public boolean validateManagerRestaurant(User managerUser, Restaurant restaurant) {
        return restaurant.getManager().equals(managerUser);
    }

    static public List<Restaurant> findRestaurantsByType(String type, List<Restaurant> restaurants) {
        List<Restaurant> result = new ArrayList<>();
        for (Restaurant r : restaurants) {
            if (r.getType().equals(type)) {
                result.add(r);
            }
        }
        return result;
    }

    static public List<String> convertToString(Map<LocalDate, List<LocalTime>> availableHours){
        List<String> timeAndDate = new ArrayList<>();
        for (Map.Entry<LocalDate, List<LocalTime>> entry : availableHours.entrySet()) {
            for (LocalTime time : entry.getValue()) {
                LocalDateTime datetime = LocalDateTime.of(entry.getKey(), time);
                timeAndDate.add(datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
        }
        return timeAndDate;
    }
}
