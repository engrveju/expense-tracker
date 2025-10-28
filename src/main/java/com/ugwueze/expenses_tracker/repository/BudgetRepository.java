package com.ugwueze.expenses_tracker.repository;


import com.ugwueze.expenses_tracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long userId, String category, LocalDate date1, LocalDate date2);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND :date BETWEEN b.startDate AND b.endDate")
    List<Budget> findActiveBudgetsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    boolean existsByUserIdAndCategoryAndStartDateAndEndDate(
            Long userId, String category, LocalDate startDate, LocalDate endDate);
}
