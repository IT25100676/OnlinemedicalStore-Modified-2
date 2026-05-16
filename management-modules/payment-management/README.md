# Payment Management Module

## Purpose

Admin controls for payment records and payment status.

## Main Files

- `src/main/java/com/medistore/controller/PaymentController.java`
- `src/main/java/com/medistore/controller/AdminPaymentController.java`
- `src/main/java/com/medistore/service/PaymentService.java`
- `src/main/java/com/medistore/repository/PaymentRepository.java`
- `src/main/java/com/medistore/entity/Payment.java`
- `src/main/java/com/medistore/entity/CardPayment.java`
- `src/main/java/com/medistore/entity/CashOnDelivery.java`
- `src/main/java/com/medistore/entity/enums/PaymentStatus.java`
- `src/main/java/com/medistore/entity/enums/PaymentType.java`
- `src/main/resources/templates/payment/page.html`
- `src/main/resources/templates/payment/history.html`
- `src/main/resources/templates/payment/invoice.html`

## Admin CRUD Coverage

- Create: payment processing creates records.
- Read: admin views all payment records.
- Update: admin updates payment status.
- Delete: admin deletes payment records.

## Persistence

All module records are stored through Spring Data JPA repositories in the configured database. No module data is saved to text files.
