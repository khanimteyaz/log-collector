package org.my.infra.log.collector.repository;

import org.my.infra.log.collector.entity.ProjectMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMetadataRepository extends JpaRepository<ProjectMetadata,Long> {
    ProjectMetadata findBySource(String source);
}
