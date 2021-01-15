FROM openjdk:8
RUN apt update && apt install -y \
maven \
nodejs
WORKDIR /usr/src/myapp
COPY pom.xml .
RUN mvn install
COPY . .
RUN mvn compile


# wait-for-it
RUN apt-get -y install bash curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > wait-for-it.sh
RUN chmod +x ./wait-for-it.sh

# start app
CMD bash -c "./wait-for-it.sh -t 0 ${WAIT_FOR} -- bash -c \"./wait-for-it.sh -t 0 ${WAIT_FOR_2} -- mvn exec:java -Dexec.classpathScope=compile -Dexec.mainClass=Program\""
