# ---- Estágio 1: build com JDK ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Dependências primeiro: esta camada fica em cache enquanto só o código muda
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
# Testes já rodaram no job "build" do CI
RUN ./mvnw -B package -DskipTests

# ---- Estágio 2: runtime só com JRE e usuário sem privilégios ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
