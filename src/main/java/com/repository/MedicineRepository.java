package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByNameContainingIgnoreCase(String name);

    List<Medicine> findByCategoryIgnoreCase(String category);

    List<Medicine> findByExpiryDateBefore(LocalDate date);

    List<Medicine> findByStockLessThan(int threshold);

    @Query("select distinct m.category from Medicine m where m.category is not null and m.category <> '' order by m.category")
    List<String> findAllCategories();

    List<Medicine> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String cat);

    @Query("select m from Medicine m where (m.expiryDate is not null and m.expiryDate < :today) or (m.stock is not null and m.stock = 0)")
    List<Medicine> findExpiredOrOutOfStock(@Param("today") LocalDate today);
}
