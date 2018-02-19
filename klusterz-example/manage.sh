#!/bin/bash
set -e

function restart() {
    kubectl config use-context docker
    kubectl delete deployment --all -n default
    lein clean
    lein uberjar
    docker build -t rutledgepaulv/klusterz-example:latest .
    docker push rutledgepaulv/klusterz-example:latest
    kubectl create -f deployment.yaml
    kubectl get pods -w
}

case "$1" in
    restart)
        restart
        echo ""
    ;;
    *)
        echo "Usage: ./manage.sh restart"
        exit 1
esac

exit 0