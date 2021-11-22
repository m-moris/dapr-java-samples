# dapr sample for java


## Dapr

Dapr CLI をインストールする

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

`make all` でビルドしてから、`run1.sh` と `run2.sh` をそれぞれ実行する

