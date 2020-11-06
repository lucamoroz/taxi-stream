FROM openjdk:8
RUN apt update && apt install -y \
maven
WORKDIR /usr/src/myapp
COPY pom.xml .
RUN mvn install
COPY . .
RUN mvn compile
CMD mvn exec:java -Dexec.classpathScope=compile -Dexec.mainClass=Program