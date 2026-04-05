# End-to-End DevSecOps Pipeline – Spring Boot Application

## Overview

This project demonstrates a **complete end-to-end DevSecOps pipeline** built around a Spring Boot application, covering the full software delivery lifecycle:

**Code → Build → Test → Security → Container → Deploy → Verify**

It reflects **real-world DevOps and platform engineering practices**, integrating quality gates, security scanning, containerization, and automated Kubernetes deployment.

---

## Architecture Overview

- **Application:** Spring Boot (Maven)  
- **CI/CD:** Jenkins (Declarative Pipeline)  
- **Code Quality:** SonarQube  
- **Security Scanning:** Trivy  
- **Containerization:** Docker  
- **Deployment:** Kubernetes (Minikube)  

---

## Pipeline Flow

```
Cleanup → Checkout → SonarQube → Quality Gate → Build & Test → Package → Docker Build → Trivy Scan → Push → Deploy → Verify
```

---

## Tech Stack

- Java / Spring Boot  
- Maven  
- Jenkins (Declarative Pipeline)  
- SonarQube (Code Quality Analysis)  
- Trivy (Container Security Scanning)  
- Docker  
- Kubernetes (Minikube)  

---

## DevSecOps Capabilities

### 🔹 Code Quality Enforcement
- Integrated **SonarQube static analysis**
- Enforced **quality gates** before proceeding in the pipeline

---

### 🔹 Security Scanning
- Integrated **Trivy image scanning**
- Detects vulnerabilities (HIGH, CRITICAL)
- Generates structured security reports (JSON)

---

### 🔹 Containerization
- Docker image build and tagging strategy:
  - `latest`
  - `<build-number>`
- Reproducible builds for consistent deployments

---

### 🔹 Kubernetes Deployment
- Dynamic manifest generation during pipeline execution  
- Automated:
  - Deployment creation  
  - Service exposure  
  - Rollout verification  

---

### 🔹 Observability & Artifacts

Pipeline outputs:

- **JUnit test reports**  
- **Trivy security reports (JSON)**  
- **Build artifacts (JAR files)**  

---

## Pipeline Stages Explained

### 1. Cleanup
Cleans Jenkins workspace to ensure a fresh build environment.

### 2. Checkout
Pulls the latest source code from GitHub.

### 3. SonarQube Analysis
Performs static code analysis for quality and maintainability.

### 4. Quality Gate
Validates code against predefined quality standards.

### 5. Build & Test
Executes unit tests using Maven.

### 6. Package
Builds the application JAR artifact.

### 7. Docker Build
Builds and tags the Docker image.

### 8. Trivy Scan
Scans the container image for vulnerabilities.

### 9. Push to Docker Hub
Pushes versioned and latest images to the registry.

### 10. Deploy to Kubernetes
- Creates Deployment and Service  
- Applies configuration  
- Waits for rollout completion  

### 11. Verify Deployment
Validates:
- Pods  
- Services  
- Deployment health  

---

## Docker Image

- `queenivas/appointment-app:latest`  
- `queenivas/appointment-app:<build-number>`  

---

## Kubernetes Deployment

- **Namespace:** default  
- **Replicas:** 2  
- **Service Type:** NodePort  
- **Port:** 30080  

Access the application:

```
minikube service appointment-app --url
```

---

## Project Structure

```
.
├── Jenkinsfile
├── Dockerfile
├── pom.xml
├── src/
├── target/
```

---

## Key Highlights

- End-to-end DevSecOps pipeline implementation  
- Integrated security scanning (Trivy)  
- Code quality enforcement (SonarQube)  
- Automated Kubernetes deployment  
- Production-like workflow using Minikube  

---

## Future Improvements

- Helm-based Kubernetes deployments  
- GitOps integration (ArgoCD / Flux)  
- TLS + Ingress Controller  
- Deployment to managed Kubernetes (AKS / EKS)  
- Advanced observability (Prometheus + Grafana)  

---

## Summary

This project demonstrates how to:

- Build a secure CI/CD pipeline  
- Integrate security into DevOps workflows (DevSecOps)  
- Automate containerized application delivery  
- Deploy and validate workloads in Kubernetes

  
<img width="1805" height="830" alt="Jenkins" src="https://github.com/user-attachments/assets/df26f0a5-c12c-4b9c-895c-27fa9485930d" />

---

## Author

DevOps & Cloud Engineer  
Focused on DevSecOps, Cloud Architecture, and Platform Engineering
