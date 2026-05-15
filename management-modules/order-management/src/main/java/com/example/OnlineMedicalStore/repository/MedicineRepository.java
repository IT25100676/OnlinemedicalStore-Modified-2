package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Medicine;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Repository
public class MedicineRepository extends AbstractTextFileRepository<Medicine> {

    public MedicineRepository() {
        super("medicines.txt");
    }

    @Override
    public Medicine save(Medicine medicine) {
        if (medicine.getCreatedAt() == null) {
            medicine.setCreatedAt(LocalDateTime.now());
        }
        return super.save(medicine);
    }

    public List<Medicine> findByNameContainingIgnoreCase(String name) {
        String value = lower(name);
        return findAll().stream()
                .filter(medicine -> lower(medicine.getName()).contains(value))
                .toList();
    }

    public List<Medicine> findByCategoryIgnoreCase(String category) {
        String value = lower(category);
        return findAll().stream()
                .filter(medicine -> lower(medicine.getCategory()).equals(value))
                .toList();
    }

    public List<Medicine> findByExpiryDateBefore(LocalDate date) {
        return findAll().stream()
                .filter(medicine -> medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(date))
                .toList();
    }

    public List<Medicine> findByStockLessThan(int threshold) {
        return findAll().stream()
                .filter(medicine -> medicine.getStock() != null && medicine.getStock() < threshold)
                .toList();
    }

    public List<String> findAllCategories() {
        return findAll().stream()
                .map(Medicine::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<Medicine> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String cat) {
        String nameValue = lower(name);
        String categoryValue = lower(cat);
        return findAll().stream()
                .filter(medicine -> lower(medicine.getName()).contains(nameValue)
                        || lower(medicine.getCategory()).contains(categoryValue))
                .sorted(Comparator.comparing(Medicine::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public List<Medicine> findExpiredOrOutOfStock(LocalDate today) {
        return findAll().stream()
                .filter(medicine -> (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(today))
                        || (medicine.getStock() != null && medicine.getStock() == 0))
                .toList();
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
