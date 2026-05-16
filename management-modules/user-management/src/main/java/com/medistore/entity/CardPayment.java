package com.medistore.entity;

import com.medistore.entity.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("CARD")
@Getter @Setter @NoArgsConstructor
public class CardPayment extends Payment {

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "card_type")
    private String cardType;

    @Column(name = "expiry_month")
    private int expiryMonth;

    @Column(name = "expiry_year")
    private int expiryYear;

    @Override
    public String getPaymentMethod() {
        return "Card";
    }

    @Override
    public boolean processPayment() {
        setStatus(PaymentStatus.SUCCESS);
        return true;
    }
}
