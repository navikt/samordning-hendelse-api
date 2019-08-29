@Library('peon-pipeline') _

pipeline {
    agent any

    environment {
        APP_NAME    = "samordning-hendelse-api"
        APP_TOKEN   = github.generateAppToken()
        DOCKER_REPO = "repo.adeo.no:5443"
    }

    stages {
        stage("checkout") {
            steps {
                script {
                    latestStage = env.STAGE_NAME
                    sh "git init"
                    sh "git pull https://x-access-token:${APP_TOKEN}@github.com/navikt/${APP_NAME}.git"
                    env.COMMIT_HASH_LONG = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                    env.COMMIT_HASH_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    github.commitStatus("pending", "navikt/$APP_NAME", APP_TOKEN, COMMIT_HASH_LONG)
                }
            }
        }
        stage("build") {
            steps {
                script {
                    latestStage = env.STAGE_NAME
                    sh '''docker run --rm -t \
                        -w /usr/src \
                        -v ${PWD}:/usr/src \
                        -v ${HOME}/.m2:/var/maven/.m2 \
                        -e MAVEN_CONFIG=/var/maven/.m2 \
                        maven:3.5-jdk-11 mvn -Duser.home=/var/maven clean package -DskipTests=true -B -V'''
                    sh '''docker run --rm -t \
                        -w /usr/src \
                        -v ${PWD}:/usr/src \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        -v ${HOME}/.m2:/var/maven/.m2 \
                        -e MAVEN_CONFIG=/var/maven/.m2 \
                        maven:3.5-jdk-11 mvn -Duser.home=/var/maven verify -B -e'''
                }
            }
        }
        stage("release") {
            steps {
                script {
                    latestStage = env.STAGE_NAME
                    withCredentials([usernamePassword(credentialsId: 'nexusUploader',
                            usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh "docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWORD $DOCKER_REPO"
                        sh "docker build . --pull -t $DOCKER_REPO/$APP_NAME:$COMMIT_HASH_SHORT"
                        try {
                            sh "docker push $DOCKER_REPO/$APP_NAME:$COMMIT_HASH_SHORT"
                        } catch (err) {
                            print("Image ${DOCKER_REPO}/${APP_NAME} already exists")
                        }
                    }
                }
            }
        }
        stage("deploy") {
            steps {
                script {
                    latestStage = env.STAGE_NAME
                    deployments = [
                            ["dev-fss", "default"]
                    ]
                    for (deployment in deployments) {
                        latestDeploy = [deployment]
                        (context, namespace, manifest) = deployment
                        deploy.naiserator(context, namespace, manifest)
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                github.commitStatus("success", "navikt/$APP_NAME", APP_TOKEN, COMMIT_HASH_LONG)
                slack.notification("good", ":pogchamp:", latestStage, deployments)
            }
        }
        failure {
            script {
                github.commitStatus("failure", "navikt/$APP_NAME", APP_TOKEN, COMMIT_HASH_LONG)
                slack.notification("danger", ":angery:", latestStage, latestDeploy)
            }
        }
    }
}
