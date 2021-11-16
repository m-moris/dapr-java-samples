dapr run --app-id backend --app-port 8888 --dapr-http-port 3501 \
  --components-path ./backend/components \
  -- java -jar backend/target/backend-0.0.1-SNAPSHOT.jar com.example.dapr.backend.BackEndApplication --server.port=8888
