package org.my.infra.log.collector.entity;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "unique_exceptions")
public class UniqueException {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;

    @Column(name="exception_hash",unique = true)
    private String exceptionHash;

    @Column(name = "normalize_exception",length = 10000)
    private String normalizeException;

    @OneToMany(mappedBy = "uniqueException",cascade = CascadeType.ALL)
    private List<CanonicalException> exceptionVersions= new ArrayList<>();

    public UniqueException() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getExceptionHash() {
        return exceptionHash;
    }

    public void setExceptionHash(String exceptionHash) {
        this.exceptionHash = exceptionHash;
    }

    public String getNormalizeException() {
        return normalizeException;
    }

    public void setNormalizeException(String normalizeException) {
        this.normalizeException = normalizeException;
    }

    public List<CanonicalException> getExceptionVersions() {
        return exceptionVersions;
    }

    public void setExceptionVersions(List<CanonicalException> exceptionVersions) {
        this.exceptionVersions = exceptionVersions;
    }

    public void addExceptionVersion(CanonicalException canonicalException) {
        this.exceptionVersions.add(canonicalException);
    }

    public CanonicalException getCanonicalException(String hash) {
       return  this.exceptionVersions.stream()
            .filter(ce->ce.getExceptionSubVersionHash().equals(hash))
            .findFirst().get();
    }
    public boolean isCanonicalExceptionAlreadyExists(String hash) {
        return this.exceptionVersions.stream().anyMatch(ce-> ce.getExceptionSubVersionHash().
            equals(hash));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UniqueException that = (UniqueException) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UniqueException{");
        sb.append("id=").append(id);
        sb.append(", exceptionHash='").append(exceptionHash).append('\'');
        sb.append(", normalizeException='").append(normalizeException).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
