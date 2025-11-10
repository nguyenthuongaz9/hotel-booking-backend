#!/bin/bash
echo "Building Docker images for all services..."

services=("discovery_service" "api_gateway" "hotel_service" "order_service" "payment_service" "user_service")

for service in "${services[@]}"; do
    echo "Building Docker image for $service..."
    docker build -t hotel-booking/$service:latest ./$service
done

echo "All Docker images built successfully!"