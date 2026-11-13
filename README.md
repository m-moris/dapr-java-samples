# dapr sample for java

[日本語 (Japanese)](./README.ja.md)

### Component versions (2026-11-13)

| Component | Version |
| --- | --- |
| Java | 25 LTS (Microsoft Build of OpenJDK) |
| Spring Boot | 4.1.2 |
| Dapr Java SDK | 1.19.0 |
| Dapr runtime / CLI | 1.19.1 |
| Redis | 8.10.3 |
| Zipkin | 3.6.1 |

Use JDK 25, Maven 3.6.3 or later, and Docker Compose v2 or later.
Maven Toolchains automatically selects an installed JDK 25 for compilation and tests,
even when Maven itself uses another JDK. If your JDK 25 is installed in a non-standard
location, set `JAVA25_HOME` to its installation directory.
For local execution, check that `java -version` reports Java 25.
The Java container image tracks the latest patch release of Microsoft OpenJDK 25.
State writes omit `contentType` metadata to keep the existing Redis Hash format,
even when Redis supports JSON. This keeps state reads and writes compatible
without migrating existing counters.

## Intro

This is a simple sample that receives HTTP communication from outside the company at the Frontend (Spring Boot) and passes it to the BackEnd (Spring Booot). It uses a State Store to maintain state.

```
+-------------+        +-------------+
| FrontEnd    | -----> | BackEnd     | --> (StateStore)
+-------------+        +-------------+
```

## Dapr

Install Dapr CLI 1.19.1.

[Install the Dapr CLI | Dapr Docs](https://docs.dapr.io/getting-started/install-dapr-cli/)

Initialize the local Dapr runtime with the version used by this sample.
Docker Compose runs its own sidecars and does not require `dapr init`.

```sh
dapr init --runtime-version 1.19.1
```

Check the installed CLI/runtime versions with `dapr --version` and the running
Dapr infrastructure containers with `docker ps`. The Redis and Zipkin versions
in the table above are pinned in `docker-compose.yml`; `dapr init` manages its
own infrastructure images.

## Local execution

1. Build with `make all`
2. Run `run1.sh` and `run2.sh`, respectively.
3. State Store uses `redis`.

The scripts are as follows, respectively.

**frontend**

```
dapr run --app-id frontend --app-port 8080 --dapr-http-port 3500 \
  --resources-path ./frontend/components \
  -- java -jar frontend/target/frontend-0.0.1-SNAPSHOT.jar com.example.dapr.frontend.FrontEndApplication --server.port=8080
```

**backend**

```
dapr run --app-id backend --app-port 8888 --dapr-http-port 3501 \
  --resources-path ./backend/components/dev \
  -- java -jar backend/target/backend-0.0.1-SNAPSHOT.jar com.example.dapr.backend.BackEndApplication --server.port=8888
```

**run locally**

```zsh
curl 'http://localhost:8080/test?name=dapr'
```

## Run in Docker Compose

```sh
make all build-local
docker compose up -d
curl 'http://localhost:8080/test?name=dapr'
docker compose down
```

## Container App

### Build container images

1. Set your image names in both `docker-compose.yml` and `Makefile`, then build with `make all build-local`.
2. For Azure Container Registry, login with `az acr login --name myregistry` and `make push`.

### Execution

See the following link on how to do this in `containerapps`.

[Tutorial: Deploy a Dapr application to Azure Container Apps using the Azure CLI | Microsoft Docs](https://docs.microsoft.com/en-us/azure/container-apps/microservices-dapr?tabs=bash)

Prepare a BLOB as a State Store and write the account name, key and container in `backend\components\storage.yaml`. This is used when deploying.

```sh
az storage account create \
  --name $STORAGE_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --location "$LOCATION" \
  --sku Standard_RAGRS \
  --kind StorageV2
```

After creating a container environment, deploy it with the following command.

**backend**

```sh
az containerapp create \
  --name backend \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --image morisacr.azurecr.io/dapr-sample-backend:latest \
  --target-port 8080 \
  --ingress 'internal' \
  --min-replicas 1 \
  --max-replicas 1 \
  --enable-dapr \
  --dapr-app-port 8080 \
  --dapr-app-id backend \
  --dapr-components backend/components/production/storage.yaml \
  --registry-login-server $ACR_SERVER --registry-username $ACR_USER --registry-password $ACR_PASSWORD
```

**frontend**

```sh
az containerapp create \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINERAPPS_ENVIRONMENT \
  --image morisacr.azurecr.io/dapr-sample-frontend:latest \
  --target-port 8080 \
  --ingress 'external' \
  --min-replicas 1 \
  --max-replicas 1 \
  --enable-dapr \
  --dapr-app-port 8080 \
  --dapr-app-id frontend \
  --registry-login-server $ACR_SERVER --registry-username $ACR_USER --registry-password $ACR_PASSWORD
```


