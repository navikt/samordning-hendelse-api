#!/usr/bin/env groovy
@Library('peon-pipeline') _

node {
    def appToken
    def commitHash
    try {
        cleanWs()

        def version
        stage("checkout") {
            appToken = github.generateAppToken()

            sh "git init"
            sh "git pull https://x-access-token:$appToken@github.com/navikt/samordning-hendelse-api.git"

            sh "make bump-version"

            version = sh(script: 'cat VERSION', returnStdout: true).trim()
            commitHash = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

            github.commitStatus("pending", "navikt/samordning-hendelse-api", appToken, commitHash)
        }

        stage("build") {
            sh "make"
        }

        stage("release") {
            withCredentials([usernamePassword(credentialsId: 'nexusUploader', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                sh "docker login -u ${env.NEXUS_USERNAME} -p ${env.NEXUS_PASSWORD} repo.adeo.no:5443"
            }

            sh "make release"

            sh "git push --tags https://x-access-token:$appToken@github.com/navikt/samordning-hendelse-api HEAD:master"
        }

        stage("upload manifest") {
            withCredentials([usernamePassword(credentialsId: 'nexusUploader', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                sh "make manifest"
            }
        }

        stage("deploy preprod") {
            build([
                    job       : 'nais-deploy-pipeline',
                    propagate : true,
                    parameters: [
                            string(name: 'APP', value: "samordning-hendelse-api"),
                            string(name: 'REPO', value: "navikt/samordning-hendelse-api"),
                            string(name: 'VERSION', value: version),
                            string(name: 'COMMIT_HASH', value: commitHash),
                            string(name: 'DEPLOY_ENV', value: 'q0')
                    ]
            ])
        }

//        stage("deploy prod") {
//            build([
//                    job       : 'nais-deploy-pipeline',
//                    wait      : false,
//                    parameters: [
//                            string(name: 'APP', value: "samordning-hendelse-api"),
//                            string(name: 'REPO', value: "navikt/samordning-hendelse-api"),
//                            string(name: 'VERSION', value: version),
//                            string(name: 'COMMIT_HASH', value: commitHash),
//                            string(name: 'DEPLOY_ENV', value: 'p')
//                    ]
//            ])
//        }

        github.commitStatus("success", "navikt/samordning-hendelse-api", appToken, commitHash)
    } catch (err) {
        github.commitStatus("failure", "navikt/samordning-hendelse-api", appToken, commitHash)
        throw err
    }
}