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
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/tests/test', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: ''])
            }
        }
        stage('Deploy To Int') {
            steps {
                azureWebAppPublish appName: 'lpg-deply', azureCredentialsId: 'azure_service_principal', dockerImageName: 'google/nodejs-hello', dockerImageTag: 'test', dockerRegistryEndpoint: [], filePath: '', publishType: 'docker', resourceGroup: 'lpg-jenkins', slotName: '', sourceDirectory: '', targetDirectory: ''
            }
        }
    }
    post {
        always {
            junit 'build/test-results/**/TEST-*.xml'
        }
    }
}