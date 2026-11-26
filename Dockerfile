# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachea dependencias: primero el pom, luego el codigo
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
# Jar extraido (clases y dependencias sueltas): arranca ~20 % mas rapido que el jar anidado.
RUN java -Djarmode=tools -jar app.jar extract --destination extraido && rm app.jar

# Render inyecta PORT; la app lo lee via server.port=${PORT:8080}
EXPOSE 8080

# TieredStopAtLevel=1: solo el compilador rapido (C1). Con 0.1 CPU el compilador
# optimizador (C2) le quita CPU a las peticiones; medido: arranque de ~120 s a ~35 s
# y sin perder rendimiento bajo carga.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:TieredStopAtLevel=1", "-jar", "extraido/app.jar"]
