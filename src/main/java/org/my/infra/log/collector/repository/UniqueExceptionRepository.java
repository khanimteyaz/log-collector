package org.my.infra.log.collector.repository;

import org.my.infra.log.collector.model.UniqueException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniqueExceptionRepository extends JpaRepository<UniqueException,Long> {
}
