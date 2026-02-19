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
                            -Dsonar.host.url=\$SONAR_HOST_URL \
                            -Dsonar.login=\$SONAR_AUTH_TOKEN
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
        
        stage('📦 Package') {
            steps {
                echo '📦 Packaging application...'
                sh './mvnw clean package -DskipTests'
            }
        }
        
        stage('🐳 Docker Build') {
            steps {
                script {
                    echo "🐳 Building Docker image: ${DOCKER_IMAGE_VERSIONED}"
                    sh """
                        docker build -t ${DOCKER_IMAGE_VERSIONED} .
                        docker tag ${DOCKER_IMAGE_VERSIONED} ${DOCKER_IMAGE_LATEST}
                    """
                    echo "✅ Docker images built successfully"
                }
            }
        }
        
        stage('🔒 Trivy Security Scan - Filesystem') {
            steps {
                script {
                    echo '🔒 Scanning filesystem for vulnerabilities...'
                    sh """
                        trivy fs --severity ${TRIVY_SEVERITY} \
                        --format table \
                        --exit-code 0 \
                        .
                    """
                }
            }
        }
        
        stage('🔒 Trivy Security Scan - Docker Image') {
            steps {
                script {
                    echo "🔒 Scanning Docker image for vulnerabilities: ${DOCKER_IMAGE_VERSIONED}"
                    sh """
                        trivy image --severity ${TRIVY_SEVERITY} \
                        --format table \
                        --exit-code 0 \
                        ${DOCKER_IMAGE_VERSIONED}
                    """
                    
                    echo '📄 Generating detailed Trivy report...'
                    sh """
                        trivy image --severity ${TRIVY_SEVERITY} \
                        --format json \
                        --output trivy-report.json \
                        ${DOCKER_IMAGE_VERSIONED}
                    """
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.json', allowEmptyArchive: true
                }
            }
        }
        
        stage('📤 Push to Docker Hub') {
            steps {
                script {
                    echo '📤 Logging in to Docker Hub...'
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                        """
                    }
                    
                    echo "📤 Pushing image: ${DOCKER_IMAGE_VERSIONED}"
                    sh "docker push ${DOCKER_IMAGE_VERSIONED}"
                    
                    echo "📤 Pushing image: ${DOCKER_IMAGE_LATEST}"
                    sh "docker push ${DOCKER_IMAGE_LATEST}"
                    
                    echo '🔓 Logging out from Docker Hub...'
                    sh 'docker logout'
                    
                    echo "✅ Images pushed successfully to Docker Hub"
                    echo "🔗 View at: https://hub.docker.com/r/${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}"
                }
            }
        }
        
        stage('📋 Prepare Helm Chart') {
            steps {
                script {
                    echo '📋 Preparing Helm chart...'
                    
                    // Update values.yaml with new image tag
                    sh """
                        sed -i 's|tag:.*|tag: "${DOCKER_IMAGE_TAG}"|g' ${HELM_CHART_PATH}/values.yaml
                    """
                    
                    echo '✅ Helm chart prepared'
                }
            }
        }
        
        stage('🔍 Helm Lint') {
            steps {
                script {
                    echo '🔍 Linting Helm chart...'
                    sh """
                        helm lint ${HELM_CHART_PATH}
                    """
                    echo '✅ Helm chart is valid'
                }
            }
        }
        
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
        
        stage('☸️ Deploy to Minikube') {
            steps {
                script {
                    echo "☸️ Deploying to Minikube namespace: ${K8S_NAMESPACE}"
                    
                    // Check if namespace exists, create if not
                    sh """
                        kubectl get namespace ${K8S_NAMESPACE} || kubectl create namespace ${K8S_NAMESPACE}
                    """
                    
                    // Check if release exists
                    def releaseExists = sh(
                        script: "helm list -n ${K8S_NAMESPACE} | grep ${HELM_RELEASE_NAME}",
                        returnStatus: true
                    ) == 0
                    
                    if (releaseExists) {
                        echo "🔄 Upgrading existing Helm release: ${HELM_RELEASE_NAME}"
                        sh """
                            helm upgrade ${HELM_RELEASE_NAME} ${HELM_CHART_PATH} \
                            --namespace ${K8S_NAMESPACE} \
                            --set image.tag=${DOCKER_IMAGE_TAG} \
                            --set image.repository=${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME} \
                            --wait \
                            --timeout 5m
                        """
                    } else {
                        echo "🆕 Installing new Helm release: ${HELM_RELEASE_NAME}"
                        sh """
                            helm install ${HELM_RELEASE_NAME} ${HELM_CHART_PATH} \
                            --namespace ${K8S_NAMESPACE} \
                            --set image.tag=${DOCKER_IMAGE_TAG} \
                            --set image.repository=${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME} \
                            --wait \
                            --timeout 5m
                        """
                    }
                    
                    echo '✅ Deployment successful'
                }
            }
        }
        
        stage('✅ Verify Deployment') {
            steps {
                script {
                    echo '✅ Verifying deployment...'
                    
                    // Wait for pods to be ready
                    sh """
                        kubectl wait --for=condition=ready pod \
                        -l app.kubernetes.io/name=appointment-app \
                        -n ${K8S_NAMESPACE} \
                        --timeout=300s
                    """
                    
                    // Get deployment status
                    sh """
                        echo "=== DEPLOYMENT STATUS ==="
                        kubectl get deployment -n ${K8S_NAMESPACE} -l app.kubernetes.io/name=appointment-app
                        
                        echo ""
                        echo "=== PODS STATUS ==="
                        kubectl get pods -n ${K8S_NAMESPACE} -l app.kubernetes.io/name=appointment-app
                        
                        echo ""
                        echo "=== SERVICE STATUS ==="
                        kubectl get service -n ${K8S_NAMESPACE} -l app.kubernetes.io/name=appointment-app
                    """
                    
                    // Get service URL
                    def serviceUrl = sh(
                        script: "minikube service ${HELM_RELEASE_NAME} -n ${K8S_NAMESPACE} --url",
                        returnStdout: true
                    ).trim()
                    
                    echo "🌐 Application URL: ${serviceUrl}"
                    echo "✅ Deployment verified successfully"
                }
            }
        }
        
        stage('🧹 Cleanup Local Docker Images') {
            steps {
                script {
                    echo '🧹 Cleaning up local Docker images...'
                    sh """
                        docker rmi ${DOCKER_IMAGE_VERSIONED} || true
                        docker rmi ${DOCKER_IMAGE_LATEST} || true
                    """
                    echo '✅ Cleanup completed'
                }
            }
        }
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