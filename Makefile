DOCKER  := docker
NAIS    := nais
VERSION := $(shell cat ./VERSION)
REGISTRY:= repo.adeo.no:5443

.PHONY: all build test docker docker-push bump-version release manifest

all: build test docker
release: tag docker-push

build:
	$(DOCKER) run --rm -t \
		-v ${PWD}:/usr/src \
		-w /usr/src \
		-u $(shell id -u) \
		-v ${HOME}/.m2:/var/maven/.m2 \
		-e MAVEN_CONFIG=/var/maven/.m2 \
		maven:3.5-jdk-11 mvn -Duser.home=/var/maven clean package -DskipTests=true -B -V

test:
	$(DOCKER) run --rm -t \
		-v ${PWD}:/usr/src \
		-w /usr/src \
		-u $(shell id -u) \
		-v ${HOME}/.m2:/var/maven/.m2 \
		-e MAVEN_CONFIG=/var/maven/.m2 \
		maven:3.5-jdk-11 mvn -Duser.home=/var/maven verify -B -e

docker:
	$(NAIS) validate
	$(DOCKER) build --pull -t $(REGISTRY)/tortuga-loot -t $(REGISTRY)/tortuga-loot:$(VERSION) .

docker-push:
	$(DOCKER) push $(REGISTRY)/tortuga-loot:$(VERSION)

bump-version:
	@echo $$(($$(cat ./VERSION) + 1)) > ./VERSION

tag:
	git add VERSION
	git commit -m "Bump version to $(VERSION) [skip ci]"
	git tag -a $(VERSION) -m "auto-tag from Makefile"

manifest:
	nais upload --app tortuga-loot -v $(VERSION)
