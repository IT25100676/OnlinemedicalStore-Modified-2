package com.example.OnlineMedicalStore.entity.enums;

/**
 * Possible states of a Prescription in the review workflow.
 *
 * Workflow:  PENDING  →  APPROVED
 *            PENDING  →  REJECTED
 *
 * OOP: Used with Polymorphism — PrescriptionService.review() applies
 *      different validation rules depending on the target status.
 */
public enum PrescriptionStatus {
    PENDING,
    APPROVED,
    REJECTED
}
