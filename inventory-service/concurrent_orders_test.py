import requests
import threading
import time

BASE_URL = "http://localhost:8080/api"

def add_product():
    product = {
        "name": "TestItem",
        "description": "For concurrency test",
        "price": 10.0,
        "stockQuantity": 10
    }
    response = requests.post(f"{BASE_URL}/products", json=product)
    print("Added product:", response.json())
    return response.json()["id"]

def process_order(product_id, order_id):
    order = {
        "items": [{"productId": product_id, "quantity": 6}]
    }
    try:
        response = requests.post(f"{BASE_URL}/orders/process", json=order)
        print(f"Order {order_id}: {response.status_code} - {response.text}")
        return response.text
    except Exception as e:
        print(f"Order {order_id} error: {e}")
        return str(e)

def run_concurrent_test():
    product_id = add_product()
    time.sleep(2)  # Wait for DB
    
    # Simulate 2 concurrent orders (over stock)
    threads = []
    results = []
    for i in range(2):
        t = threading.Thread(target=lambda i=i: results.append(process_order(product_id, i)))
        threads.append(t)
        t.start()
    
    for t in threads:
        t.join()
    
    print("Results:", results)
    # Final stock check
    final = requests.get(f"{BASE_URL}/products/{product_id}")
    print("Final stock:", final.json()["stockQuantity"])
    # Expect stock >=0, one order succeeded

if __name__ == "__main__":
    run_concurrent_test()
