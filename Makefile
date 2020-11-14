
start: build run

build:
	docker-compose build

run:
	docker-compose up -d

logs:
	docker-compose logs -f

stop:
	docker-compose stop

kill:
	docker-compose kill

clean:
	docker-compose down -v --rmi all --remove-orphans