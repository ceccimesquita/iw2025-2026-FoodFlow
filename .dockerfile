# Etapa de build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar archivos de Maven wrapper y pom.xml
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Dar permisos de ejecución al wrapper
RUN chmod +x mvnw

# Descargar dependencias (cacheable)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Build de producción con Vaadin
RUN ./mvnw clean package -DskipTests -Pproduction

# Etapa de ejecución (imagen más ligera)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto (Railway usa $PORT)
EXPOSE 8080

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
