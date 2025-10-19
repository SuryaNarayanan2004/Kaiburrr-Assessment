#!/bin/bash

# Kaiburr Task Manager - Kubernetes Cleanup Script
# This script removes all deployed resources

echo "🧹 Cleaning up Kaiburr Task Manager Kubernetes deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed."
    exit 1
fi

# Delete Spring Boot application
print_status "Deleting Spring Boot application..."
kubectl delete -f deployment-app.yaml

# Delete MongoDB
print_status "Deleting MongoDB..."
kubectl delete -f deployment-mongo.yaml

# Wait for pods to terminate
print_status "Waiting for pods to terminate..."
kubectl wait --for=delete pod -l app=kaiburr-task-manager --timeout=60s
kubectl wait --for=delete pod -l app=mongodb --timeout=60s

# Show remaining resources
echo ""
print_status "Remaining resources:"
kubectl get pods
kubectl get services
kubectl get pv
kubectl get pvc

echo ""
print_status "Cleanup completed!"
print_warning "Note: Persistent volumes and claims may still exist. Use 'kubectl delete pv mongodb-pv' and 'kubectl delete pvc mongodb-pvc' if you want to remove them completely."
