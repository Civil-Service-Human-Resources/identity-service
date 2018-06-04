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
        post {
            always {
                junit 'build/test-results/**/TEST-*.xml'
            }
        }
    }
}