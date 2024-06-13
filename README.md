# Technical Communication Platform for Programmers

This project is a technical communication platform designed for programmers, built using a robust technology stack to handle high concurrency and provide efficient communication features.

## Table of Contents
1. [Project Description](#project-description)
2. [Features](#features)
3. [Technology Stack](#technology-stack)
4. [Architecture](#architecture)
5. [Installation](#installation)
6. [Usage](#usage)
7. [Contributing](#contributing)
8. [License](#license)

## Project Description
The platform serves as a communication hub for programmers, enabling them to discuss technical topics, share knowledge, and collaborate on various projects. It includes functionalities such as user authentication, posting and commenting on topics, and asynchronous message notifications.

## Features
- **Front-end and Back-end Separation**: Achieves high cohesion and low coupling through JSON data parsing.
- **High Concurrency**: Utilizes Redis for cluster deployment and multiple master nodes for efficient writing services.
- **Asynchronous Message Notifications**: Kafka is used to persist messages to queues and handle asynchronous notifications.
- **Timed Task Execution**: Spring Quartz is used to calculate scores of popular posts regularly.
- **Redis Cache Warm-up**: Popular posts are queried from MySQL and cached in Redis to improve performance.
- **Verification Code Functionality**: Verification codes are sent via third-party services, stored in Redis, and checked for consistency.

## Technology Stack
- **Backend**: SpringBoot, SSM (Spring, Spring MVC, MyBatis)
- **Messaging and Caching**: Redis, Kafka, Elasticsearch
- **Security**: Spring Security, MD5 encryption, Kaptcha for verification codes
- **Scheduling**: Spring Quartz
- **Database**: MySQL
- **Caching**: Redis, Caffeine
- **Frontend**: JSON data parsing for high cohesion and low coupling

## Architecture

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/fredwuzheng/Community.git
   ```
2. Navigate to the project directory:
   ```bash
   cd communication
   ```

3. Install dependencies:
   ```bash
   mvn install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```


## Usage
1. Register a new account or log in with an existing account.
2. Navigate to the forums to post topics, comment, and interact with other users.
3. Utilize the search functionality to find specific topics or posts.
4. Receive real-time notifications for new messages or updates.

## Contributing
Contributions are welcome! Please read the [contributing guidelines](CONTRIBUTING.md) first. You can submit issues and pull requests through GitHub.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.


