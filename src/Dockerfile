# ------------ Build Stage ------------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first (enables caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Build
RUN mvn clean package -DskipTests

# ------------ Runtime Stage ------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy built JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
