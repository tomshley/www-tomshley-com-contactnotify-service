
### Setup Cluster Registry Auth
```shell
source ./.secure_files/.tfstate.env
```
```shell
export K8S_DOCKER_CONFIG_AUTH=$(echo "{ \"auths\": { \"https://$K8S_DOCKER_REGISTRY\":{ \"auth\":\"$(printf "$K8S_DOCKER_REGISTRY_USER:$K8S_DOCKER_REGISTRY_PASS" | openssl base64 -A)\" } }}")
export K8S_DOCKER_CONFIG_AUTH_BASE64=$(echo "$K8S_DOCKER_CONFIG_AUTH" | base64)
export TWILIO_ACCOUNT_SID_BASE64=$(echo -n "$TWILIO_ACCOUNT_SID_HOSTNAME" | base64 -w 0);
export TWILIO_AUTH_TOKEN_BASE64=$(echo -n "$TWILIO_AUTH_TOKEN_USERNAME" | base64 -w 0);
export TWILIO_PHONE_NUMBER_BASE64=$(echo -n "$TWILIO_PHONE_NUMBER_PASSWORD" | base64 -w 0);
export KAFKA_BROKER_SERVER_BASE64=$(echo -n "$CONFLUENTCLOUD_BROKER_ENDPOINT" | base64 -w 0);
export KAFKA_CLUSTER_KEY_BASE64=$(echo -n "$CONFLUENTCLOUD_CLUSTER_KEY" | base64 -w 0);
export KAFKA_CLUSTER_SECRET_BASE64=$(echo -n "$CONFLUENTCLOUD_CLUSTER_PASSWORD" | base64 -w 0);
```

To Auth With Docker to Push
```shell
echo "$DOCKER_PUSH_REGISTRY_PASS" | docker logout $DOCKER_PUSH_REGISTRY
echo "$DOCKER_PUSH_REGISTRY_PASS" | docker login $DOCKER_PUSH_REGISTRY --username $DOCKER_PUSH_REGISTRY_USER --password-stdin
```

Push the latest build to dock
```shell
docker push registry.gitlab.com/tomshley/brands/usa/tomshleyllc/tech/www-tomshley-com-contactnotify-service/www-tomshley-com-contactnotify-service:latest
```

### Deploy The Service
```shell
kubectl delete namespace www-tomshley-com-contactnotify-service-namespace
kubectl apply -f kubernetes/namespace.json
kubectl config set-context --current --namespace=www-tomshley-com-contactnotify-service-namespace
envsubst < kubernetes/credentials-registry.yml | kubectl apply -f -
envsubst < kubernetes/connection-kafka.yml | kubectl apply -f -
envsubst < kubernetes/connection-twilio.yml | kubectl apply -f -
kubectl apply -f kubernetes/rbac.yml
kubectl apply -f kubernetes/service.yml
kubectl apply -f kubernetes/deployment.yml
```

Tail logs
```shell
kubectl logs --follow -l app=www-tomshley-com-contactnotify-service --namespace=www-tomshley-com-contactnotify-service-namespace
```
