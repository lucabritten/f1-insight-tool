FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only the pom first to leverage Docker layer caching
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Now copy sources and build
COPY src ./src

# Package shaded JAR
RUN mvn -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN useradd -u 10001 -r -s /sbin/nologin appuser && chown -R appuser:appuser /app
USER appuser

# Copy the shaded JAR from build stage
COPY --from=build /workspace/target/f1-insight-tool-1.0-SNAPSHOT.jar /app/app.jar

# Default port; Render sets $PORT at runtime. We pass it to Spring below.
ENV PORT=8080

ENV F1_DB_PATH=/var/data/f1data.db

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=$PORT"]
