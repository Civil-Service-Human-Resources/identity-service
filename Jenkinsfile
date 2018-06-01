pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew init'
                sh './gradlew build'
            }
        }
    }
}