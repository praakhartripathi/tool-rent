package in.toolrent.booking.repository;

import in.toolrent.booking.entity.Booking;
import in.toolrent.tenant.entity.Tenant;
import in.toolrent.tool.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByTenantOrderByCreatedAtDesc(Tenant tenant);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Optional<Booking> findByIdAndTenant(UUID id, Tenant tenant);

    /**
     * Availability engine — count active bookings that OVERLAP the requested date range.
     * Overlaps if: existing.start <= requested.end AND existing.end >= requested.start
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.tool = :tool
          AND b.status IN ('PENDING', 'CONFIRMED')
          AND b.startDate <= :endDate
          AND b.endDate   >= :startDate
    """)
    long countOverlappingBookings(@Param("tool")      Tool      tool,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate")   LocalDate endDate);
}
