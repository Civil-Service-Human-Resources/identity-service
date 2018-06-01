pipeline {
    agent {
        docker { image 'gradle:jdk' }
    }
    stages {
        stage('Build') {
            steps {
                sh './gradlew init'
                sh './gradlew build'
            }
        }
    }
}