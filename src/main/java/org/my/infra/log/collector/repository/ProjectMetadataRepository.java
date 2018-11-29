package org.my.infra.log.collector.repository;

import org.my.infra.log.collector.entity.ProjectMetadata;
import org.my.infra.log.collector.entity.UniqueException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMetadataRepository extends JpaRepository<ProjectMetadata,Long> {
    ProjectMetadata findBySource(String source);
}
