pipeline {
    agent any
    
    environment {
        // Docker Hub Configuration
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        DOCKER_HUB_USERNAME = 'queenivas'  // ⚠️ CHANGE THIS!
        DOCKER_IMAGE_NAME = 'appointment-app'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_IMAGE_LATEST = "${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest"
        DOCKER_IMAGE_VERSIONED = "${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
        
        // SonarQube Configuration
        SONARQUBE_ENV = 'sonarqube'
        SONAR_PROJECT_KEY = 'appointment-app'
        SONAR_HOST_URL = 'http://localhost:9000' // ⚠️ CHANGE THIS!
        SONAR_AUTH_TOKEN = credentials('sonar-auth-token')  // Using Jenkins credentials
        
        // Kubernetes Configuration
        K8S_NAMESPACE = 'default'
        
        // Helm Configuration
        HELM_CHART_PATH = './helm/appointment-app'
        HELM_RELEASE_NAME = 'appointment-app'
        
        // Trivy Configuration
        TRIVY_SEVERITY = 'HIGH,CRITICAL'
    }
    
    stages {
        stage('🧹 Cleanup Workspace') {
            steps {
                echo '🧹 Cleaning workspace...'
                cleanWs()
            }
        }
        
        stage('📥 Checkout') {
            steps {
                echo '📥 Checking out code from repository...'
                checkout scm
            }
        }
        
        stage('🔍 SonarQube Analysis') {
            steps {
                script {
                    echo '🔍 Running SonarQube code quality analysis...'
                    withSonarQubeEnv("${SONARQUBE_ENV}") {
                        sh """
                            ./mvnw clean verify sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.projectName='Appointment App' \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_AUTH_TOKEN}
                        """
                    }
                }
            }
        }
        
        stage('⏳ Quality Gate') {
            steps {
                script {
                    echo '⏳ Waiting for SonarQube Quality Gate result...'
                    timeout(time: 5, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "❌ Pipeline aborted due to quality gate failure: ${qg.status}"
                        } else {
                            echo "✅ Quality Gate passed!"
                        }
                    }
                }
            }
        }
        
        // Continue with the remaining stages...
        
        stage('🔨 Build & Test') {
            steps {
                echo '🔨 Building application and running tests...'
                sh './mvnw clean test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    echo '📊 Test results published'
                }
            }
        }
        
        // Additional stages...

        stage('📦 Helm Package') {
            steps {
                script {
                    echo '📦 Packaging Helm chart...'
                    sh """
                        helm package ${HELM_CHART_PATH} --version 1.0.${BUILD_NUMBER}
                    """
                    echo '✅ Helm chart packaged'
                }
            }
        }
        
        // Final stages...
    }
    
    post {
        success {
            script {
                echo '✅ ========================================='
                echo '✅ PIPELINE COMPLETED SUCCESSFULLY!'
                echo '✅ ========================================='
                echo "📦 Docker Image: ${DOCKER_IMAGE_VERSIONED}"
                echo "🔗 Docker Hub: https://hub.docker.com/r/${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}"
                echo "☸️  Kubernetes Namespace: ${K8S_NAMESPACE}"
                echo "📊 Helm Release: ${HELM_RELEASE_NAME}"
                
                // Get service URL
                def serviceUrl = sh(
                    script: "minikube service ${HELM_RELEASE_NAME} -n ${K8S_NAMESPACE} --url 2>/dev/null || echo 'Run: minikube service ${HELM_RELEASE_NAME} -n ${K8S_NAMESPACE}'",
                    returnStdout: true
                ).trim()
                echo "🌐 Application URL: ${serviceUrl}"
                echo '✅ ========================================='
            }
        }
        
        failure {
            echo '❌ ========================================='
            echo '❌ PIPELINE FAILED!'
            echo '❌ ========================================='
            echo '📋 Check the console output for details'
            echo '❌ ========================================='
        }
        
        always {
            echo '🧹 Performing final cleanup...'
            
            // Archive important artifacts
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: 'trivy-report.json', allowEmptyArchive: true
            
            // Publish test results
            junit '**/target/surefire-reports/*.xml'
            
            echo '✅ Cleanup completed'
        }
    }
}