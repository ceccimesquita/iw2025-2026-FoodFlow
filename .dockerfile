# Etapa de build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar solo pom.xml primero para cachear dependencias
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw* ./

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B -Pproduction || true

# Copiar código fuente
COPY src ./src

# Build con más memoria y timeout
ENV MAVEN_OPTS="-Xmx1024m -XX:+UseContainerSupport"
RUN mvn clean package -DskipTests -Pproduction -B -e

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar solo el JAR
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Memoria optimizada para Railway
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de inicio
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]