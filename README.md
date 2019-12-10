# Solution to customer service problem

## Technologies

This solution relies on:
- Spring boot : project packaging, embedded Tomcat server
- Spring data rest : create RESTful repositories for our entities
- Spring data jpa : manage database persistency
- h2 database : in memory, embedded database
- JUnit 5 : unit/integration tests

## How to package?

Maven 3 must be installed to package the application.
You must open a terminal on the application main folder, and execute the following maven command:

```
mvn package
```

It will build the solution in the folder "target".

To start the application, execute the commande:

```
java -jar demo-0.0.1-SNAPSHOT.jar
```

The server will be starting on port 8080.

### Testing the scenario

You can test the scenario manually with the provided "HAL" API browser that can be displayed in a browser: (if deployed locally) http://localhost:8080/
You can also use postman or curl for the test.

The curl commands for the test scenario are:

// Create the first message

```
curl -X POST -H "Content-Type: application/json" -d '{"author":"J\u00e9r\u00e9mie Durand", "content":"Bonjour, j\u0027ai un probl\u00e8me avec mon nouveau t\u00e9l\u00e9phone", "channel":"SMS"}' http://localhost:8080/messages
```

// Create the customer file

```
curl -X POST -H "Content-Type: application/json" -d '{"customer":"J\u00e9r\u00e9mie Durand"}' http://localhost:8080/customerFiles
```

// Attach the first message to the customer file

```
curl -i -X POST -H "Content-Type:text/uri-list" -d "http://localhost:8080/messages/1" http://localhost:8080/customerFiles/2/messages
```

// Second implementation (legacy impl) of adding the message to the customer file

```
curl -i -X POST -H "Content-Type: application/json" -d '{"messageId": 1}' http://localhost:8080/customerFiles-custom/2/messages
```

// Create the second message

```
curl -X POST -H "Content-Type: application/json" -d '{"author":"Sonia Valentin", "content":"Je suis Sonia, et je vais mettre tout en oeuvre pour vous aider. Quel est le mod\u00e8le de votre t\u00e9l\u00e9phone ?", "channel":"SMS"}' http://localhost:8080/messages
```

// Attach the second message to the customer file

```
curl -i -X POST -H "Content-Type:text/uri-list" -d "http://localhost:8080/messages/3" http://localhost:8080/customerFiles/2/messages		
```

// Alternative for linking the message to the customer file (legacy implementation)

```
curl -i -X POST -H "Content-Type: application/json" -d '{"messageId": 3}' http://localhost:8080/customerFiles-custom/2/messages
```

// Set the reference on the file

```
curl -i -X PATCH -H "Content-Type: application/json" -d '{"ref":"KA-18B6"}' http://localhost:8080/customerFiles/1
```

// Display all files

```
curl http://localhost:8080/customerFiles
```

### Unit tests / Integration tests

Also, the scenario has been implemented as an integration test in the class: DemoApplicationTests
You can execute the tests with the maven command: mvn test
