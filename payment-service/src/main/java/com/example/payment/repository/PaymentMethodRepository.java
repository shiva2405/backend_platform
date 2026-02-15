package com.example.payment.repository;

import com.example.payment.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByUserId(Long userId);
    
    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(Long userId);
    
    @Modifying
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.userId = :userId")
    void clearDefaultForUser(Long userId);
    
    boolean existsByUserIdAndType(Long userId, PaymentMethod.PaymentType type);
}
