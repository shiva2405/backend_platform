import requests
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost:8080/api"

def add_product():
    product = {
        "name": "BulkItem",
        "description": "For load test",
        "price": 5.0,
        "stockQuantity": 100
    }
    response = requests.post(f"{BASE_URL}/products", json=product)
    print("Added product:", response.json())
    return response.json()["id"]

def process_order(product_id, order_num):
    order_id = f"order-{order_num}-{int(time.time())}"
    order = {
        "orderId": order_id,
        "items": [{"productId": product_id, "quantity": 1}]
    }
    try:
        response = requests.post(f"{BASE_URL}/orders/process", json=order)
        print(f"Order {order_num}: {response.status_code} - {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"Order {order_num} error: {e}")
        return False

def run_load_test(num_orders=50):
    product_id = add_product()
    time.sleep(2)
    
    successes = 0
    with ThreadPoolExecutor(max_workers=20) as executor:
        futures = [executor.submit(process_order, product_id, i) for i in range(num_orders)]
        for future in as_completed(futures):
            if future.result():
                successes += 1
    
    print(f"Successes: {successes}/{num_orders}")
    final = requests.get(f"{BASE_URL}/products/{product_id}")
    print("Final stock:", final.json()["stockQuantity"])
    # Expect ~100 - num_orders if all succeed

if __name__ == "__main__":
    run_load_test()
