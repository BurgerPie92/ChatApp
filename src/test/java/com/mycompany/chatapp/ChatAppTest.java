package com.mycompany.chatapp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** End-to-end tests for the Part 2 console flow. */
class ChatAppTest {

    @Test
    void failedLoginDoesNotOpenQuickChat() {
        String output = runChat(
                "Clayton", "Burger", "a_b", "Passw0rd!", "+27123456789",
                "a_b", "wrong-password");

        assertTrue(output.contains("Username or password incorrect, please try again."));
        assertFalse(output.contains("Welcome to QuickChat."));
        assertFalse(output.contains("Send Messages"));
    }

    @Test
    void welcomeMenuComingSoonAndQuitWorkAfterSuccessfulLogin() {
        String output = runChat(
                "Clayton", "Burger", "a_b", "Passw0rd!", "+27123456789",
                "a_b", "Passw0rd!", "0", "2", "3");

        assertTrue(output.contains("Welcome to QuickChat."));
        assertTrue(output.contains("1) Send Messages"));
        assertTrue(output.contains("2) Show recently sent messages"));
        assertTrue(output.contains("3) Quit"));
        assertTrue(output.contains("Coming Soon."));
        assertTrue(output.contains("Total messages sent: 0"));
    }

    @Test
    void messageLoopStopsAtUserLimitAndCountsOnlySentMessages() {
        String output = runChat(
                "Clayton", "Burger", "a_b", "Passw0rd!", "+27123456789",
                "a_b", "Passw0rd!", "1", "1",
                "+27718693002", "Hi Mike, can you join us for dinner tonight?", "1",
                "1", "3");

        assertTrue(output.contains("Message ID generated: "));
        assertTrue(output.contains("Message Hash: "));
        assertTrue(output.contains("Message successfully sent."));
        assertTrue(output.contains("You have reached the message limit."));
        assertTrue(output.contains("Total messages sent: 1"));
        assertTrue(output.indexOf("Message ID: ") < output.indexOf("Message Hash: "));
        assertTrue(output.indexOf("Message Hash: ") < output.indexOf("Recipient: "));
        assertTrue(output.indexOf("Recipient: ") < output.indexOf("Message: "));
    }

    private static String runChat(String... inputLines) {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        String input = String.join("\n", inputLines) + "\n";

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
            ChatApp.main(new String[0]);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
        return capturedOutput.toString(StandardCharsets.UTF_8);
    }
}
