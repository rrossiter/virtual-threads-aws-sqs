.PHONY: up down restart logs

up:
	chmod +x init-localstack.sh
	docker-compose up -d

down:
	docker-compose down

restart:
	docker-compose down
	chmod +x init-localstack.sh
	docker-compose up -d

logs:
	docker-compose logs -f
