#!/usr/bin/env groovy
@Library('peon-pipeline') _

node {
    def APP_NAME    = "samordning-hendelse-api"
    def APP_TOKEN   = github.generateAppToken()
    def DOCKER_REPO = "repo.adeo.no:5443"
    def COMMIT_HASH_LONG
    def COMMIT_HASH_SHORT
    stage('checkout') {
        try {
            sh "git init"
            sh "git pull https://x-access-token:${APP_TOKEN}@github.com/navikt/${APP_NAME}.git"
            COMMIT_HASH_LONG = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
            COMMIT_HASH_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
            github.commitStatus("pending", "navikt/${APP_NAME}", APP_TOKEN, COMMIT_HASH_LONG)
        } catch (err) {
            github.commitStatus("failure", "navikt/${APP_NAME}", APP_TOKEN, COMMIT_HASH_LONG)
            slackSend([color  : 'danger',
                       message: "Failed to checkout ${APP_NAME}:<https://github.com/navikt/${APP_NAME}/commit/${COMMIT_HASH_LONG}|`${COMMIT_HASH_SHORT}`>"
            ])
            error("Failed checkout stage")
        }
    }
    stage('build') {
        try {
            sh '''docker run --rm -t \
                    -w /usr/src \
                    -v ${PWD}:/usr/src \
                    -v ${HOME}/.m2:/var/maven/.m2 \
                    -e MAVEN_CONFIG=/var/maven/.m2 \
                    maven:3.5-jdk-11 mvn -Duser.home=/var/maven clean package -DskipTests=true -B -V
                '''
            sh '''docker run --rm -t \
                    -w /usr/src \
                    -v ${PWD}:/usr/src \
                    -v /var/run/docker.sock:/var/run/docker.sock \
                    -v ${HOME}/.m2:/var/maven/.m2 \
                    -e MAVEN_CONFIG=/var/maven/.m2 \
                    maven:3.5-jdk-11 mvn -Duser.home=/var/maven verify -B -e
                '''
            github.createDeploymentStatus(APP_TOKEN, "navikt/${APP_NAME}", COMMIT_HASH_SHORT, "success")
        } catch (err) {
            github.commitStatus("failure", "navikt/${APP_NAME}", APP_TOKEN, COMMIT_HASH_LONG)
            slackSend([color  : 'danger',
                       message: "Failed to build ${APP_NAME}:<https://github.com/navikt/${APP_NAME}/commit/${COMMIT_HASH_LONG}|`${COMMIT_HASH_SHORT}`>"
            ])
            error("Failed build stage")
        }
    }
    stage('release') {
        try {
            withCredentials([usernamePassword(credentialsId: 'nexusUploader',
                    usernameVariable: 'NEXUS_USERNAME',
                    passwordVariable: 'NEXUS_PASSWORD'
            )]) {
                sh "docker login -u ${env.NEXUS_USERNAME} -p ${env.NEXUS_PASSWORD} ${DOCKER_REPO}"
                sh "docker build . --pull -t ${DOCKER_REPO}/${APP_NAME}:${COMMIT_HASH_SHORT}"
                sh "docker push ${DOCKER_REPO}/${APP_NAME}:${COMMIT_HASH_SHORT}"
            }
        } catch (err) {
            github.commitStatus("failure", "navikt/${APP_NAME}", APP_TOKEN, COMMIT_HASH_LONG)
            slackSend([color  : 'danger',
                       message: "Failed to upload ${APP_NAME}:<https://github.com/navikt/${APP_NAME}/commit/${COMMIT_HASH_LONG}|`${COMMIT_HASH_SHORT}`> to nexus"
            ])
            error("Failed release stage")
        }
    }
    stage('deploy dev') {
        try {
            build([
                    job       : 'nais-deploy-pipeline',
                    propagate : true,
                    parameters: [
                            string(name: 'APP', value: "${APP_NAME}"),
                            string(name: 'REPO', value: "navikt/${APP_NAME}"),
                            string(name: 'VERSION', value: COMMIT_HASH_SHORT),
                            string(name: 'COMMIT_HASH', value: COMMIT_HASH_LONG),
                            string(name: 'DEPLOY_ENV', value: 'q0')
                    ]
            ])
            github.commitStatus("success", "navikt/${APP_NAME}", APP_TOKEN, COMMIT_HASH_LONG)
            slackSend([color  : 'good',
                       message: "Successfully deployed ${APP_NAME}:<https://github.com/navikt/${APP_NAME}/commit/${COMMIT_HASH_LONG}|`${COMMIT_HASH_SHORT}`>" + " to q0 :pogchamp:"
            ])
        } catch (err) {
            github.commitStatus("failure", "navikt/${APP_NAME}", APP_TOKEN, COMMIT_HASH_LONG)
            slackSend([color  : 'danger',
                       message: "Failed to deploy ${APP_NAME}:<https://github.com/navikt/${APP_NAME}/commit/${COMMIT_HASH_LONG}|`${COMMIT_HASH_SHORT}`>" + " to q0 :angery:"
            ])
            error("Failed deploy stage")
        }
    }
}