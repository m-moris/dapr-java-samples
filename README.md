# dapr sample for java

[日本語 (Japanese)](./README.ja.md)

### Updates

- Update dapr verison 1.5.0 to 1.8.0
- Update Java version 11 to 17
- Update Spring Boot version

## Intro

This is a simple sample that receives HTTP communication from outside the company at the Frontend (Spring Boot) and passes it to the BackEnd (Spring Booot). It uses a State Store to maintain state.

```
+-------------+        +-------------+
| FrontEnd    | -----> | BackEnd     | --> (StateStore)
+-------------+        +-------------+
```

## Dapr

Install Dapr CLI.

[Install the Dapr CLI | Dapr Docs](https://docs.dapr.io/getting-started/install-dapr-cli/)

When reconfiguring the environment, uninstall and then initialize.

```sh
dapr unisntall --all
dapr init
```

Do a `docker ps` and if you see the following, you are good to go.


```sh
$ docker ps
CONTAINER ID   IMAGE               COMMAND                  CREATED          STATUS                    PORTS                              NAMES
be8a72b20325   daprio/dapr:1.5.0   "./placement"            14 seconds ago   Up 13 seconds             0.0.0.0:50005->50005/tcp           dapr_placement
6f683eb77d97   redis               "docker-entrypoint.s…"   14 seconds ago   Up 12 seconds             0.0.0.0:6379->6379/tcp             dapr_redis
0c08eafb6574   openzipkin/zipkin   "start-zipkin"           14 seconds ago   Up 12 seconds (healthy)   9410/tcp, 0.0.0.0:9411->9411/tcp   dapr_zipkin
```

## Local execution

1. Build with `make all`
2. Run `run1.sh` and `run2.sh`, respectively.
3. State Store uses `redis`.

The scripts are as follows, respectively.

**frontend**

```
dapr run --app-id frontend --app-port 8080 --dapr-http-port 3500 \
  --components-path ./frontend/components \
  -- java -jar frontend/target/frontend-0.0.1-SNAPSHOT.jar com.example.dapr.frontend.FrontEndApplication --server.port=8080
```

**backend**

```
dapr run --app-id backend --app-port 8888 --dapr-http-port 3501 \
  --components-path ./backend/components/dev \
  -- java -jar backend/target/backend-0.0.1-SNAPSHOT.jar com.example.dapr.backend.BackEndApplication --server.port=8888
```

**run locally**

```zsh
curl http://localhost:8080/test?name=dapr
```

## Container App

### Build container images

1. Rename the image in `docker-compose.yml` appropriately and build the image with `make all images`. Then, register it in your container registry.
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


