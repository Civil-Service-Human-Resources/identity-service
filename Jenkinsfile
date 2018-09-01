pipeline {
    agent any
    stages {
        stage('Gradle Build') {
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
        stage('Build Container & Push to ACR') {
            steps {
                script {
                    docker.withRegistry("${env.DOCKER_REGISTRY_URL}", 'docker_registry_credentials') {
                        def customImage = docker.build("identity-service:${env.BUILD_ID}")
                        customImage.push()
                    }
                }
            }
        }
        stage('Deploy to Integration') {
            steps {
                script {
                    def tfHome = tool name: 'Terraform', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
                    env.PATH = "${tfHome}:${env.PATH}"
                }
                withCredentials([
                    string(credentialsId: 'SECURE_FILES', variable: 'SF'),
                    usernamePassword(credentialsId: 'docker_registry_credentials', usernameVariable: 'acr_username', passwordVariable: 'acr_password')
                    ]) {
                    sh "set +e; rm -rf lpg-terraform-paas"
                    sh "git clone https://github.com/Civil-Service-Human-Resources/lpg-terraform-paas.git -b acrmodules --single-branch"
                    dir("lpg-terraform-paas/environments/master") {
                        sh "ln -s ${SF}/azure/cabinet-azure/00-integration/state.tf state.tf"
                        sh "ln -s ${SF}/azure/cabinet-azure/00-integration/integration-vars.tf integration-vars.tf"
                        sh "ln -s ../00-integration/00-vars.tf 00-vars.tf"
                        sh "terraform --version"
                        sh "terraform init"
                        sh "terraform validate"
                        sh "terraform plan -target=module.identity -var 'identity_docker_tag=${env.BUILD_ID}' -var 'docker_registry_server_username=${acr_username}' -var 'docker_registry_server_password=${acr_password}'"
                        sh "terraform apply -target=module.identity -var 'identity_docker_tag=${env.BUILD_ID}' -var 'docker_registry_server_username=${acr_username}' -var 'docker_registry_server_password=${acr_password}' -auto-approve"
                        sh "terraform plan -target=module.identity-management -var 'identity_docker_tag=${env.BUILD_ID}' -var 'docker_registry_server_username=${acr_username}' -var 'docker_registry_server_password=${acr_password}'"
                        sh "terraform apply -target=module.identity-management -var 'identity_docker_tag=${env.BUILD_ID}' -var 'docker_registry_server_username=${acr_username}' -var 'docker_registry_server_password=${acr_password}' -auto-approve"
                    }
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