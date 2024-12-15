package carparkingsystem;

import java.io.*;
import java.util.*;

class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private String role;

    public User(String userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return userId + "," + name + "," + email + "," + password + "," + role;
    }

    public static User fromString(String line) {
        String[] parts = line.split(",");
        return new User(parts[0], parts[1], parts[2], parts[3], parts[4]);
    }
}

class ParkingSlot {
    private int slotId;
    private String status;
    private String carNumber;

    public ParkingSlot(int slotId) {
        this.slotId = slotId;
        this.status = "Available";
        this.carNumber = "";
    }

    public int getSlotId() {
        return slotId;
    }

    public String getStatus() {
        return status;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void parkCar(String carNumber) {
        this.status = "Occupied";
        this.carNumber = carNumber;
    }

    public void unparkCar() {
        this.status = "Available";
        this.carNumber = "";
    }

    @Override
    public String toString() {
        return slotId + "," + status + "," + carNumber;
    }

    public static ParkingSlot fromString(String line) {
        String[] parts = line.split(",");
        ParkingSlot slot = new ParkingSlot(Integer.parseInt(parts[0]));
        slot.status = parts[1];
        slot.carNumber = parts[2];
        return slot;
    }
}

public class CarParkingSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<User> users = new ArrayList<>();
    private static final List<ParkingSlot> slots = new ArrayList<>();
    private static final String USERS_FILE = "users.txt";
    private static final String SLOTS_FILE = "slots.txt";

    public static void main(String[] args) {
        ensureFilesExist();
        loadUsersFromFile();
        loadSlotsFromFile();

        System.out.println("Welcome to the Car Parking System!");
        while (true) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    saveUsersToFile();
                    saveSlotsToFile();
                    System.out.println("Thank you for using the system!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void ensureFilesExist() {
        try {
            File usersFile = new File(USERS_FILE);
            File slotsFile = new File(SLOTS_FILE);

            if (!usersFile.exists()) {
                usersFile.createNewFile();
            }

            if (!slotsFile.exists()) {
                slotsFile.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error ensuring files exist: " + e.getMessage());
        }
    }

    private static void register() {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        if (getUserById(userId) != null) {
            System.out.println("User ID already exists. Try a different one.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Role (Admin/User): ");
        String role = scanner.nextLine();

        if (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("User")) {
            System.out.println("Invalid role. Registration failed.");
            return;
        }

        User user = new User(userId, name, email, password, role);
        users.add(user);
        System.out.println("Registration successful!");
    }

    private static void login() {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = getUserById(userId);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("Invalid credentials. Try again.");
            return;
        }

        if (user.getRole().equalsIgnoreCase("Admin")) {
            adminMenu();
        } else {
            userMenu();
        }
    }

    private static void adminMenu() {
        System.out.println("Admin Login Successful!");
        while (true) {
            System.out.println("\n1. Add Parking Slot");
            System.out.println("2. View Parking Slots");
            System.out.println("3. Remove Car from Slot");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addParkingSlot();
                case 2 -> viewParkingSlots();
                case 3 -> removeCarFromSlot();
                case 4 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void userMenu() {
        System.out.println("User Login Successful!");
        while (true) {
            System.out.println("\n1. Park Car");
            System.out.println("2. View Parking Slots");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> parkCar();
                case 2 -> viewParkingSlots();
                case 3 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addParkingSlot() {
        System.out.print("Enter Slot ID: ");
        int slotId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (getSlotById(slotId) != null) {
            System.out.println("Slot ID already exists.");
            return;
        }

        slots.add(new ParkingSlot(slotId));
        System.out.println("Parking slot added successfully!");
    }

    private static void viewParkingSlots() {
        if (slots.isEmpty()) {
            System.out.println("No parking slots available.");
            return;
        }

        System.out.println("Slot ID | Status      | Car Number");
        System.out.println("---------------------------------");
        for (ParkingSlot slot : slots) {
            System.out.printf("%-7d | %-11s | %s%n", slot.getSlotId(), slot.getStatus(), slot.getCarNumber());
        }
    }

    private static void parkCar() {
        System.out.print("Enter Car Number: ");
        String carNumber = scanner.nextLine();

        ParkingSlot slot = getAvailableSlot();
        if (slot == null) {
            System.out.println("No available parking slots.");
            return;
        }

        slot.parkCar(carNumber);
        System.out.println("Car parked successfully in slot " + slot.getSlotId());
    }

    private static void removeCarFromSlot() {
        System.out.print("Enter Slot ID: ");
        int slotId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        ParkingSlot slot = getSlotById(slotId);
        if (slot == null || slot.getStatus().equals("Available")) {
            System.out.println("Slot is either invalid or already empty.");
            return;
        }

        slot.unparkCar();
        System.out.println("Car removed from slot " + slotId);
    }

    private static void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(User.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    private static void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.write(user.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static void loadSlotsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SLOTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                slots.add(ParkingSlot.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading parking slots: " + e.getMessage());
        }
    }

    private static void saveSlotsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SLOTS_FILE))) {
            for (ParkingSlot slot : slots) {
                writer.write(slot.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving parking slots: " + e.getMessage());
        }
    }

    private static User getUserById(String userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    private static ParkingSlot getSlotById(int slotId) {
        for (ParkingSlot slot : slots) {
            if (slot.getSlotId() == slotId) {
                return slot;
            }
        }
        return null;
    }

    private static ParkingSlot getAvailableSlot() {
        for (ParkingSlot slot : slots) {
            if (slot.getStatus().equals("Available")) {
                return slot;
            }
        }
        return null;
    }
}
