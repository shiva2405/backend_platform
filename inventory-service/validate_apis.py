import requests
import json

BASE_URL = "http://localhost:8080/api"

def test_add_product():
    product = {
        "name": "Laptop",
        "description": "High performance laptop",
        "price": 999.99,
        "stockQuantity": 50
    }
    response = requests.post(f"{BASE_URL}/products", json=product)
    print("Add Product:", response.status_code, response.json())
    return response.json()["id"]

def test_get_products():
    response = requests.get(f"{BASE_URL}/products")
    print("Get Products:", response.status_code, response.json())

def test_update_product(product_id):
    update_data = {
        "name": "Laptop Updated",
        "description": "Updated description",
        "price": 1099.99,
        "stockQuantity": 45
    }
    response = requests.put(f"{BASE_URL}/products/{product_id}", json=update_data)
    print("Update Product:", response.status_code, response.json())

def test_process_order(product_id):
    order = {
        "items": [
            {
                "productId": product_id,
                "quantity": 5
            }
        ]
    }
    response = requests.post(f"{BASE_URL}/orders/process", json=order)
    print("Process Order:", response.status_code, response.text)

if __name__ == "__main__":
    # Note: This assumes the Spring Boot app is running on localhost:8080
    # In practice, run the app first, then this script.
    print("Testing Inventory APIs...")
    product_id = test_add_product()
    test_get_products()
    test_update_product(product_id)
    test_process_order(product_id)
    test_get_products()
