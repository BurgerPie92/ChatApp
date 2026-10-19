/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.chatapp;

import java.util.Scanner;
import java.util.regex.Pattern;

/** Console entry point for registration, login, and the QuickChat menu. */
public class ChatApp {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            runApplication(scanner);
        }
    }

    private static void runApplication(Scanner scanner) {
        Message.resetSession();

        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Enter a username: ");
        String username = scanner.nextLine();
        while (!Login.isValidUsername(username)) {
            System.out.println("Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.");
            System.out.print("Please enter a valid username: ");
            username = scanner.nextLine();
        }

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();
        while (!Login.isValidPassword(password)) {
            System.out.println("Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
            System.out.print("Please enter a password that meets the complexity requirements: ");
            password = scanner.nextLine();
        }

        System.out.print("Enter a South African cell phone number: ");
        String cellPhoneNumber = scanner.nextLine();
        while (!Login.isValidPhoneNumber(cellPhoneNumber)) {
            System.out.println("Please input a South African number starting with +27.");
            System.out.print("Please enter a valid South African cell phone number: ");
            cellPhoneNumber = scanner.nextLine();
        }

        Login user = new Login(firstName, lastName, username, password, cellPhoneNumber);
        System.out.println(user.registerUser());

        System.out.print("Enter your username to log in: ");
        String loginUsername = scanner.nextLine();
        System.out.print("Enter your password to log in: ");
        String loginPassword = scanner.nextLine();
        String loginStatus = user.returnLoginStatus(loginUsername, loginPassword);
        System.out.println(loginStatus);

        if (!user.loginUser(loginUsername, loginPassword)) {
            return;
        }

        System.out.println("Welcome to QuickChat.");
        int messageLimit = readNonNegativeInteger(scanner, "How many messages would you like to enter? ");
        int messagesCreated = 0;
        boolean totalDisplayed = false;
        boolean running = true;

        if (messageLimit == 0) {
            printTotalMessages();
            totalDisplayed = true;
        }

        while (running) {
            System.out.println("\nPlease choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            int option = readInteger(scanner, "Enter 1, 2, or 3: ");

            switch (option) {
                case 1 -> {
                    if (messagesCreated >= messageLimit) {
                        System.out.println("You have reached the message limit.");
                    } else {
                        for (int messageNumber = messagesCreated; messageNumber < messageLimit; messageNumber++) {
                            Message message = createMessage(scanner, messageNumber);
                            System.out.println("Message ID generated: " + message.getMessageID());
                            int action = readMessageAction(scanner);
                            String actionStatus = message.SentMessage(action);
                            System.out.println(actionStatus);

                            if (action == 1 && actionStatus.startsWith("Message successfully sent")) {
                                System.out.println(message.printSingleMessage());
                            } else if (action == 2) {
                                System.out.print("Enter 0 to delete this discarded message, or press Enter to continue: ");
                                if ("0".equals(scanner.nextLine().trim())) {
                                    System.out.println("Message deleted.");
                                }
                            }

                            messagesCreated++;
                        }
                        if (!totalDisplayed && messagesCreated >= messageLimit) {
                            printTotalMessages();
                            totalDisplayed = true;
                        }
                    }
                }
                case 2 -> System.out.println("Coming Soon.");
                case 3 -> {
                    if (!totalDisplayed) {
                        printTotalMessages();
                        totalDisplayed = true;
                    }
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }

    private static Message createMessage(Scanner scanner, int messageNumber) {
        String recipient;
        while (true) {
            System.out.print("Enter the recipient cell number: ");
            recipient = scanner.nextLine().trim();
            Message recipientCheck = new Message(messageNumber, recipient, "placeholder");
            String recipientStatus = recipientCheck.checkRecipientCell();
            if (!"Cell phone number successfully captured.".equals(recipientStatus)) {
                System.out.println(recipientStatus);
                continue;
            }
            break;
        }

        while (true) {
            System.out.print("Enter your message (maximum 250 characters): ");
            String text = scanner.nextLine();
            Message draft = new Message(messageNumber, recipient, text);
            String messageStatus = draft.checkMessageLength();
            if (!"Message ready to send.".equals(messageStatus)) {
                if (messageStatus.startsWith("Message exceeds")) {
                    System.out.println("Please enter a message of less than 250 characters.");
                } else {
                    System.out.println(messageStatus);
                }
                continue;
            }
            return draft;
        }
    }

    private static int readMessageAction(Scanner scanner) {
        while (true) {
            System.out.println("Choose what to do with this message:");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int action = readInteger(scanner, "Enter 1, 2, or 3: ");
            if (action >= 1 && action <= 3) {
                return action;
            }
            System.out.println("Invalid option. Please choose 1, 2, or 3.");
        }
    }

    private static int readNonNegativeInteger(Scanner scanner, String prompt) {
        while (true) {
            int value = readInteger(scanner, prompt);
            if (value >= 0) {
                return value;
            }
            System.out.println("Please enter zero or a positive whole number.");
        }
    }

    private static int readInteger(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static void printTotalMessages() {
        System.out.println("Total messages sent: " + Message.getTotalMessagesSent());
    }

}

/**
 * Handles registration validation and authentication for the chat application.
 */
class Login {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.{1,5}$)[A-Za-z0-9]*_[A-Za-z0-9]*$");
    // Reference list for the South African cell phone regular expression:
    // 1. Oracle, Java Regular Expressions: https://docs.oracle.com/javase/tutorial/essential/regex/
    // 2. Oracle, java.util.regex.Pattern API: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
    // 3. ICASA, Numbering Plan Regulations: https://www.icasa.org.za/legislation-and-regulations/numbering-plan-regulations
    // The pattern checks the international code (+27) followed by nine digits.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+27\\d{9}$");

    private final String firstName;
    private final String lastName;
    private final String username;
    private final String password;
    private final String cellPhoneNumber;
    private boolean registered;

    public Login(String firstName, String lastName, String username,
                 String password, String cellPhoneNumber) {
        this.firstName = requireValue(firstName, "firstName");
        this.lastName = requireValue(lastName, "lastName");
        this.username = requireValue(username, "username");
        this.password = requireValue(password, "password");
        this.cellPhoneNumber = requireValue(cellPhoneNumber, "cellPhoneNumber");
    }

    public boolean checkUserName() {
        // Check that the username contains an underscore and is no longer than five characters.
        return isValidUsername(username);
    }

    public boolean checkPasswordComplexity() {
        // Check that the password has at least eight characters.
        // Check that it contains a capital letter, a number, and a special character.
        return isValidPassword(password);
    }

    /**
     * Validates the South African international format +27 followed by nine digits.
     * Reference: South African Government, Telephone Numbering Plan, ICASA.
     */
    public boolean checkCellPhoneNumber() {
        // Check that the number starts with South Africa's international code (+27)
        // and is followed by exactly nine digits.
        return isValidPhoneNumber(cellPhoneNumber);
    }

    // These helper methods allow the console to validate input before creating the Login object.
    static boolean isValidUsername(String value) {
        return value != null && USERNAME_PATTERN.matcher(value).matches();
    }

    static boolean isValidPassword(String value) {
        return value != null
                && value.length() >= 8
                && value.matches(".*[A-Z].*")
                && value.matches(".*\\d.*")
                && value.matches(".*[^A-Za-z0-9].*");
    }

    static boolean isValidPhoneNumber(String value) {
        return value != null && PHONE_PATTERN.matcher(value).matches();
    }

    public String registerUser() {
        // Stop registration and return a message if the username is invalid.
        if (!checkUserName()) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }

        // Stop registration and return a message if the password is invalid.
        if (!checkPasswordComplexity()) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }

        // Stop registration and return a message if the phone number is invalid.
        if (!checkCellPhoneNumber()) {
            return "Cell phone number incorrectly formatted or does not contain international code.";
        }

        // All validation checks passed, so mark the user as registered.
        registered = true;
        return "User registered successfully.";
    }

    public boolean loginUser(String enteredUsername, String enteredPassword) {
        // Login succeeds only when registration was successful and both credentials match.
        return registered && username.equals(enteredUsername) && password.equals(enteredPassword);
    }

    public String returnLoginStatus(String enteredUsername, String enteredPassword) {
        // Return a welcome message when the login details are correct.
        if (loginUser(enteredUsername, enteredPassword)) {
            return "Welcome " + firstName + ", " + lastName + ", it is great to see you again.";
        }

        // Return an error message when the login details are incorrect.
        return "Username or password incorrect, please try again.";
    }

    private static String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value;
    }
}


