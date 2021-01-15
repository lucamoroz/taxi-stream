FROM wurstmeister/kafka:2.11-2.0.0

# wait-for-it
RUN apk add bash curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > wait-for-it.sh
RUN chmod +x ./wait-for-it.sh

# start app
CMD bash -c "./wait-for-it.sh -t 0 ${WAIT_FOR} -- start-kafka.sh"