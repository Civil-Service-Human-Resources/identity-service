pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew clean'
                sh './gradlew build'
            }
        }
        stage('Test') {
            steps {
                publishHTML([allowMissing: false,
                             alwaysLinkToLastBuild: true,
                             keepAll: true,
                             reportDir: 'build/reports/tests/test',
                             reportFiles: 'index.html',
                             reportName: 'HTML Report',
                             reportTitles: ''])
            }
        }
        stage('Push To Docker') {
            steps {
                docker.withRegistry("${env.DOCKER_REGISTRY_URL}", 'docker_registry_credentials') {
                    def customImage = docker.build("identity-service:${env.BUILD_ID}")
                    customImage.push()
                }
            }
        }
    }
    post {
        always {
            junit 'build/test-results/**/TEST-*.xml'
        }
    }
}