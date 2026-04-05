# cicd-project
#  End-to-End DevSecOps Pipeline – Spring Boot Application

This project demonstrates a **complete DevSecOps pipeline** built around a Spring Boot application, covering:

 Code → Build → Test → Security → Container → Deploy → Verify

It showcases real-world practices used in modern platform engineering and DevOps environments.


#  Architecture Overview

Application: Spring Boot (Maven)  
CI/CD: Jenkins Pipeline  
Security: SonarQube + Trivy  
Containerization: Docker  
Deployment: Kubernetes (Minikube)


#  Pipeline Flow

Cleanup → Checkout → SonarQube → Quality Gate → Build & Test → Package → Docker Build → Trivy Scan → Push → Deploy → Verify

Tech Stack
Java / Spring Boot
Maven
Jenkins (Declarative Pipeline)
SonarQube (Code Quality)
Trivy (Container Security)
Docker
Kubernetes (Minikube)
 DevSecOps Features
 Code Quality
Integrated SonarQube analysis
Enforced quality gates before continuing pipeline
 Security Scanning
Integrated Trivy image scanning
Detects vulnerabilities (HIGH, CRITICAL)
 Containerization
Docker build with versioning:
latest
build-number
 Kubernetes Deployment
Dynamic manifest generation
Deployment + Service creation
Automated rollout verification
 Observability

Pipeline outputs:

Test reports (JUnit)
Security reports (Trivy JSON)
Build artifacts (JAR)
 Pipeline Stages Explained
1. Cleanup

Cleans Jenkins workspace to ensure fresh builds

2. Checkout

Pulls latest code from GitHub

3. SonarQube Analysis

Runs static code analysis

4. Quality Gate

Ensures code meets defined quality standards

5. Build & Test

Runs unit tests using Maven

6. Package

Builds application JAR

7. Docker Build

Builds and tags Docker image

8. Trivy Scan

Scans image for vulnerabilities

9. Push to Docker Hub

Pushes image to registry

10. Deploy to Kubernetes
Creates deployment + service
Applies configuration
Waits for rollout
11. Verify Deployment

Checks pods, services, and deployment status

 Docker Image
queenivas/appointment-app:latest
queenivas/appointment-app:<build-number>
 Kubernetes Deployment
Namespace: default
Replicas: 2
Service Type: NodePort
Port: 30080

Access application:

minikube service appointment-app --url
 Project Structure
.
├── Jenkinsfile
├── Dockerfile
├── pom.xml
├── src/
├── target/
 Key Highlights
End-to-end DevSecOps pipeline
Integrated security scanning (Trivy)
Code quality enforcement (SonarQube)
Automated Kubernetes deployment
Production-like workflow using Minikube
 Future Improvements
Helm charts for deployment
GitOps (ArgoCD / Flux)
TLS + Ingress controller
External Kubernetes cluster (AKS / EKS)
Observability (Prometheus + Grafana)
