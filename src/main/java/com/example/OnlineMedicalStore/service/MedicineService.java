package com.example.OnlineMedicalStore.service;

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.entity.enums.MedicineType;
import com.example.OnlineMedicalStore.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public List<Medicine> findAll() { return medicineRepository.findAll(); }

    public Optional<Medicine> findById(Long id) { return medicineRepository.findById(id); }

    public List<Medicine> search(String query) {
        return medicineRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query);
    }

    public List<Medicine> findByCategory(String category) {
        return medicineRepository.findByCategoryIgnoreCase(category);
    }

    public List<String> getAllCategories() { return medicineRepository.findAllCategories(); }

    public List<Medicine> findExpiringSoon() {
        return medicineRepository.findByExpiryDateBefore(LocalDate.now().plusDays(30));
    }

    public List<Medicine> findLowStock() { return medicineRepository.findByStockLessThan(10); }

    public long countAll() { return medicineRepository.count(); }

    @Transactional
    public Medicine addMedicine(String type, Map<String, String> params) {
        Medicine m = createByType(type);
        applyCommonFields(m, params);
        applyTypeFields(m, type, params);
        return medicineRepository.save(m);
    }

    @Transactional
    public Medicine updateMedicine(Long id, Map<String, String> params) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        applyCommonFields(m, params);
        String type = m.getMedicineType().name();
        applyTypeFields(m, type, params);
        return medicineRepository.save(m);
    }

    @Transactional
    public void deleteMedicine(Long id) { medicineRepository.deleteById(id); }

    @Transactional
    public void updateStock(Long id, int delta) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        m.setStock(Math.max(0, m.getStock() + delta));
        medicineRepository.save(m);
    }

    private Medicine createByType(String type) {
        return switch (type.toUpperCase()) {
            case "TABLET" -> new Tablet();
            case "SYRUP" -> new Syrup();
            case "INJECTION" -> new Injection();
            default -> throw new RuntimeException("Unknown medicine type: " + type);
        };
    }

    private void applyCommonFields(Medicine m, Map<String, String> p) {
        if (p.containsKey("name")) m.setName(p.get("name"));
        if (p.containsKey("category")) m.setCategory(p.get("category"));
        if (p.containsKey("price")) m.setPrice(new BigDecimal(p.get("price")));
        if (p.containsKey("stock")) m.setStock(Integer.parseInt(p.get("stock")));
        if (p.containsKey("expiryDate") && !p.get("expiryDate").isBlank())
            m.setExpiryDate(LocalDate.parse(p.get("expiryDate")));
        if (p.containsKey("description")) m.setDescription(p.get("description"));
        if (p.containsKey("manufacturer")) m.setManufacturer(p.get("manufacturer"));
        m.setRequiresPrescription("on".equals(p.get("requiresPrescription")) || "true".equals(p.get("requiresPrescription")));
    }

    private void applyTypeFields(Medicine m, String type, Map<String, String> p) {
        switch (type.toUpperCase()) {
            case "TABLET" -> {
                Tablet t = (Tablet) m;
                if (p.containsKey("dosage")) t.setDosage(p.get("dosage"));
                if (p.containsKey("tabletCount") && !p.get("tabletCount").isBlank())
                    t.setTabletCount(Integer.parseInt(p.get("tabletCount")));
                if (p.containsKey("coatingType")) t.setCoatingType(p.get("coatingType"));
            }
            case "SYRUP" -> {
                Syrup s = (Syrup) m;
                if (p.containsKey("volumeMl") && !p.get("volumeMl").isBlank())
                    s.setVolumeMl(Integer.parseInt(p.get("volumeMl")));
                if (p.containsKey("flavor")) s.setFlavor(p.get("flavor"));
                if (p.containsKey("dosagePerMl")) s.setDosagePerMl(p.get("dosagePerMl"));
            }
            case "INJECTION" -> {
                Injection inj = (Injection) m;
                if (p.containsKey("concentration")) inj.setConcentration(p.get("concentration"));
                if (p.containsKey("injectionType")) inj.setInjectionType(p.get("injectionType"));
                if (p.containsKey("volumeMl") && !p.get("volumeMl").isBlank())
                    inj.setVolumeMl(Double.parseDouble(p.get("volumeMl")));
            }
        }
    }
}
