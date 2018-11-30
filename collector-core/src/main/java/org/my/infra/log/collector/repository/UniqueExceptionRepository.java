package org.my.infra.log.collector.repository;

import org.my.infra.log.collector.entity.UniqueException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniqueExceptionRepository extends JpaRepository<UniqueException,Long> {
    UniqueException findByExceptionHash(String exceptionHash);
    boolean existsByExceptionHash(String exceptionHash);
}
