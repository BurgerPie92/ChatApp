package com.mycompany.chatapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for the Part 2 message creation and handling features. */
class MessageTest {
    @BeforeEach
    void resetMessageSession() {
        Message.resetSession();
    }

    @Test
    void generatedMessageIdHasTenDigitsAndIsUnique() {
        Message first = new Message(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message(1, "08575975889", "Hi Keegan, did you receive the payment?");

        assertTrue(first.checkMessageID());
        assertTrue(second.checkMessageID());
        assertNotEquals(first.getMessageID(), second.getMessageID());
        assertFalse(new Message("12345", 0, "+27718693002", "Hi").checkMessageID());
    }

    @Test
    void recipientAcceptsAssignmentExamplesAndRejectsInvalidNumber() {
        assertEquals("Cell phone number successfully captured.",
                new Message(0, "+27718693002", "Hi").checkRecipientCell());
        assertEquals("Cell phone number successfully captured.",
                new Message(0, "08575975889", "Hi").checkRecipientCell());
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                new Message(0, "08966553", "Hi").checkRecipientCell());
    }

    @Test
    void messageLengthReturnsRequiredSuccessAndFailureMessages() {
        String maximumLengthMessage = "x".repeat(250);
        String tooLongMessage = "x".repeat(253);

        assertEquals("Message ready to send.",
                new Message(0, "+27718693002", maximumLengthMessage).checkMessageLength());
        assertEquals("Message exceeds 250 characters by 3; please reduce the size.",
                new Message(0, "+27718693002", tooLongMessage).checkMessageLength());
    }

    @Test
    void messageHashesUseTheIdPrefixCounterAndFirstAndLastWords() {
        Message first = new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message("9876543210", 1, "08575975889",
                "Hi Keegan, did you receive the payment?");
        Message[] messages = {first, second};
        String[] expectedHashes = {"00:0:HITONIGHT", "98:1:HIPAYMENT"};

        for (int index = 0; index < messages.length; index++) {
            assertEquals(expectedHashes[index], messages[index].createMessageHash());
        }
    }

    @Test
    void sendingMessageReturnsConfirmationAndPrintsFieldsInRequiredOrder() {
        Message message = new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");

        assertEquals("Message successfully sent.", message.SentMessage("Send Message"));
        assertEquals(1, message.returnTotalMessagess());
        assertEquals("Message ID: 0012345678" + System.lineSeparator()
                        + "Message Hash: 00:0:HITONIGHT" + System.lineSeparator()
                        + "Recipient: +27718693002" + System.lineSeparator()
                        + "Message: Hi Mike, can you join us for dinner tonight?",
                message.printMessages());
    }

    @Test
    void disregardingMessageDoesNotIncreaseSentTotal() {
        Message message = new Message("1234567890", 0, "08575975889",
                "Hi Keegan, did you receive the payment?");

        assertEquals("Press 0 to delete the message.", message.SentMessage("Disregard Message"));
        assertEquals(0, message.returnTotalMessagess());
        assertEquals("No messages have been sent.", message.printMessages());
    }

    @Test
    void storingMessageCreatesAppendableEscapedJson(@TempDir Path temporaryDirectory) throws IOException {
        Path jsonFile = temporaryDirectory.resolve("messages.json");
        Message first = new Message("0012345678", 0, "+27718693002", "Hi Mike, \"see you\" tonight.");
        Message second = new Message("9876543210", 1, "08575975889", "Hi Keegan, did you receive the payment?");

        assertEquals("Message successfully stored.", first.SentMessage("Store Message", jsonFile));
        assertEquals("Message successfully stored.", second.SentMessage("Store Message", jsonFile));

        String json = Files.readString(jsonFile);
        assertTrue(json.startsWith("["));
        assertTrue(json.stripTrailing().endsWith("]"));
        assertTrue(json.contains("\\\"see you\\\""));
        assertTrue(json.contains("0012345678"));
        assertTrue(json.contains("9876543210"));
        assertEquals(0, first.returnTotalMessagess());
    }

    @Test
    void assignmentScenarioSendsFirstAndDisregardsSecondMessage() {
        Message first = new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message("9876543210", 1, "08575975889",
                "Hi Keegan, did you receive the payment?");

        assertEquals("Message successfully sent.", first.SentMessage("1"));
        assertEquals("Press 0 to delete the message.", second.SentMessage("2"));
        assertEquals(1, first.returnTotalMessagess());
        assertTrue(first.printMessages().contains("Hi Mike, can you join us for dinner tonight?"));
        assertFalse(first.printMessages().contains("Hi Keegan, did you receive the payment?"));
    }
}
