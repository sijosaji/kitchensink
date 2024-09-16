# KitchenSink Spring Boot Application

This is a Spring Boot application for managing members. It provides a REST API for performing CRUD operations on members, with added features like role-based authorization and rate limiting.

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)

## Features
- **Member Management**: Create, read, update, and delete members.
- **Authorization**: Role-based access control for various operations.
- **Rate Limiting**: Limits the number of requests to protect the API from abuse.
- **Validation**: Ensures member data integrity with validation rules.

## Requirements

To build and run this application, you need the following installed on your system:

- Java 21
- Maven 3.6+
- MongoDB (locally or remotely)
- An IDE (e.g., IntelliJ IDEA, Eclipse, VS Code)

## Installation

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/sijosaji/kitchensink.git
    cd kitchensink
    ```

2. **Build the Project**:
    ```bash
    mvn clean install
    ```

## Configuration

 **MongoDB Setup**:
   - Ensure MongoDB is running.
   - Update the `application.properties` file located in the `src/main/resources` directory with your MongoDB connection details
   - Example :

     ```properties
     spring.data.mongodb.uri=mongodb://localhost:27017/mongo_migration
     ```

## Running the Application

You can run the application in two ways:

1. **Using Maven**:
    ```bash
    mvn spring-boot:run
    ```

2. **Using the Executable JAR**:
    ```bash
    java -jar target/member-management-0.0.1-SNAPSHOT.jar
    ```

The application will start on `http://localhost:8080`.

## API Endpoints

### `GET /kitchensink/rest/members`

Retrieve a list of all members, ordered by name in ascending order.

- **Response**:
  - `200 OK`: A list of members.
  
- **Curl**:
  ```bash
  curl -X GET "http://localhost:8080/kitchensink/rest/members" -H "Authorization: Bearer <token>"
  ```
### `GET /kitchensink/rest/members/{id}`

Retrieve a member by its ID.
- **Response**:
  - `200 OK`: The member details.
  - `404 NOT FOUND`: If the member is not found.
  
- **Curl**:
  ```bash
  curl -X GET "http://localhost:8080/kitchensink/rest/members/1" -H "Authorization: Bearer <token>"
  ```
  
### `POST /kitchensink/rest/members`

Create a new member.

- **Request**:
  - **Body**: `Member` - JSON object containing member details:
    ```json
    {
         "name": "name",
         "email": "name@mail.com",
         "phoneNumber": "8368452188"
    }
    ```
- **Response**:
  - `200 OK`: If the member is successfully created.
  - `409 Conflict`: If member email is already taken.
  - `400 Bad Request`: If any of member fields validation fails.

- **Curl**:
    ```bash
    curl -X POST http://localhost:8080/kitchensink/rest/members \
    -H "Authorization: Bearer <token> \
    -d '{
          "name": "name",
          "email": "name@mail.com",
          "phoneNumber": "8368452188"
        }'
    ```

### `PATCH /kitchensink/rest/members/{id}`

Update a Member.

- **Request**:
  - **Body**: `Member` - JSON object containing member details to be updated:
    ```json
      {
      "name": "Jane Doe",
      "email": "janedoe@example.com",
      "phone": "0987654321"
      }
    ```
- **Response**:
  - `200 OK`: If the member is successfully updated.
  - `409 Conflict`: If member email is already taken.
  - `404 NOT FOUND`: If the member is not found.

- **Curl**:
    ```bash
    curl -X PATCH "http://localhost:8080/kitchensink/rest/members/{id}" \
    -H "Authorization: Bearer <token>" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{
          "name": "Jane Doe",
          "email": "janedoe@example.com",
          "phone": "0987654321"
    }'
    ```

### `DELETE /kitchensink/rest/members/{id}`

Delete a member by its ID.
- **Response**:
  - `204 NO CONTENT`: If the member details are successfully deleted.
  - `404 NOT FOUND`: If the member is not found.
  
- **Curl**:
  ```bash
  curl -X DELETE "http://localhost:8080/kitchensink/rest/members/{id}" \
  -H "Authorization: Bearer <token>"
  ```
    
  

    

