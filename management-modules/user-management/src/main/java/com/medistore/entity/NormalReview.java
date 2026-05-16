package com.medistore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("NORMAL")
@Getter @Setter @NoArgsConstructor
public class NormalReview extends Review {

    @Column(name = "guest_name")
    private String guestName;

    @Override
    public String getReviewType() {
        return "Normal";
    }

    @Override
    public String getDisplayBadge() {
        return "Customer Review";
    }
}
