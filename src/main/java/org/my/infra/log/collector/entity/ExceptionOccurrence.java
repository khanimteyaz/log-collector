package org.my.infra.log.collector.entity;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="exception_occurrences")
public class ExceptionOccurrence {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;

    @Column(name="jira_id")
    private String jiraId;

    @Column(name="occurred_at")
    private Timestamp occurredAt;

    @Column(name="app")
    private String app;

    @Column(name="source")
    private String source;

    @Column(name="host")
    private String host;

    @Column(name="other_info",length = 1000)
    private String otherInfos;

    public ExceptionOccurrence() {

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

    public Timestamp getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Timestamp occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOtherInfos() {
        return otherInfos;
    }

    public void setOtherInfos(String otherInfos) {
        this.otherInfos = otherInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExceptionOccurrence that = (ExceptionOccurrence) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExceptionOccurrence{");
        sb.append("id=").append(id);
        sb.append(", jiraId='").append(jiraId).append('\'');
        sb.append(", occurredAt=").append(occurredAt);
        sb.append(", app='").append(app).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", otherInfos='").append(otherInfos).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
