# Kubernetes Deployment Testing Guide

## 🧪 Testing the Kaiburr Task Manager Kubernetes Deployment

This guide will walk you through testing the Kubernetes deployment step by step.

## Prerequisites Check

### 1. Verify kubectl Installation
```bash
kubectl version --client
# Expected: Client Version: v1.34.1 (or similar)
```

### 2. Start Kubernetes Cluster

#### Option A: Docker Desktop (Recommended)
1. Open Docker Desktop
2. Go to **Settings** → **Kubernetes**
3. Check **"Enable Kubernetes"**
4. Click **"Apply & Restart"**
5. Wait for Kubernetes to start (green indicator)

#### Option B: Install and Start Minikube
```bash
# Install Minikube (Windows)
# Download from: https://minikube.sigs.k8s.io/docs/start/
# Or use Chocolatey: choco install minikube

# Start Minikube
minikube start
```

### 3. Verify Cluster is Running
```bash
kubectl cluster-info
# Expected: Kubernetes control plane is running at https://...
```

## 🚀 Deployment Testing Steps

### Step 1: Build Docker Image
```bash
# Navigate to project root
cd ..

# Build the application image
docker build -t kaiburr-task-manager:latest .
```

### Step 2: Load Image into Kubernetes

#### For Docker Desktop:
```bash
# Image should be automatically available
docker images | grep kaiburr-task-manager
```

#### For Minikube:
```bash
# Load image into Minikube
minikube image load kaiburr-task-manager:latest
```

### Step 3: Deploy MongoDB
```bash
# Navigate to k8s directory
cd k8s

# Deploy MongoDB with persistent storage
kubectl apply -f deployment-mongo.yaml

# Verify deployment
kubectl get pods -l app=mongodb
kubectl get services -l app=mongodb
kubectl get pv
kubectl get pvc
```

**Expected Output:**
```
NAME                     READY   STATUS    RESTARTS   AGE
mongodb-xxx-xxx          1/1     Running   0          30s

NAME             TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)     AGE
mongodb-service  ClusterIP   10.96.xxx.xxx <none>        27017/TCP   30s

NAME          CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM
mongodb-pv    2Gi        RWO            Retain           Bound    default/mongodb-pvc

NAME           STATUS   VOLUME        CAPACITY   ACCESS MODES
mongodb-pvc    Bound    mongodb-pv    2Gi        RWO
```

### Step 4: Deploy Spring Boot Application
```bash
# Deploy the application
kubectl apply -f deployment-app.yaml

# Verify deployment
kubectl get pods -l app=kaiburr-task-manager
kubectl get services -l app=kaiburr-task-manager
```

**Expected Output:**
```
NAME                           READY   STATUS    RESTARTS   AGE
kaiburr-task-manager-xxx-xxx   1/1     Running   0          45s

NAME                           TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
kaiburr-task-manager-service   NodePort   10.96.xxx.xxx <none>        8080:30080/TCP   45s
```

### Step 5: Monitor Deployment Progress
```bash
# Watch pods starting up
kubectl get pods -w

# Check application logs
kubectl logs -l app=kaiburr-task-manager

# Check MongoDB logs
kubectl logs -l app=mongodb
```

### Step 6: Test Application Endpoints

#### Get Service URL:
```bash
# For Docker Desktop
echo "Access URL: http://localhost:30080"

# For Minikube
minikube service kaiburr-task-manager-service --url
```

#### Test API Endpoints:
```bash
# Test 1: Health Check - Get all tasks
curl http://localhost:30080/tasks
# Expected: [] (empty array)

# Test 2: Create a task
curl -X PUT http://localhost:30080/tasks \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Task","owner":"Test User","command":"echo Hello World"}'

# Expected Response:
# {
#   "id": "xxx-xxx-xxx",
#   "name": "Test Task",
#   "owner": "Test User", 
#   "command": "echo Hello World",
#   "taskExecutions": []
# }

# Test 3: Get all tasks (should show the created task)
curl http://localhost:30080/tasks

# Test 4: Get task by ID
curl http://localhost:30080/tasks?id=<task-id-from-previous-response>

# Test 5: Search tasks by name
curl "http://localhost:30080/tasks/search?name=Test"

# Test 6: Execute a task
curl -X PUT http://localhost:30080/tasks/<task-id>/execute
```

