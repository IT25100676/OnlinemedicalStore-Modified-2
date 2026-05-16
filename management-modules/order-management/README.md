# Order Management Module

## Purpose

Admin controls for customer orders, delivery details, and order status.

## Main Files

- `src/main/java/com/medistore/controller/OrderController.java`
- `src/main/java/com/medistore/service/OrderService.java`
- `src/main/java/com/medistore/repository/OrderRepository.java`
- `src/main/java/com/medistore/entity/Order.java`
- `src/main/java/com/medistore/entity/OrderItem.java`
- `src/main/java/com/medistore/entity/enums/OrderStatus.java`
- `src/main/resources/templates/order/cart.html`
- `src/main/resources/templates/order/checkout.html`
- `src/main/resources/templates/order/detail.html`
- `src/main/resources/templates/order/history.html`

## Admin CRUD Coverage

- Create: customer checkout creates orders.
- Read: admin views all orders.
- Update: admin updates order status, delivery address, and notes.
- Delete: admin deletes orders.

## Persistence

All module records are stored through Spring Data JPA repositories in the configured database. No module data is saved to text files.
