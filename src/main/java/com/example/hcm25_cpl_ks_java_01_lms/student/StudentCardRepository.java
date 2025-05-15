package com.example.hcm25_cpl_ks_java_01_lms.student;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentCardRepository extends JpaRepository<StudentCard, Long> {
    Optional<StudentCard> findByStudent_Id(Long userId);

    boolean existsByCardNumber(String cardNumber);

    List<StudentCard> findByStudentIdIn(List<Long> userIds);

}