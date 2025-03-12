package tythor.herakia.superentity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class SuperEntityIdentity extends SuperEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    protected Long id;
}
