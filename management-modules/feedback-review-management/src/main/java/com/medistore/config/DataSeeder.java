package com.medistore.config;

import com.medistore.entity.Admin;
import com.medistore.entity.Injection;
import com.medistore.entity.Medicine;
import com.medistore.entity.Syrup;
import com.medistore.entity.Tablet;
import com.medistore.repository.AdminRepository;
import com.medistore.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MedicineRepository medicineRepository;
    private final AdminRepository adminRepository;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedMedicines();
    }

    private void seedAdmin() {
        if (adminRepository.existsByUsername("admin")) {
            return;
        }
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setEmail("admin@medistore.local");
        admin.setPassword("admin123");
        admin.setDepartment("Pharmacy");
        adminRepository.save(admin);
    }

    private void seedMedicines() {
        if (medicineRepository.count() > 0) {
            return;
        }

        Tablet panadol = new Tablet();
        applyCommon(panadol, "Panadol 500mg", "Pain Relief", "GlaxoSmithKline",
                "Paracetamol tablets for fever and mild pain relief.", "25.00", 120);
        panadol.setDosage("500mg");
        panadol.setTabletCount(10);
        panadol.setCoatingType("Film-coated");
        medicineRepository.save(panadol);

        Syrup coughRelief = new Syrup();
        applyCommon(coughRelief, "Cough Relief Syrup", "Cough & Cold", "MediCare",
                "Soothing syrup for dry cough symptoms.", "420.00", 45);
        coughRelief.setVolumeMl(100);
        coughRelief.setFlavor("Honey Lemon");
        coughRelief.setDosagePerMl("5ml per dose");
        medicineRepository.save(coughRelief);

        Tablet vitaminC = new Tablet();
        applyCommon(vitaminC, "Vitamin C 1000mg", "Vitamins", "HealthPlus",
                "Daily immune support supplement.", "850.00", 80);
        vitaminC.setDosage("1000mg");
        vitaminC.setTabletCount(30);
        vitaminC.setCoatingType("Chewable");
        medicineRepository.save(vitaminC);

        Injection insulin = new Injection();
        applyCommon(insulin, "Insulin Injection", "Diabetes", "NovoCare",
                "Prescription insulin injection for diabetes management.", "2400.00", 25);
        insulin.setRequiresPrescription(true);
        insulin.setConcentration("100 IU/ml");
        insulin.setInjectionType("Subcutaneous");
        insulin.setVolumeMl(10.0);
        medicineRepository.save(insulin);
    }

    private void applyCommon(Medicine medicine, String name, String category, String manufacturer,
                             String description, String price, int stock) {
        medicine.setName(name);
        medicine.setCategory(category);
        medicine.setManufacturer(manufacturer);
        medicine.setDescription(description);
        medicine.setPrice(new BigDecimal(price));
        medicine.setStock(stock);
        medicine.setExpiryDate(LocalDate.now().plusYears(2));
    }
}
