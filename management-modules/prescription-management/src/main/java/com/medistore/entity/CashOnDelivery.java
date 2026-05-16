package com.medistore.entity;

import com.medistore.entity.enums.PaymentStatus;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("COD")
@Getter @Setter @NoArgsConstructor
public class CashOnDelivery extends Payment {

    @Override
    public String getPaymentMethod() {
        return "Cash on Delivery";
    }

    @Override
    public boolean processPayment() {
        setStatus(PaymentStatus.PENDING);
        return true;
    }
}
