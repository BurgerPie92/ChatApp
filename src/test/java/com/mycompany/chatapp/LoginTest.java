package com.mycompany.chatapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/** Unit tests for the registration and login feature in ChatApp.java. */
class LoginTest {
    private Login validLogin() {
        return new Login("David", "Williams", "d_w", "N3w!Pass9", "+27111222333");
    }

    @Test
    void usernameIsCorrectlyFormatted() {
        assertTrue(validLogin().checkUserName());
    }

    @Test
    void usernameIsIncorrectlyFormatted() {
        Login login = new Login("David", "Williams", "kyle!!!!!", "N3w!Pass9", "+27111222333");
        assertFalse(login.checkUserName());
        assertTrue(login.registerUser().startsWith("Username is not correctly formatted"));
    }

    @Test
    void passwordMeetsComplexityRequirements() {
        assertTrue(validLogin().checkPasswordComplexity());
    }

    @Test
    void passwordDoesNotMeetComplexityRequirements() {
        Login login = new Login("David", "Williams", "d_w", "password", "+27111222333");
        assertFalse(login.checkPasswordComplexity());
        assertTrue(login.registerUser().startsWith("Password is not correctly formatted"));
    }

    @Test
    void cellPhoneNumberIsCorrectlyFormatted() {
        assertTrue(validLogin().checkCellPhoneNumber());
    }

    @Test
    void cellPhoneNumberIsIncorrectlyFormatted() {
        Login login = new Login("David", "Williams", "d_w", "N3w!Pass9", "0112345678");
        assertFalse(login.checkCellPhoneNumber());
        assertEquals(
                "Cell phone number incorrectly formatted or does not contain international code.",
                login.registerUser());
    }

    @Test
    void loginIsSuccessfulWithCorrectDetails() {
        Login login = validLogin();
        assertEquals("User registered successfully.", login.registerUser());
        assertTrue(login.loginUser("d_w", "N3w!Pass9"));
        assertEquals("Welcome David, Williams, it is great to see you again.",
                login.returnLoginStatus("d_w", "N3w!Pass9"));
    }

    @Test
    void loginFailsWithIncorrectDetails() {
        Login login = validLogin();
        login.registerUser();
        assertFalse(login.loginUser("a_s", "wrong"));
        assertEquals("Username or password incorrect, please try again.",
                login.returnLoginStatus("a_s", "wrong"));
    }
}

