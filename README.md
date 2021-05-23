# EE-Docker

## How to run

Project can be started as a Docker container. Make sure that the TCP or UNIX socket for docker is reachable inside the container.

Build and run the image:
```sh
docker build . -t ee-docker
docker run -p 5055:5055 ee-docker -d
```

Integration tests can be run using (artillery)[https://artillery.io].
```sh
npm install -g artillery
artillery run integration-tests/artillery.yml
```
