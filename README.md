# Building Role Based Access using AI for Java Developers
This is the repository for the LinkedIn Learning course `Building Role Based Access using AI for Java Developers`. The full course is available from [LinkedIn Learning][lil-course-url].

# Developer Setup
- To ensure everything works, run `mvn clean install` in the root directory.
- To run the application, run `mvn exec:java -Dexec.mainClass="com.ll.App"` in the root directory. This should print out "Hello World!".

## Running the Application
### Starting the Application
1. Build the application:
```bash
mvn clean install
```

2. Start MongoDB:
```bash
docker-compose up -d
```

3. Running all tests
```
mvn clean test
```

4. Start the server:
```bash
mvn spring-boot:run -Dspring-boot.run.main-class=com.ll.App
```

The application will start on port 8080 by default.

## API Documentation
Once the server is running, you can access:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml


[0]: # (Replace these placeholder URLs with actual course URLs)

[lil-course-url]: https://www.linkedin.com/learning/
[lil-thumbnail-url]: http://

