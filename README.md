# Preventive Health

A comprehensive application designed to help users track and manage their health history and preventive care measures and make appointments with one tap. 

## Overview

Preventive Health is a platform focused on empowering users to take a proactive approach to their health. The application provides reminders for preventive screenings and check-ups, and offers personalized health recommendations based on user profiles.

## Features

- **Preventive Care Calendar**: Schedule and receive reminders for preventive examinations
- **Personalized Recommendations**: Get health reminder advice tailored to your age, gender, and health profile
- **Medical Records Management**: Store and access your medical history securely. Share your medical history with other doctors(readOnly)
- **Find specialists**: Find specialists in different cities and their ratings. Make an appointment fast and easy.

## Installation

### Prerequisites
- Node.js (v14 or higher)
- Docker (for PostgreSQL and MinIO containers)
- Java 11 or higher
- Maven

### Setup Steps
1. Clone the repository:
   ```
   git clone https://github.com/AlexandrosManolis/preventiveHealth.git
   ```

2. Navigate to the project directory:
   ```
   cd preventiveHealth
   ```

### Setup Google Email Service
1. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Create a new project or select an existing one
3. Create OAuth 2.0 credentials
4. Download the credentials JSON file
5. Place the file in the `src/main/resources/` directory as `credentials.json`

### Configure Infrastructure
*Replace `<...>` with your actual data.*

1. Set up environment variables:
   Create a `.env` file in the root directory and add the following:
   ```
   POSTGRES_USER=<postgres_user>
   POSTGRES_PASSWORD=<postgres_password>
   POSTGRES_DB=preventiveHealth
  
   MINIO_ACCESS_KEY=<minio_user>
   MINIO_SECRET_KEY=<minio_password>
   MINIO_BUCKET_NAME=preventiveHealth
   ```

#### Automated Deployment using Docker Compose
To start PostgreSQL and MinIO with a single command:
```bash
docker-compose up -d
```

To stop PostgreSQL and MinIO with a single command:
```bash
docker-compose down
```

### Build and Run

#### Without tests
```bash
./mvnw package -Dmaven.test.skip
```
Then, to run the application (when a PostgreSQL database is active):
```bash
java -jar target/preventiveHealth-0.0.1-SNAPSHOT.jar
```

#### With tests
```bash
./mvnw test
```

## Project Structure

```
preventiveHealth/
├── src/
│   ├── main/
│   │   ├── java/            # Java backend code
│   │   ├── resources/       # Application resources
│   │   │   ├── credentials.json  # Google Email API credentials
│   │   │   └── application.properties
│   └── test/                # Test files
├── .env #change it with your credentials
├── docker-compose.yml #PostgreSQL and minio automation
└── pom.xml
```

## Technologies

- **Frontend**: Vue.js
- **Backend**: Node.js, SpringBoot
- **Database**: PostgreSQL
- **Storage**: MinIO (S3-compatible object storage)
- **Authentication**: JWT (JSON Web Tokens)
- **Email Service**: Google Email API

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Contact

Alexandros Manolis - [@github](https://github.com/AlexandrosManolis)

Project Link: [https://github.com/AlexandrosManolis/preventiveHealth](https://github.com/AlexandrosManolis/preventiveHealth)