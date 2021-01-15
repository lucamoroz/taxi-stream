FROM hayd/alpine-deno:1.5.2

WORKDIR /app

# These steps will be re-run upon each file change in your working directory:
ADD . .
# Compile the main app so that it doesn't need to be compiled each startup/entry.
RUN deno cache main.ts

# wait-for-it
RUN apk add bash curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > wait-for-it.sh
RUN chmod +x ./wait-for-it.sh

# start app
CMD bash -c "./wait-for-it.sh -t 0 ${WAIT_FOR} -- deno run --allow-net --allow-read main.ts"
