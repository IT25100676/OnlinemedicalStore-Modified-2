package com.medistore.entity;

import com.medistore.entity.enums.MedicineType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("TABLET")
@Getter @Setter @NoArgsConstructor
public class Tablet extends Medicine {

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "tablet_count")
    private Integer tabletCount;

    @Column(name = "coating_type")
    private String coatingType;

    @Override
    public MedicineType getMedicineType() {
        return MedicineType.TABLET;
    }

    @Override
    public String getDisplayFormat() {
        String count = tabletCount != null ? tabletCount + " tablets" : "Tablet";
        return dosage != null && !dosage.isBlank()
                ? count + " - " + dosage
                : count;
    }
}
