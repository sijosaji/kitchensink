# Member Management Spring Boot Application

This is a Spring Boot application for managing members. It provides a REST API for performing CRUD operations on members, with added features like role-based authorization and rate limiting.

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [License](#license)

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
    git clone https://github.com/yourusername/member-management.git
    cd member-management
    ```

2. **Build the Project**:
    ```bash
    mvn clean install
    ```

## Configuration

1. **MongoDB Setup**:
   - Ensure MongoDB is running.
   - Update the `application.properties` file located in the `src/main/resources` directory with your MongoDB connection details:

     ```properties
     spring.data.mongodb.uri=mongodb://localhost:27017/member_management
     ```

2. **Application Properties**:
   - You can configure additional properties like server port, rate limiter settings, etc., in the `application.properties` file.

     ```properties
     server.port=9001
     rate.limiter.enabled=true
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

The application will start on `http://localhost:9001`.

## API Endpoints

### `GET /api/members`

Retrieve a list of all members, ordered by name in ascending order.

- **Response**:
  - `200 OK`: A list of members.
  
- **Curl**:
  ```bash
  curl -X GET "http://localhost:9001/rest/members" -H "Authorization: Bearer <token>"
  ```
### `GET /api/members/{id}`

Retrieve a member by its ID.
- **Response**:
  - `200 OK`: The member details.
  - `404 NOT FOUND`: If the member is not found.
  
- **Curl**:
  ```bash
  curl -X GET "http://localhost:9001/rest/members/1" -H "Authorization: Bearer <token>"
  ```
  
### `POST /api/members`

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
  - **200 OK**: If the member is successfully created.
  - **409 Conflict**: If member email is already taken.

- **Curl**:
    ```bash
    curl -X POST http://localhost:8080/rest/members \
    -H "Authorization: Bearer <token> \
    -d '{
          "name": "name",
          "email": "name@mail.com",
          "phoneNumber": "8368452188"
        }'
    ```

### `PATCH /api/members/{id}`

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
  - **200 OK**: If the member is successfully updated.
  - **409 Conflict**: If member email is already taken.
  - **404 NOT FOUND**: If the member is not found.

- **Curl**:
    ```bash
    curl -X PATCH "{base_url}/api/members/{id}" \
    -H "Authorization: Bearer {token}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{
          "name": "Jane Doe",
          "email": "janedoe@example.com",
          "phone": "0987654321"
    }'
    ```

### `DELETE /api/members/{id}`

Delete a member by its ID.
- **Response**:
  - `204 NO CONTENT`: If the member details are successfully deleted.
  - `404 NOT FOUND`: If the member is not found.
  
- **Curl**:
  ```bash
  curl -X DELETE "{base_url}/api/members/{id}" \
  -H "Authorization: Bearer {token}"
  ```
    
  

    