## 🔍 Validation Checklist

### ✅ Deployment Validation
- [ ] MongoDB pod is running
- [ ] Spring Boot application pod is running
- [ ] Persistent volume is bound
- [ ] Services are created and accessible
- [ ] Application logs show successful startup
- [ ] MongoDB connection is established

### ✅ API Testing
- [ ] GET /tasks returns empty array initially
- [ ] PUT /tasks creates a new task successfully
- [ ] GET /tasks returns the created task
- [ ] GET /tasks?id=xxx returns specific task
- [ ] GET /tasks/search?name=xxx returns filtered results
- [ ] PUT /tasks/{id}/execute executes the task

### ✅ Data Persistence
- [ ] Data persists after pod restart
- [ ] MongoDB data survives pod deletion
- [ ] Persistent volume retains data

## 🐛 Troubleshooting

### Common Issues and Solutions:

#### 1. Pod Not Starting
```bash
# Check pod status
kubectl describe pod <pod-name>

# Check logs
kubectl logs <pod-name>

# Common causes:
# - Image not found: Build and load image correctly
# - Resource limits: Check available resources
# - Configuration errors: Validate YAML syntax
```

#### 2. Service Not Accessible
```bash
# Check service endpoints
kubectl get endpoints

# Check service details
kubectl describe service <service-name>

# Common causes:
# - Wrong service type
# - Incorrect port mapping
# - Firewall blocking access
```

#### 3. MongoDB Connection Issues
```bash
# Check MongoDB pod logs
kubectl logs -l app=mongodb

# Test MongoDB connectivity from app pod
kubectl exec -it <app-pod-name> -- nslookup mongodb-service

# Common causes:
# - MongoDB not ready
# - Wrong connection string
# - Network policy blocking traffic
```

#### 4. Persistent Volume Issues
```bash
# Check PV and PVC status
kubectl get pv,pvc

# Check storage class
kubectl get storageclass

# Common causes:
# - No available storage
# - Wrong access mode
# - Storage class mismatch
```

## 🧹 Cleanup After Testing

```bash
# Delete all resources
kubectl delete -f deployment-app.yaml
kubectl delete -f deployment-mongo.yaml

# Verify cleanup
kubectl get pods
kubectl get services
kubectl get pv,pvc
```

## 📊 Performance Testing

### Load Testing (Optional)
```bash
# Install hey (HTTP load testing tool)
# go install github.com/rakyll/hey@latest

# Run load test
hey -n 100 -c 10 http://localhost:30080/tasks
```

### Resource Monitoring
```bash
# Monitor resource usage
kubectl top pods
kubectl top nodes

# Check resource requests vs limits
kubectl describe pod <pod-name>
```

## 🎯 Success Criteria

The deployment is successful when:
1. All pods are running and healthy
2. Services are accessible from outside the cluster
3. API endpoints respond correctly
4. Data persists across pod restarts
5. Application handles concurrent requests
6. Logs show no errors or warnings

## 📝 Test Results Template

```
Date: ___________
Tester: ___________
Environment: Docker Desktop / Minikube (circle one)
Kubernetes Version: ___________

✅ Deployment Status:
- MongoDB Pod: Running / Failed
- App Pod: Running / Failed
- Services: Created / Failed
- PV/PVC: Bound / Failed

✅ API Testing:
- GET /tasks: Pass / Fail
- PUT /tasks: Pass / Fail
- GET /tasks?id=: Pass / Fail
- Search: Pass / Fail
- Execute: Pass / Fail

✅ Data Persistence:
- Data survives restart: Pass / Fail
- PV retains data: Pass / Fail

Issues Found:
1. ________________
2. ________________

Overall Status: ✅ PASS / ❌ FAIL
```

