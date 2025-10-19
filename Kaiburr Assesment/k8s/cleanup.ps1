# Kaiburr Task Manager - Kubernetes Cleanup Script (PowerShell)
# This script removes all deployed resources

Write-Host "🧹 Cleaning up Kaiburr Task Manager Kubernetes deployment..." -ForegroundColor Yellow

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
    kubectl version --client 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "kubectl not found"
    }
} catch {
    Write-Error "kubectl is not installed."
    exit 1
}

# Delete Spring Boot application
Write-Status "Deleting Spring Boot application..."
kubectl delete -f deployment-app.yaml

# Delete MongoDB
Write-Status "Deleting MongoDB..."
kubectl delete -f deployment-mongo.yaml

# Wait for pods to terminate
Write-Status "Waiting for pods to terminate..."
kubectl wait --for=delete pod -l app=kaiburr-task-manager --timeout=60s 2>$null
kubectl wait --for=delete pod -l app=mongodb --timeout=60s 2>$null

# Show remaining resources
Write-Host ""
Write-Status "Remaining resources:"
Write-Host "Pods:" -ForegroundColor Cyan
kubectl get pods
Write-Host ""
Write-Host "Services:" -ForegroundColor Cyan
kubectl get services
Write-Host ""
Write-Host "Persistent Volumes:" -ForegroundColor Cyan
kubectl get pv
Write-Host ""
Write-Host "Persistent Volume Claims:" -ForegroundColor Cyan
kubectl get pvc

Write-Host ""
Write-Status "Cleanup completed!"
Write-Warning "Note: Persistent volumes and claims may still exist. Use the following commands if you want to remove them completely:"
Write-Host "kubectl delete pv mongodb-pv" -ForegroundColor White
Write-Host "kubectl delete pvc mongodb-pvc" -ForegroundColor White

