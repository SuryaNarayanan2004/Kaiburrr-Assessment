# Kaiburr Task Manager - Kubernetes Deployment Guide

This guide will help you deploy the Kaiburr Task Manager application to Kubernetes using Minikube or Docker Desktop.

## Prerequisites

- Docker Desktop with Kubernetes enabled, OR
- Minikube installed and running
- kubectl installed and configured

## 🚀 Deployment Steps

### Step 1: Start Kubernetes Cluster

#### Option A: Docker Desktop
1. Open Docker Desktop
2. Go to Settings → Kubernetes
3. Enable Kubernetes and click "Apply & Restart"

#### Option B: Minikube
```bash
# Start Minikube cluster
minikube start

# Enable ingress (optional)
minikube addons enable ingress
```

### Step 2: Build and Load Docker Image

#### For Docker Desktop:
```bash
# Build the Docker image
docker build -t kaiburr-task-manager:latest .

# Tag for Docker Desktop Kubernetes
docker tag kaiburr-task-manager:latest kaiburr-task-manager:latest
```

#### For Minikube:
```bash
# Build the image in Minikube's Docker environment
minikube image build -t kaiburr-task-manager:latest .

# Or load existing image into Minikube
minikube image load kaiburr-task-manager:latest
```

### Step 3: Deploy MongoDB

```bash
# Deploy MongoDB with persistent storage
kubectl apply -f deployment-mongo.yaml

# Verify MongoDB deployment
kubectl get pods -l app=mongodb
kubectl get services -l app=mongodb
```

### Step 4: Deploy Spring Boot Application

```bash
# Deploy the Spring Boot application
kubectl apply -f deployment-app.yaml

# Verify application deployment
kubectl get pods -l app=kaiburr-task-manager
kubectl get services -l app=kaiburr-task-manager
```

### Step 5: Verify Deployment

```bash
# Check all pods are running
kubectl get pods

# Check services
kubectl get services

# Check persistent volumes
kubectl get pv
kubectl get pvc
```

### Step 6: Test the Application

#### Get the service URL:

**For Docker Desktop:**
```bash
# The service is accessible at localhost:30080
curl http://localhost:30080/tasks
```

**For Minikube:**
```bash
# Get the Minikube IP and service port
minikube service kaiburr-task-manager-service --url

# Test the endpoint (replace with actual URL from above command)
curl http://<minikube-ip>:30080/tasks
```

#### Test API endpoints:
```bash
# Get all tasks (should return empty array initially)
curl http://localhost:30080/tasks

# Create a new task
curl -X PUT http://localhost:30080/tasks \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Task","owner":"Test User","command":"echo Hello World"}'

# Get all tasks again (should show the created task)
curl http://localhost:30080/tasks
```

## 🔧 Useful Commands

### View Logs
```bash
# View application logs
kubectl logs -l app=kaiburr-task-manager

# View MongoDB logs
kubectl logs -l app=mongodb

# Follow logs in real-time
kubectl logs -f deployment/kaiburr-task-manager
```

### Debug Issues
```bash
# Describe pods for detailed information
kubectl describe pod <pod-name>

# Check events
kubectl get events --sort-by=.metadata.creationTimestamp

# Execute commands inside pods
kubectl exec -it <pod-name> -- /bin/bash
```

### Scale Application
```bash
# Scale the application to 3 replicas
kubectl scale deployment kaiburr-task-manager --replicas=3
```

## 🧹 Cleanup

```bash
# Delete all resources
kubectl delete -f deployment-app.yaml
kubectl delete -f deployment-mongo.yaml

# Or delete everything at once
kubectl delete -f .
```

## 📁 File Structure

```
k8s/
├── README.md                 # This deployment guide
├── deployment-app.yaml       # Spring Boot app deployment + service
└── deployment-mongo.yaml     # MongoDB deployment + service + persistent storage
```

## 🔍 Troubleshooting

### Common Issues:

1. **Image not found**: Make sure the Docker image is built and available in your Kubernetes cluster
2. **Pods not starting**: Check logs with `kubectl logs <pod-name>`
3. **Service not accessible**: Verify the service type and ports are correct
4. **MongoDB connection issues**: Ensure MongoDB pod is running and service is accessible

### Health Checks:
- Application health: `http://localhost:30080/tasks`
- MongoDB health: Check pod logs for connection status
