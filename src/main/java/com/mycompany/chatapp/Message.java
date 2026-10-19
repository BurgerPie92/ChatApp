package com.mycompany.chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Represents one QuickChat message and its send, disregard, or store action. */
public class Message {
    private static final int MAX_MESSAGE_LENGTH = 250;
    private static final Pattern RECIPIENT_PATTERN = Pattern.compile("^(?:\\+27\\d{9}|0\\d{9,10})$");
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("\\d{10}");
    private static final Path DEFAULT_STORAGE_FILE = Path.of("stored_messages.json");
    private static final List<Message> SENT_MESSAGES = new ArrayList<>();
    private static final Set<String> GENERATED_IDS = new HashSet<>();

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private boolean sent;
    private boolean stored;

    /** Creates a message with a unique random ten-digit ID. */
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /** Constructor with an ID argument so the hash algorithm can be tested deterministically. */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = Objects.requireNonNull(messageID, "messageID");
        this.messageNumber = messageNumber;
        this.recipient = Objects.requireNonNull(recipient, "recipient");
        this.messageText = Objects.requireNonNull(messageText, "messageText");
        this.messageHash = createMessageHash();
    }

    /** Confirms that the ID contains exactly ten numeric characters. */
    public boolean checkMessageID() {
        return MESSAGE_ID_PATTERN.matcher(messageID).matches();
    }

    /**
     * Accepts a South African international number (+27 plus nine digits) and
     * local mobile numbers used in the assignment's examples.
     */
    public String checkRecipientCell() {
        if (RECIPIENT_PATTERN.matcher(recipient).matches()) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    /** Checks the 250-character limit using the response specified for the tests. */
    public String checkMessageLength() {
        if (messageText.isBlank()) {
            return "Please enter a message.";
        }
        if (!WORD_PATTERN.matcher(messageText).find()) {
            return "Please enter a message containing at least one word.";
        }
        if (messageText.length() > MAX_MESSAGE_LENGTH) {
            return "Message exceeds 250 characters by "
                    + (messageText.length() - MAX_MESSAGE_LENGTH)
                    + "; please reduce the size.";
        }
        return "Message ready to send.";
    }

    /**
     * Builds the hash from the first two ID digits, zero-based message number,
     * and first and last words in uppercase.
     */
    public String createMessageHash() {
        if (!checkMessageID()) {
            return "Invalid Message ID.";
        }
        Matcher words = WORD_PATTERN.matcher(messageText);
        String firstWord = null;
        String lastWord = null;
        while (words.find()) {
            if (firstWord == null) {
                firstWord = words.group();
            }
            lastWord = words.group();
        }
        if (firstWord == null) {
            return "Invalid Message.";
        }
        return messageID.substring(0, 2) + ":" + messageNumber + ":"
                + firstWord.toUpperCase(Locale.ROOT) + lastWord.toUpperCase(Locale.ROOT);
    }

    /**
     * Applies the user's choice. Choices may be 1/2/3 or the corresponding words.
     * JSON output follows the string-escaping rules in RFC 8259, section 7.
     */
    public String SentMessage(String choice) {
        return SentMessage(choice, DEFAULT_STORAGE_FILE);
    }

    /** Overload accepting a storage path to keep JSON-file tests isolated. */
    public String SentMessage(String choice, Path storageFile) {
        if (!checkMessageID()) {
            return "Message ID must contain ten digits.";
        }
        String recipientStatus = checkRecipientCell();
        if (!"Cell phone number successfully captured.".equals(recipientStatus)) {
            return recipientStatus;
        }
        String messageStatus = checkMessageLength();
        if (!"Message ready to send.".equals(messageStatus)) {
            return messageStatus;
        }

        String normalizedChoice = Objects.requireNonNull(choice, "choice").trim().toLowerCase(Locale.ROOT);
        if (normalizedChoice.equals("1") || normalizedChoice.equals("send")
                || normalizedChoice.equals("send message")) {
            if (!sent) {
                SENT_MESSAGES.add(this);
                sent = true;
            }
            return "Message successfully sent.";
        }
        if (normalizedChoice.equals("2") || normalizedChoice.equals("disregard")
                || normalizedChoice.equals("discard") || normalizedChoice.equals("disregard message")) {
            return "Press 0 to delete the message.";
        }
        if (normalizedChoice.equals("3") || normalizedChoice.equals("store")
                || normalizedChoice.equals("store message")) {
            if (stored) {
                return "Message successfully stored.";
            }
            try {
                storeMessage(storageFile);
                stored = true;
                return "Message successfully stored.";
            } catch (IOException exception) {
                return "Message could not be stored: " + exception.getMessage();
            }
        }
        return "Invalid message option.";
    }

    /** Returns all sent messages in the order required by the assignment. */
    public String printMessages() {
        if (SENT_MESSAGES.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder output = new StringBuilder();
        for (Message sentMessage : SENT_MESSAGES) {
            if (output.length() > 0) {
                output.append(System.lineSeparator()).append(System.lineSeparator());
            }
            output.append(sentMessage.printSingleMessage());
        }
        return output.toString();
    }

    /** Returns the total number of messages sent in this application session. */
    public int returnTotalMessagess() {
        return SENT_MESSAGES.size();
    }

    /** Appends this message as a JSON object in a valid JSON array file. */
    public String storeMessage() throws IOException {
        return storeMessage(DEFAULT_STORAGE_FILE);
    }

    /** Appends this message to the specified JSON file. */
    public String storeMessage(Path storageFile) throws IOException {
        Objects.requireNonNull(storageFile, "storageFile");
        Path absoluteFile = storageFile.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String object = toJsonObject();
        if (!Files.exists(absoluteFile) || Files.readString(absoluteFile, StandardCharsets.UTF_8).isBlank()) {
            Files.writeString(absoluteFile, "[\n  " + object + "\n]\n", StandardCharsets.UTF_8);
            return "Message successfully stored.";
        }

        String existing = Files.readString(absoluteFile, StandardCharsets.UTF_8).trim();
        if (!existing.startsWith("[") || !existing.endsWith("]")) {
            throw new IOException("The message file does not contain a JSON array.");
        }
        String contents = existing.substring(1, existing.length() - 1).trim();
        String updated = contents.isEmpty()
                ? "[\n  " + object + "\n]\n"
                : "[\n  " + contents + ",\n  " + object + "\n]\n";
        Files.writeString(absoluteFile, updated, StandardCharsets.UTF_8);
        return "Message successfully stored.";
    }

    public String printSingleMessage() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + messageText;
    }

    public static int getTotalMessagesSent() {
        return SENT_MESSAGES.size();
    }

    /** Clears per-run data before the console application starts. */
    public static void resetSession() {
        SENT_MESSAGES.clear();
        GENERATED_IDS.clear();
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    private String toJsonObject() {
        return "{\"messageId\":\"" + escapeJson(messageID)
                + "\",\"messageNumber\":" + messageNumber
                + ",\"messageHash\":\"" + escapeJson(messageHash)
                + "\",\"recipient\":\"" + escapeJson(recipient)
                + "\",\"message\":\"" + escapeJson(messageText) + "\"}";
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private static String generateMessageID() {
        String newID;
        do {
            long randomNumber = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
            newID = Long.toString(randomNumber).substring(0, 10);
        } while (!GENERATED_IDS.add(newID));
        return newID;
    }
}
