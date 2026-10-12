package com.placementhub.placementhub;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    List<Application> findByStudentEmailIgnoreCase(String studentEmail);

    boolean existsByStudentEmailIgnoreCaseAndJob_Id(
            String studentEmail,
            Long jobId
    );
}