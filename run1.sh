# You can also debug in vscode.

dapr run --app-id frontend --app-port 8080 --dapr-http-port 3500 \
  --components-path ./frontend/components \
  -- java -jar frontend/target/frontend-0.0.1-SNAPSHOT.jar com.example.dapr.frontend.FrontEndApplication --server.port=8080


