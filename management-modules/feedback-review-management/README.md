# Feedback and Review Management Module

## Purpose

Customer feedback submission and admin review moderation.

## Main Files

- `src/main/java/com/medistore/controller/ReviewController.java`
- `src/main/java/com/medistore/controller/MedicineController.java`
- `src/main/java/com/medistore/service/ReviewService.java`
- `src/main/java/com/medistore/repository/ReviewRepository.java`
- `src/main/java/com/medistore/entity/Review.java`
- `src/main/java/com/medistore/entity/NormalReview.java`
- `src/main/java/com/medistore/entity/VerifiedReview.java`
- `src/main/resources/templates/review/submit.html`
- `src/main/resources/templates/review/list.html`
- `src/main/resources/templates/review/moderate.html`
- `src/main/resources/templates/medicine/detail.html`

## Admin CRUD Coverage

- Create: customers submit reviews.
- Read: admin views pending and all reviews.
- Update: admin approves reviews; customers edit submitted reviews directly in the existing review submission form, including reviews still pending moderation.
- Delete: admin rejects/removes reviews.

## Persistence

All module records are stored through Spring Data JPA repositories in the configured database. No module data is saved to text files.
