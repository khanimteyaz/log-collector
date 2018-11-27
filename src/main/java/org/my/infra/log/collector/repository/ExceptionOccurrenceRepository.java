package org.my.infra.log.collector.repository;

import org.my.infra.log.collector.entity.ExceptionOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExceptionOccurrenceRepository extends JpaRepository<ExceptionOccurrence,Long> {
}
