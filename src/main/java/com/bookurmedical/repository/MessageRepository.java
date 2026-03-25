package com.bookurmedical.repository;

import com.bookurmedical.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByCaseIdOrderByTimestampAsc(String caseId);
}
