# If you want you can use this Makefile to specify debug commands, test scripts, etc.

# Some examples below

start:
	docker-compose build && docker-compose up -d

restart:
	docker-compose restart

build:
	docker-compose build

purge:
	docker-compose down -v --rmi all --remove-orphans