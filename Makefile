.PHONY: all build run logs kill clean

image_name = storm-aic7
container_name = storm-container

all: build run logs

build:
	docker build -t $(image_name) .

run:
	docker run -d --rm --name $(container_name) $(image_name)

logs:
	docker logs -f $(container_name)

kill:
	docker kill $(container_name)

clean:
	docker image prune -f
	docker image rm $(image_name)