package com.example.OnlineMedicalStore.entity;

import com.example.OnlineMedicalStore.entity.enums.MedicineType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("SYRUP")
@Getter @Setter @NoArgsConstructor
public class Syrup extends Medicine {

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "flavor")
    private String flavor;

    @Column(name = "dosage_per_ml")
    private String dosagePerMl;

    @Override
    public MedicineType getMedicineType() {
        return MedicineType.SYRUP;
    }

    @Override
    public String getDisplayFormat() {
        String volume = volumeMl != null ? volumeMl + " ml" : "Syrup";
        return dosagePerMl != null && !dosagePerMl.isBlank()
                ? volume + " - " + dosagePerMl
                : volume;
    }
}
