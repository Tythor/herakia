package tythor.herakia.superentity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@MappedSuperclass
public abstract class SuperEntity {
    public abstract Long getId();

    public abstract void setId(Long id);

    @CreationTimestamp
    @Column(name = "created_timestamp")
    protected Instant createdTimestamp;

    @UpdateTimestamp
    @Column(name = "updated_timestamp")
    protected Instant updatedTimestamp;

    @Version
    @Column(name = "version")
    private Long version;
}
