#!/bin/bash
./reset-docker.sh
echo "=== COMPLETE HOTEL BOOKING SYSTEM DEPLOYMENT ==="

echo "0. Cleaning up..."
docker-compose down
docker system prune -f

echo "1. Building all services..."
services=("discovery_service" "api_gateway" "user_service" "hotel_service" "order_service" "payment_service")

for service in "${services[@]}"; do
    echo "  Building $service..."
    service_dir="${service//_/-}"  
    if [ -d "$service_dir" ]; then
        cd "$service_dir"
        ./mvnw clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo "ERROR: Build failed for $service"
            exit 1
        fi
        cd ..
        docker build -t "hotel-booking/${service_dir}:latest" "./$service_dir"
    else
        echo "WARNING: Directory $service_dir not found"
    fi
done

echo "2. Starting infrastructure services..."
docker-compose up -d mysql-db mongodb keycloak zipkin

echo "3. Waiting for infrastructure to be ready..."
echo "   Waiting for MySQL..."
until docker exec mysql-db mysqladmin ping -h localhost -uroot -proot --silent; do
    printf '.'
    sleep 5
done
echo " MySQL OK"

echo "   Waiting for MongoDB..."
until docker exec mongodb mongosh --eval "db.adminCommand('ping')" --quiet > /dev/null; do
    printf '.'
    sleep 5
done
echo " MongoDB OK"

echo "   Waiting for Keycloak..."
until curl -f http://localhost:8181/realms/master > /dev/null 2>&1; do
    printf '.'
    sleep 10
done
echo " Keycloak OK"

echo "   Waiting for Zipkin..."
until curl -f http://localhost:9411/health > /dev/null 2>&1; do
    printf '.'
    sleep 5
done
echo " Zipkin OK"

echo "4. Starting Discovery Service..."
docker-compose up -d discovery-service

echo "   Waiting for Discovery Service..."

echo " Discovery Service OK"

echo "5. Starting microservices..."
docker-compose up -d user-service hotel-service order-service payment-service api-gateway

echo "6. Waiting for system to stabilize..."
sleep 30

echo "7. Final status check..."
echo ""
docker-compose ps

echo ""
echo "=== DEPLOYMENT COMPLETE ==="
echo "Keycloak Admin: http://localhost:8181 (admin/admin)"
echo "Eureka Dashboard: http://localhost:8671" 
echo "API Gateway: http://localhost:8900"
echo "Zipkin Tracing: http://localhost:9411"
echo "MySQL: localhost:3306 (root/root)"
echo "MongoDB: localhost:27017"
echo ""
echo "Check logs: docker-compose logs -f [service-name]"