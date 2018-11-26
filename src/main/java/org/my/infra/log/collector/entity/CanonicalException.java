package org.my.infra.log.collector.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="exception_versions")
public class CanonicalException {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;

    @Column(name="exception_sub_version_hash",unique = true)
    private String exceptionSubVersionHash;

    @Column(name="exception",length = 10000)
    private String exception;

    @OneToMany(mappedBy = "canonicalException",cascade = CascadeType.ALL)
    private List<JiraIssue> issues= new ArrayList<>();

    @OneToOne
    private UniqueException uniqueException;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getExceptionSubVersionHash() {
        return exceptionSubVersionHash;
    }

    public void setExceptionSubVersionHash(String exceptionSubVersionHash) {
        this.exceptionSubVersionHash = exceptionSubVersionHash;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public UniqueException getUniqueException() {
        return uniqueException;
    }

    public void setUniqueException(UniqueException uniqueException) {
        this.uniqueException = uniqueException;
    }

    public void addJiraIssue(JiraIssue jiraIssue) {
        this.issues.add(jiraIssue);
    }

    public Optional<JiraIssue> getOpenJiraIssue(String source, String app) {
        return this.issues.stream().filter(issue->
            issue.getSource().equals(source)
                && issue.getApp().equals(app)
                && issue.getStatus().equals("OPEN")
        ).findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CanonicalException that = (CanonicalException) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CanonicalException{");
        sb.append("id=").append(id);
        sb.append(", exceptionSubVersionHash='").append(exceptionSubVersionHash).append('\'');
        sb.append(", exception='").append(exception).append('\'');
        sb.append(", uniqueException=").append(uniqueException);
        sb.append('}');
        return sb.toString();
    }
}
