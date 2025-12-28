# Etapa de build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# 1. INSTALAR NODE.JS (Crucial para Vaadin/Hilla)
# Vaadin necesita Node para compilar el frontend en el perfil de producción
RUN apk add --no-cache nodejs npm

# Copiar archivos de dependencias Java
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw* ./

# Descargar dependencias Java
RUN mvn dependency:go-offline -B -Pproduction || true

# 2. COPIAR ARCHIVOS DEL FRONTEND
# Vaadin necesita estos archivos en la raíz para el build de producción via Vite
# Si tienes una carpeta 'frontend', asegúrate de copiarla también.
COPY package*.json ./
COPY vite.config.* ./
COPY types.d.ts ./
COPY tsconfig.json ./
# Copiar carpeta frontend si existe (descomenta si tu proyecto la usa explícitamente)
# COPY frontend ./frontend

# Copiar código fuente Java
COPY src ./src

# Build de producción
ENV MAVEN_OPTS="-Xmx1024m -XX:+UseContainerSupport"
# El -Pproduction activará el plugin de vaadin para usar Node y compilar el bundle
RUN mvn clean package -DskipTests -Pproduction -B -e

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Memoria optimizada
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 3. FORZAR MODO PRODUCCIÓN
# Esto asegura que Vaadin no intente arrancar DevMode si algo falla
CMD ["sh", "-c", "java $JAVA_OPTS -Dvaadin.productionMode=true -Dserver.port=${PORT:-8080} -jar app.jar"]