# Management Modules

This folder separates the administrative management work into branch-friendly module folders.

The latest `main` branch uses the `medistore` Spring Boot project scaffold, package namespace `com.medistore`, and application entry point `MedistoreApplication`. Each module copy has been aligned to that baseline while retaining the feature files needed for its management area.

## Suggested Branches

- `feature/user-management`
- `feature/medicine-management`
- `feature/order-management`
- `feature/prescription-management`
- `feature/payment-management`
- `feature/review-management`

## Merge Order

1. User Management
2. Medicine Management
3. Order Management
4. Prescription Management
5. Payment Management
6. Feedback and Review Management

After each merge, run from the relevant module folder:

```bat
mvn test
```

For final integration, run:

```bat
mvn -DskipTests package
java -jar target\medistore-0.0.1-SNAPSHOT.jar
```