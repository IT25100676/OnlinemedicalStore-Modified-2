# Medicine Management Module

## Purpose

Admin controls for medicines, stock, pricing, prescription requirement, and medicine details.

## Main Files

- `src/main/java/com/medistore/controller/MedicineController.java`
- `src/main/java/com/medistore/service/MedicineService.java`
- `src/main/java/com/medistore/repository/MedicineRepository.java`
- `src/main/java/com/medistore/entity/Medicine.java`
- `src/main/java/com/medistore/entity/Tablet.java`
- `src/main/java/com/medistore/entity/Syrup.java`
- `src/main/java/com/medistore/entity/Injection.java`
- `src/main/java/com/medistore/entity/enums/MedicineType.java`
- `src/main/resources/templates/medicine/add.html`
- `src/main/resources/templates/medicine/edit.html`
- `src/main/resources/templates/medicine/list.html`
- `src/main/resources/templates/medicine/detail.html`

## Admin CRUD Coverage

- Create: add medicine.
- Read: view/search/filter medicines.
- Update: edit medicine details and stock.
- Delete: delete medicine.

## Persistence

All module records are stored through Spring Data JPA repositories in the configured database. No module data is saved to text files.
