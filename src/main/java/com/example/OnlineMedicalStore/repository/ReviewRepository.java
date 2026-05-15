package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Review;
import com.example.OnlineMedicalStore.entity.Medicine;
import com.example.OnlineMedicalStore.entity.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Repository
public class ReviewRepository extends AbstractTextFileRepository<Review> {

    public ReviewRepository() {
        super("reviews.txt");
    }

    @Override
    public Review save(Review review) {
        if (review.getReviewDate() == null) {
            review.setReviewDate(LocalDateTime.now());
        }
        return super.save(review);
    }

    public List<Review> findByMedicineAndApprovedTrue(Medicine medicine) {
        Long medicineId = medicine != null ? medicine.getId() : null;
        return findAllByOrderByReviewDateDesc().stream()
                .filter(review -> review.getMedicine() != null && medicineId != null
                        && medicineId.equals(review.getMedicine().getId())
                        && review.isApproved())
                .toList();
    }

    public List<Review> findByUser(User user) {
        Long userId = user != null ? user.getId() : null;
        return findAllByOrderByReviewDateDesc().stream()
                .filter(review -> review.getUser() != null && userId != null
                        && userId.equals(review.getUser().getId()))
                .toList();
    }

    public List<Review> findByUserAndMedicine(User user, Medicine medicine) {
        Long userId = user != null ? user.getId() : null;
        Long medicineId = medicine != null ? medicine.getId() : null;
        return findAllByOrderByReviewDateDesc().stream()
                .filter(review -> review.getUser() != null && review.getMedicine() != null
                        && userId != null && medicineId != null
                        && userId.equals(review.getUser().getId())
                        && medicineId.equals(review.getMedicine().getId()))
                .toList();
    }

    public List<Review> findByApprovedFalse() {
        return findAllByOrderByReviewDateDesc().stream()
                .filter(review -> !review.isApproved())
                .toList();
    }

    public List<Review> findAllByOrderByReviewDateDesc() {
        return findAll().stream()
                .sorted(Comparator.comparing(Review::getReviewDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Double findAverageRatingByMedicine(Medicine medicine) {
        List<Review> reviews = findByMedicineAndApprovedTrue(medicine);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
