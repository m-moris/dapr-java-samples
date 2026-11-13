# dapr sample for java

### コンポーネントのバージョン（2026-11-13）

| コンポーネント | バージョン |
| --- | --- |
| Java | 25 LTS（Microsoft Build of OpenJDK） |
| Spring Boot | 4.1.2 |
| Dapr Java SDK | 1.19.0 |
| Dapr runtime / CLI | 1.19.1 |
| Redis | 8.10.3 |
| Zipkin | 3.6.1 |

JDK 25、Maven 3.6.3 以降、Docker Compose v2 以降を使用します。
Maven 自体が別の JDK で動いていても、Maven Toolchains がインストール済みの JDK 25 をビルド・テスト用に自動選択します。
標準外の場所にインストールしている場合は、`JAVA25_HOME` を JDK 25 のインストール先に設定してください。
ローカル実行時は、`java -version` が Java 25 を表示することを確認してください。
Java のコンテナイメージは Microsoft OpenJDK 25 の最新パッチに追従します。
Redis が JSON に対応していても従来の Hash 形式を維持するため、状態保存時の `contentType` メタデータを省略しています。
既存のカウントデータを移行せず、同じ形式で読み書きできます。


## 概要

外部からの HTTP通信を、Frontend (Spring Boot) で受けて、BackEnd (Spring Booot) に流す簡単なサンプルです。状態を保持するState Store を使っています。

```
+-------------+        +-------------+
| FrontEnd    | -----> | BackEnd     | --> (StateStore)
+-------------+        +-------------+
```

## Dapr

Dapr CLI 1.19.1 をインストールする。

[Install the Dapr CLI | Dapr Docs](https://docs.dapr.io/getting-started/install-dapr-cli/)

サンプルに合わせたバージョンでローカルの Dapr runtime を初期化する。
Docker Compose で実行する場合は専用のサイドカーを起動するため、`dapr init` は不要。

```sh
dapr init --runtime-version 1.19.1
```

`dapr --version` で CLI / runtime のバージョン、`docker ps` で関連コンテナの起動を確認する。
上表の Redis / Zipkin のバージョンは `docker-compose.yml` で固定している。
`dapr init` が起動するインフラ用イメージは別途 Dapr CLI が管理する。

## ローカル実行

`make all` でビルドしてから、`run1.sh` と `run2.sh` をそれぞれ実行する。State Store は `redis` が使われる。

スクリプトの内容はそれぞれ以下の通り。

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

起動後、以下で呼び出す。

```sh
curl 'http://localhost:8080/test?name=dapr'
```

## Docker Compose で実行

```sh
make all build-local
docker compose up -d
curl 'http://localhost:8080/test?name=dapr'
docker compose down
```

## Container App

### コンテナイメージのビルド

1. `docker-compose.yml` と `Makefile` の両方でイメージ名を変更し、`make all build-local` でビルドする。
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


