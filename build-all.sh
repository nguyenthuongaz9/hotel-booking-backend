echo "Building all microservices..."

services=("discovery_service" "api_gateway" "hotel_service" "order_service" "payment_service" "user_service")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd $service
    mvn clean package -DskipTests
    cd ..
done

echo "All services built successfully!"