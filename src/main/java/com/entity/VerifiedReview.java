package com.example.OnlineMedicalStore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("VERIFIED")
@Getter
@Setter 
@NoArgsConstructor
public class VerifiedReview extends Review {

    @Column(name = "order_id_ref")
    private Long orderIdRef;

    @Override
    public String getReviewType() {
        return "Verified";
    }

    @Override
    public String getDisplayBadge() {
        return "Verified Purchase";
    }
}
