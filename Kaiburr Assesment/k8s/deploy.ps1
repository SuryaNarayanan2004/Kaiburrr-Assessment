# Kaiburr Task Manager - Kubernetes Deployment Script (PowerShell)
# This script automates the deployment process on Windows

Write-Host "Starting Kaiburr Task Manager Kubernetes Deployment..." -ForegroundColor Green

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Check if kubectl is installed
try {
    $kubectlVersion = kubectl version --client 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "kubectl not found"
    }
    Write-Status "kubectl is installed"
} catch {
    Write-Error "kubectl is not installed. Please install kubectl first."
    exit 1
}

# Check if Kubernetes cluster is running
try {
    kubectl cluster-info 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Cluster not accessible"
    }
    Write-Status "Kubernetes cluster is running"
} catch {
    Write-Error "Kubernetes cluster is not running. Please start your cluster first."
    Write-Host ""
    Write-Host "To enable Kubernetes in Docker Desktop:" -ForegroundColor Cyan
    Write-Host "1. Open Docker Desktop" -ForegroundColor Cyan
    Write-Host "2. Go to Settings -> Kubernetes" -ForegroundColor Cyan
    Write-Host "3. Enable Kubernetes and click 'Apply & Restart'" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Or install Minikube:" -ForegroundColor Cyan
    Write-Host "choco install minikube" -ForegroundColor Cyan
    Write-Host "minikube start" -ForegroundColor Cyan
    exit 1
}

# Build Docker image
Write-Status "Building Docker image..."
docker build -t kaiburr-task-manager:latest ./mongo

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to build Docker image"
    exit 1
}
Write-Status "Docker image built successfully"

# Check if we're using Minikube
try {
    $minikubeStatus = minikube status 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Detected Minikube, loading image..."
        minikube image load kaiburr-task-manager:latest
    }
} catch {
    Write-Status "Using Docker Desktop Kubernetes"
}

# Deploy MongoDB
Write-Status "Deploying MongoDB..."
kubectl apply -f "$PSScriptRoot\deployment-mongo.yaml"

# Wait for MongoDB to be ready
Write-Status "Waiting for MongoDB to be ready..."
kubectl wait --for=condition=ready pod -l app=mongodb --timeout=120s
if ($LASTEXITCODE -ne 0) {
    Write-Error "MongoDB deployment failed or timed out"
    Write-Status "Checking MongoDB pod status..."
    kubectl get pods -l app=mongodb
    kubectl logs -l app=mongodb
    exit 1
}

# Deploy Spring Boot application
Write-Status "Deploying Spring Boot application..."
kubectl apply -f k8s/deployment-app.yaml


# Wait for application to be ready
Write-Status "Waiting for application to be ready..."
kubectl wait --for=condition=ready pod -l app=kaiburr-task-manager --timeout=120s
if ($LASTEXITCODE -ne 0) {
    Write-Error "Application deployment failed or timed out"
    Write-Status "Checking application pod status..."
    kubectl get pods -l app=kaiburr-task-manager
    kubectl logs -l app=kaiburr-task-manager
    exit 1
}

# Show deployment status
Write-Status "Deployment completed successfully!"
Write-Host ""
Write-Host "Deployment Status:" -ForegroundColor Cyan
kubectl get pods
Write-Host ""
Write-Host "Services:" -ForegroundColor Cyan
kubectl get services
Write-Host ""

# Show access information
try {
    $minikubeStatus = minikube status 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Access URLs (Minikube):" -ForegroundColor Cyan
        minikube service kaiburr-task-manager-service --url
    }
} catch {
    Write-Host "Access URL (Docker Desktop):" -ForegroundColor Cyan
    Write-Host "   http://localhost:30080" -ForegroundColor White
}

Write-Host ""
Write-Status "You can now test the API endpoints:"
Write-Host "   GET  http://localhost:30080/tasks" -ForegroundColor White
Write-Host "   PUT  http://localhost:30080/tasks" -ForegroundColor White
Write-Host ""
Write-Host "Deployment completed successfully!" -ForegroundColor Green

# Optional: Run a quick test
Write-Host ""
Write-Host "Running quick API test..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:30080/tasks" -Method GET -TimeoutSec 10
    Write-Status "API test successful! Response: $response"
} catch {
    Write-Warning "API test failed. Please check the application logs."
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

