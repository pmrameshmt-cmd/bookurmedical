package com.bookurmedical.repository;

import com.bookurmedical.entity.Slot;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SlotRepository extends MongoRepository<Slot, String> {
    List<Slot> findByDoctorId(String doctorId);
    List<Slot> findByCaseId(String caseId);
    List<Slot> findByPatientId(String patientId);
}
