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
        stage('Deploy To Int') {
            steps {
                azureWebAppPublish appName: 'lpg-deply', azureCredentialsId: 'azure_service_principal',
                        dockerImageName: 'cshr/identity-service', dockerImageTag: 'test',
                        publishType: 'docker', resourceGroup: 'lpg-jenkins',
                        dockerRegistryEndpoint: [credentialsId: '86d3c40b-df14-4e5b-9a6c-9a940ab2fa4f', url: "https://index.docker.io/v1/"]
            }
        }
    }
    post {
        always {
            junit 'build/test-results/**/TEST-*.xml'
        }
    }
}