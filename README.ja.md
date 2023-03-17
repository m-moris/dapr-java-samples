# dapr sample for java

### Updates

- Update dapr verison 1.5.0 to 1.8.0
- Update Java version 11 to 17
- Update Spring Boot version


## 概要

外部からの HTTP通信を、Frontend (Spring Boot) で受けて、BackEnd (Spring Booot) に流す簡単なサンプルです。状態を保持するState Store を使っています。

```
+-------------+        +-------------+
| FrontEnd    | -----> | BackEnd     | --> (StateStore)
+-------------+        +-------------+
```

## Dapr

Dapr CLI をインストールする。

[Install the Dapr CLI | Dapr Docs](https://docs.dapr.io/getting-started/install-dapr-cli/)

環境を再設定するときは、アンインストールしてから初期化する。

```sh
dapr unisntall --all
dapr init
```

`docker ps` して、以下が表示されれば問題なし。

```sh
$ docker ps
CONTAINER ID   IMAGE               COMMAND                  CREATED          STATUS                    PORTS                              NAMES
be8a72b20325   daprio/dapr:1.5.0   "./placement"            14 seconds ago   Up 13 seconds             0.0.0.0:50005->50005/tcp           dapr_placement
6f683eb77d97   redis               "docker-entrypoint.s…"   14 seconds ago   Up 12 seconds             0.0.0.0:6379->6379/tcp             dapr_redis
0c08eafb6574   openzipkin/zipkin   "start-zipkin"           14 seconds ago   Up 12 seconds (healthy)   9410/tcp, 0.0.0.0:9411->9411/tcp   dapr_zipkin
```

## ローカル実行

`make all` でビルドしてから、`run1.sh` と `run2.sh` をそれぞれ実行する。State Store は `redis` が使われる。

スクリプトの内容はそれぞれ以下の通り。

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

## Container App

### コンテナイメージのビルド

1. `docker-compose.yml` でイメージ名は適当に変更し、`make all images` し、自分のコンテナレジストリに登録する。
2. Azure Container Registry なら、`az acr login --name myregistry` でログイン後、`make push` する。

### 実行

`containerapps` での実行方法で、基本的には [チュートリアル: Azure CLI を使用して Dapr アプリケーションを Azure Container Apps にデプロイする | Microsoft Docs](https://docs.microsoft.com/ja-jp/azure/container-apps/microservices-dapr?tabs=bash) を参考のこと。

State Store としてBLOBを用意し、`backend\components\storage.yaml` に、アカウント名、キー、コンテナを記述する。デプロイ時に使用する。

```sh
az storage account create \
  --name $STORAGE_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --location "$LOCATION" \
  --sku Standard_RAGRS \
  --kind StorageV2
```

コンテナ環境を作成したあと、以下のコマンドでデプロイする。


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


