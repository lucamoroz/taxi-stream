#########################
###### build stage ######
#########################
FROM golang:1.13.1-alpine AS builder
WORKDIR /app
RUN apk add --no-cache git
# Prevent downloads on every build.
# Only downloads the dependencies if they actually change
COPY go.mod go.sum ./
RUN go mod download
# Build the app
COPY . .
RUN go build -v -o api


#########################
###### final stage ######
#########################
FROM alpine:latest
RUN apk --no-cache add ca-certificates
COPY --from=builder /app/api /app/api
EXPOSE 3000
ENTRYPOINT ./app/api
