package org.elkased.eventticketplatform.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ticket_validations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketValidation {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketValidationStatusEnum status;

    @Column(name = "validation_date", nullable = false)
    private LocalDateTime validationDate;

    @Column(name = "validation_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketValidationMethod validationMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TicketValidation that = (TicketValidation) o;
        return Objects.equals(id, that.id) && status == that.status && Objects.equals(validationDate, that.validationDate) && validationMethod == that.validationMethod && Objects.equals(createdAt, that.createdAt) && Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(validationDate);
        result = 31 * result + Objects.hashCode(validationMethod);
        result = 31 * result + Objects.hashCode(createdAt);
        result = 31 * result + Objects.hashCode(updated);
        return result;
    }
}
