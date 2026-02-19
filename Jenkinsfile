pipeline {
    agent any

    environment {
        // Docker Hub Configuration
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        DOCKER_HUB_USERNAME  = 'queenivas'
        DOCKER_IMAGE_NAME    = 'appointment-app'
        DOCKER_IMAGE_TAG     = "${BUILD_NUMBER}"
        DOCKER_IMAGE_LATEST  = "${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest"
        DOCKER_IMAGE_VERSION = "${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"

        // SonarQube Configuration
        SONARQUBE_ENV     = 'sonarqube'
        SONAR_PROJECT_KEY = 'appointment-app'

        // Kubernetes / Helm
        K8S_NAMESPACE     = 'default'
        HELM_CHART_PATH   = './helm/appointment-app'
        HELM_RELEASE_NAME = 'appointment-app'

        // Trivy
        TRIVY_SEVERITY = 'HIGH,CRITICAL'
    }

    stages {
        stage('🧹 Cleanup Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('📥 Checkout') {
            steps {
                checkout scm
            }
        }

        // Commented out SonarQube Analysis Stage
        /*
        stage('🔍 SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh """
                        ./mvnw clean verify sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY}
                    """
                }
            }
        }

        stage('⏳ Sonar Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        */

        stage('🔨 Build & Test') {
            steps {
                sh './mvnw clean test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('📦 Package') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('🐳 Docker Build') {
            steps {
                sh """
                    docker build -t ${DOCKER_IMAGE_VERSION} .
                    docker tag ${DOCKER_IMAGE_VERSION} ${DOCKER_IMAGE_LATEST}
                """
            }
        }

        stage('🔒 Trivy Image Scan') {
            steps {
                sh """
                    trivy image \
                    --severity ${TRIVY_SEVERITY} \
                    --exit-code 0 \
                    --format json \
                    --output trivy-report.json \
                    ${DOCKER_IMAGE_VERSION}
                """
            }
        }

        stage('📤 Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: DOCKER_CREDENTIALS_ID,
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                        docker push ${DOCKER_IMAGE_VERSION}
                        docker push ${DOCKER_IMAGE_LATEST}
                        docker logout
                    """
                }
            }
        }

        stage('📋 Helm Deploy to Minikube') {
            steps {
                sh """
                    helm upgrade --install ${HELM_RELEASE_NAME} ${HELM_CHART_PATH} \
                    --namespace ${K8S_NAMESPACE} \
                    --create-namespace \
                    --set image.repository=${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME} \
                    --set image.tag=${DOCKER_IMAGE_TAG} \
                    --wait
                """
            }
        }

        stage('✅ Verify Deployment') {
            steps {
                sh """
                    kubectl get pods -n ${K8S_NAMESPACE}
                    kubectl get svc -n ${K8S_NAMESPACE}
                    minikube service ${HELM_RELEASE_NAME} -n ${K8S_NAMESPACE} --url
                """
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: 'trivy-report.json', allowEmptyArchive: true
        }

        success {
            echo '✅ PIPELINE COMPLETED SUCCESSFULLY'
        }

        failure {
            echo '❌ PIPELINE FAILED — CHECK LOGS'
        }
    }
}