
package com.bookurmedical.repository;

import com.bookurmedical.entity.MedicalCaseSheet;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MedicalCaseSheetRepository extends MongoRepository<MedicalCaseSheet, String> {
    Optional<MedicalCaseSheet> findByUserId(String userId);
}
