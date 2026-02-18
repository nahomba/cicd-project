pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh './mvnw clean test'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t appiontment-app .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                  docker rm -f appiontment-app || true
                  docker run -d -p 8080:8080 --name appiontment-app appiontment-app
                '''
            }
        }
    }
}
