# Postgres database with Docker
```bash
docker run --name ds-lab-pg /
--rm /
-e POSTGRES_PASSWORD=pass123 /
-e POSTGRES_USER=dbuser /
-e POSTGRES_DB=preventiveHealth /
-d /
--net=host -v ds-lab-vol:/var/lib/postgresql/data postgres:14
```

# Build and run without test

You can build the project without running the tests by executing:
```bash
./mvnw package -Dmaven.test.skip
```
Then, to run the application (when a postgres database is active):
```bash
java -jar target/farmerCompensation-0.0.1-SNAPSHOT.jar
```


# Build and run test
To run the tests along with the build use:
```bash
./mvnw test
```
