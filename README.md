# EE-Docker

## How to run

Project can be started as a Docker container. Make sure that the TCP port or UNIX socket for docker is reachable inside the container.

Build and run the image:

Using `tcp`:

```sh
docker build --build-arg HOST_CONNECTION_TYPE=tcp . -t ee-docker
docker run --name ee-docker --network ee-docker-network -p 5055:5055 ee-docker
```

Using socket:

```sh
docker build . -t ee-docker
docker run --name ee-docker -v /var/run/docker.sock:/var/run/docker.sock --network ee-docker-network -p 5055:5055 ee-docker
```

Integration tests can be run using [artillery](https://artillery.io).

```sh
npm install -g artillery
artillery run integration-tests/artillery.yml
```
