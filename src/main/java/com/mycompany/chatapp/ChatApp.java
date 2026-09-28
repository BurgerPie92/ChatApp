/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.chatapp;

import java.util.regex.Pattern;
import java.util.Scanner;

/**
 *
 * @author Clayton.Burger
 */
public class ChatApp {

    public static void main(String[] args) {
        // Create a Scanner so the user can enter details in the console.
        Scanner scanner = new Scanner(System.in);

        // Ask the user for the details needed to create an account.
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

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

        // Create a Login object containing the user's registration details.
        Login user = new Login(
                firstName, lastName, username, password, cellPhoneNumber);

        // Validate the details and register the user.
        System.out.println(user.registerUser());

        // Ask the user for login details after registration.
        System.out.print("Enter your username to log in: ");
        String loginUsername = scanner.nextLine();
        System.out.print("Enter your password to log in: ");
        String loginPassword = scanner.nextLine();

        // Check whether the login details match the registered details.
        System.out.println(user.returnLoginStatus(loginUsername, loginPassword));

        // Run the built-in checks for the Login class.
        runUnitTests();

        // Close the Scanner after all console input has been read.
        scanner.close();
    }

    /** Runs the same checks previously stored in LoginTest.java. */
    private static void runUnitTests() {
        Login validLogin = new Login("David", "Williams", "d_w", "N3w!Pass9", "+27111222333");

        // Username tests.
        check(validLogin.checkUserName(), "Valid username should pass");
        check(!new Login("David", "Williams", "kyle!!!!!", "N3w!Pass9", "+27111222333").checkUserName(),
                "Invalid username should fail");

        // Password tests.
        check(validLogin.checkPasswordComplexity(), "Valid password should pass");
        check(!new Login("David", "Williams", "d_w", "password", "+27111222333").checkPasswordComplexity(),
                "Invalid password should fail");

        // Cell phone tests.
        check(validLogin.checkCellPhoneNumber(), "Valid phone number should pass");
        check(!new Login("David", "Williams", "d_w", "N3w!Pass9", "0112345678").checkCellPhoneNumber(),
                "Invalid phone number should fail");

        // Registration and successful login tests.
        check("User registered successfully.".equals(validLogin.registerUser()),
                "Valid registration should succeed");
        check(validLogin.loginUser("d_w", "N3w!Pass9"), "Valid login should succeed");
        check("Welcome David, Williams, it is great to see you again."
                        .equals(validLogin.returnLoginStatus("d_w", "N3w!Pass9")),
                "Successful login message should be returned");

        // Failed registration and failed login tests.
        Login invalidUsername = new Login("David", "Williams", "kyle!!!!!", "N3w!Pass9", "+27111222333");
        check(invalidUsername.registerUser().startsWith("Username is not correctly formatted"),
                "Invalid registration should fail");
        check(!validLogin.loginUser("a_s", "wrong"), "Incorrect password should fail");

        System.out.println("All unit tests passed.");
    }

    private static void check(boolean condition, String failureMessage) {
        if (!condition) {
            throw new AssertionError(failureMessage);
        }
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


