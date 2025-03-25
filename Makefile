.DEFAULT_GOAL := help

help: ## show this message
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-10s\033[0m %s\n", $$1, $$2}'

all: ## build all
	(cd frontend && mvn clean package) && \
	(cd backend  && mvn clean package)

push: ## push images to registry
	docker-compose push

build-local: ## build local images
	cd backend  && docker build -t morisacr.azurecr.io/dapr-sample-backend:latest .
	cd frontend && docker build -t morisacr.azurecr.io/dapr-sample-frontend:latest .

up: ## start the services
	docker compose up

down: ## stop the services
	docker compose down