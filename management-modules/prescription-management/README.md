# Prescription Management Module

## Purpose

Admin controls for uploaded prescriptions and Rx review decisions.

## Main Files

- `src/main/java/com/medistore/controller/PrescriptionController.java`
- `src/main/java/com/medistore/service/PrescriptionService.java`
- `src/main/java/com/medistore/repository/PrescriptionRepository.java`
- `src/main/java/com/medistore/entity/Prescription.java`
- `src/main/java/com/medistore/entity/enums/PrescriptionStatus.java`
- `src/main/resources/templates/prescription/list.html`
- `src/main/resources/templates/prescription/review.html`
- `src/main/resources/templates/prescription/upload.html`

## Admin CRUD Coverage

- Create: customer uploads prescription.
- Read: admin views all prescriptions and pending Rx queue.
- Update: admin approves, rejects, or keeps prescriptions pending with notes.
- Delete: admin deletes prescription records.

## Persistence

All module records are stored through Spring Data JPA repositories in the configured database. No module data is saved to text files.
