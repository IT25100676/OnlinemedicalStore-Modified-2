# User Management Module

## Purpose

Admin controls for customer accounts and separate administrator credentials.

## Main Files

- `src/main/java/com/medistore/controller/UserController.java`
- `src/main/java/com/medistore/controller/AuthController.java`
- `src/main/java/com/medistore/service/UserService.java`
- `src/main/java/com/medistore/service/AdminService.java`
- `src/main/java/com/medistore/repository/UserRepository.java`
- `src/main/java/com/medistore/repository/AdminRepository.java`
- `src/main/java/com/medistore/entity/User.java`
- `src/main/java/com/medistore/entity/Customer.java`
- `src/main/java/com/medistore/entity/Admin.java`
- `src/main/resources/templates/user/list.html`
- `src/main/resources/templates/auth/admin-login.html`
- `src/main/resources/templates/auth/customer-login.html`
- `src/main/resources/templates/auth/register.html`

## Admin CRUD Coverage

- Create: create administrator credentials.
- Read: view customers and admin accounts.
- Update: update customer contact details and password.
- Delete/Deactivate: deactivate customer accounts.

## Persistence

All module records are stored through Spring Data JPA repositories in the configured database. No module data is saved to text files.
