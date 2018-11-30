package org.my.infra.log.collector.entity;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="jira_issues")
public class JiraIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;

    @Column(name="jira_id")
    private String jiraId;

    @Column(name="source",length = 50)
    private String source;

    @Column(name="app",length = 50)
    private String app;

    @Column(name="status",length = 30)
    private String status;

    @Column(name="start_at")
    private Timestamp startAt;

    @OneToOne
    private CanonicalException canonicalException;

    public JiraIssue() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJiraId() {
        return jiraId;
    }

    public void setJiraId(String jiraId) {
        this.jiraId = jiraId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Timestamp getStartAt() {
        return startAt;
    }

    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public CanonicalException getCanonicalException() {
        return canonicalException;
    }

    public void setCanonicalException(CanonicalException canonicalException) {
        this.canonicalException = canonicalException;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JiraIssue jiraIssue = (JiraIssue) o;
        return id == jiraIssue.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JiraIssue{");
        sb.append("id=").append(id);
        sb.append(", jiraId='").append(jiraId).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", app='").append(app).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", startAt=").append(startAt);
        sb.append(", canonicalException=").append(canonicalException);
        sb.append('}');
        return sb.toString();
    }
}
