package com.medistore.entity;

import com.medistore.entity.enums.MedicineType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("INJECTION")
@Getter @Setter @NoArgsConstructor
public class Injection extends Medicine {

    @Column(name = "concentration")
    private String concentration;

    @Column(name = "injection_type")
    private String injectionType;

    @Column(name = "volume_ml")
    private Double volumeMl;

    @Override
    public MedicineType getMedicineType() {
        return MedicineType.INJECTION;
    }

    @Override
    public String getDisplayFormat() {
        String volume = volumeMl != null ? volumeMl + " ml" : "Injection";
        return concentration != null && !concentration.isBlank()
                ? volume + " - " + concentration
                : volume;
    }
}
