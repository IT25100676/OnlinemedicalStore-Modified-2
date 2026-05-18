package com.example.OnlineMedicalStore.service;

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.repository.MedicineRepository;
import com.example.OnlineMedicalStore.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MedicineRepository medicineRepository;

    public List<Review> findByMedicine(Medicine medicine) {
        return reviewRepository.findByMedicineAndApprovedTrue(medicine);
    }

    public List<Review> findByUser(User user) {
        return reviewRepository.findByUser(user); }

    public List<Review> findByUserAndMedicine(User user, Medicine medicine) {
        return reviewRepository.findByUserAndMedicine(user, medicine);
    }

    public List<Review> findPendingModeration() {
        return reviewRepository.findByApprovedFalse(); }

    public List<Review> findAll() {
        return reviewRepository.findAllByOrderByReviewDateDesc(); }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id); }

    public Double getAverageRating(Medicine medicine) {
        return reviewRepository.findAverageRatingByMedicine(medicine);
    }

    @Transactional
    public Review addReview(User user, Long medicineId, int rating, String comment, boolean isVerified, Long orderIdRef) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        Review review;
        if (isVerified) {
            VerifiedReview vr = new VerifiedReview();
            vr.setOrderIdRef(orderIdRef);
            review = vr;
        } else {
            NormalReview nr = new NormalReview();
            if (user instanceof Customer) nr.setGuestName(user.getUsername());
            review = nr;
        }
        review.setUser(user);
        review.setMedicine(medicine);
        review.setRating(Math.max(1, Math.min(5, rating)));
        review.setComment(comment);
        review.setApproved(false);
        return reviewRepository.save(review);
    }

    @Transactional
    public void approve(Long id) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        r.setApproved(true);
        reviewRepository.save(r);
    }

    @Transactional
    public void delete(Long id) {
        reviewRepository.deleteById(id); }

    @Transactional
    public Review update(Long id, int rating, String comment) {
        Review r = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        r.setRating(Math.max(1, Math.min(5, rating)));
        r.setComment(comment);
        r.setReviewDate(LocalDateTime.now());
        r.setApproved(false); 
        return reviewRepository.save(r);
    }

    @Transactional
    public Review updateByUser(Long id, User user, int rating, String comment) {
        if (user == null) {
            throw new RuntimeException("Please login before editing a review.");
        }
        Review r = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        if (r.getUser() == null || !user.getId().equals(r.getUser().getId())) {
            throw new RuntimeException("You can only edit your own reviews.");
        }
        return update(id, rating, comment);
    }
}
