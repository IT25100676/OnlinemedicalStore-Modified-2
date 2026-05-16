package com.medistore.repository;

import com.medistore.entity.Review;
import com.medistore.entity.Medicine;
import com.medistore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMedicineAndApprovedTrue(Medicine medicine);

    List<Review> findByUser(User user);

    List<Review> findByUserAndMedicine(User user, Medicine medicine);

    List<Review> findByApprovedFalse();

    List<Review> findAllByOrderByReviewDateDesc();

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.medicine = :medicine and r.approved = true")
    Double findAverageRatingByMedicine(@Param("medicine") Medicine medicine);
}
