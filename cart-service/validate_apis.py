import requests

BASE_URL = "http://localhost:8081/api/cart"

def test_add_item():
    item = {
        "userId": "user1",
        "productId": 1,
        "quantity": 2
    }
    response = requests.post(f"{BASE_URL}", json=item)
    print("Add Item:", response.status_code, response.json())
    return response.json()["id"]

def test_get_cart(user_id="user1"):
    response = requests.get(f"{BASE_URL}/{user_id}")
    print("Get Cart:", response.status_code, response.json())

def test_update_item(item_id):
    update = {"userId": "user1", "productId": 1, "quantity": 3}
    response = requests.put(f"{BASE_URL}/{item_id}", json=update)
    print("Update Item:", response.status_code, response.json())

def test_remove_item(item_id):
    response = requests.delete(f"{BASE_URL}/{item_id}")
    print("Remove Item:", response.status_code)

def test_clear_cart(user_id="user1"):
    response = requests.delete(f"{BASE_URL}/clear/{user_id}")
    print("Clear Cart:", response.status_code)

if __name__ == "__main__":
    print("Testing Cart APIs...")
    item_id = test_add_item()
    test_get_cart()
    test_update_item(item_id)
    test_get_cart()
    test_remove_item(item_id)
    test_get_cart()
    # Add another and clear
    item_id2 = test_add_item()
    test_clear_cart()
    test_get_cart()
