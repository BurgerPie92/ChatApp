# ChatApp

Console application for the Programming 1A POE. Part 1 provides registration and login; Part 2 adds the QuickChat message menu and message handling.

## Requirements

- JDK 17
- Apache Maven (NetBeans includes Maven support for Maven projects)

## Run in NetBeans

Open this project folder in NetBeans. Run the `ChatApp` main class. The user must register and log in successfully before the QuickChat menu is shown.

## Run from a terminal

From the project folder, run:

```text
mvn test
mvn package
java -cp target/classes com.mycompany.chatapp.ChatApp
```

The application asks for the maximum number of message attempts before showing its menu. It supports sending messages, the required `Coming Soon.` option, and quitting. Stored messages are appended to `stored_messages.json` in JSON array format.

## Part 2 tests

The JUnit tests cover generated ten-digit message IDs, assignment recipient examples, message length responses, message hashes, sending and disregarding, total sent count, output field order, and JSON storage. GitHub Actions runs the Maven test suite on pushes and pull requests.
