package com.medistore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("CUSTOMER")
@Getter @Setter @NoArgsConstructor
public class Customer extends User {

    @Column(name = "loyalty_points")
    private int loyaltyPoints = 0;

    @Column(name = "preferred_payment")
    private String preferredPayment;

    @Override
    public String getDisplayRole() {
        return "Customer";
    }
}
