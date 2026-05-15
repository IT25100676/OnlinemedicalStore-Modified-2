# Order Management Module

## Purpose

Admin controls for customer orders, delivery details, and order status.

## Main Files

- `src/main/java/com/example/OnlineMedicalStore/controller/OrderController.java`
- `src/main/java/com/example/OnlineMedicalStore/service/OrderService.java`
- `src/main/java/com/example/OnlineMedicalStore/repository/OrderRepository.java`
- `src/main/java/com/example/OnlineMedicalStore/entity/Order.java`
- `src/main/java/com/example/OnlineMedicalStore/entity/OrderItem.java`
- `src/main/java/com/example/OnlineMedicalStore/entity/enums/OrderStatus.java`
- `src/main/resources/templates/order/cart.html`
- `src/main/resources/templates/order/checkout.html`
- `src/main/resources/templates/order/detail.html`
- `src/main/resources/templates/order/history.html`

## Admin CRUD Coverage

- Create: customer checkout creates orders.
- Read: admin views all orders.
- Update: admin updates order status, delivery address, and notes.
- Delete: admin deletes orders.

## Data Files

- `data/orders.txt`
