#!/usr/bin/env groovy
@Library('peon-pipeline') _

def commitStatus(state, repo, token, sha) {
    sh "curl -X POST https://api.github.com/repos/$repo/statuses/$sha" +
            " -H 'Authorization: Bearer $token'" +
            " -H 'Content-Type: application/json'" +
            " -x webproxy-internett.nav.no:8088" +
            " -d '{" +
            "\"state\": \"$state\", " +
            "\"context\": \"continuous-integration/jenkins\", " +
            "\"description\": \"Build #${env.BUILD_NUMBER}: $state\", " +
            "\"target_url\": \"${env.BUILD_URL}\"" +
            "}'"
}

node {
    def appToken
    def commitHash
    try {
        cleanWs()

        def version
        stage("checkout") {
            def appId = 23087
            def genJwtToken = "/usr/lib/github-apps-support/bin/generate-jwt.sh"
            def genAppToken = "/usr/lib/github-apps-support/bin/generate-installation-token.sh"

            withCredentials([file(credentialsId: 'peon-ci-key', variable: 'PRIVATE_KEY')]) {
                def jwtToken = sh(script: "$genJwtToken ${env.PRIVATE_KEY} $appId", returnStdout: true).trim()
                appToken = sh(script: "$genAppToken $jwtToken", returnStdout: true).trim()
            }

            sh "git init"
            sh "git pull https://x-access-token:$appToken@github.com/navikt/samordning-hendelse-api.git"

            sh "make bump-version"

            version = sh(script: 'cat VERSION', returnStdout: true).trim()

            commitHash = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

            commitStatus("pending", "navikt/samordning-hendelse-api", appToken, commitHash)
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
                    wait      : false,
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

        commitStatus("success", "navikt/samordning-hendelse-api", appToken, commitHash)
    } catch (err) {
        commitStatus("error", "navikt/samordning-hendelse-api", appToken, commitHash)
        throw err
    }
}