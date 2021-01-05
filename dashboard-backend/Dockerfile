FROM hayd/alpine-deno:1.5.2

EXPOSE 8080

WORKDIR /app

USER deno

#COPY deps.ts .
#RUN deno cache deps.ts

# These steps will be re-run upon each file change in your working directory:
ADD . .
# Compile the main app so that it doesn't need to be compiled each startup/entry.
RUN deno cache main.ts

CMD ["run", "--allow-net", "--allow-read", "main.ts"]