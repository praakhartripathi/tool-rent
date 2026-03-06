package in.toolrent.booking.dto;

import in.toolrent.booking.entity.Booking;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Booking — avoids direct entity serialisation,
 * exposes only the fields the client needs.
 */
@Data
@Builder
public class BookingResponse {

    private UUID                    id;
    private UUID                    toolId;
    private String                  toolName;
    private UUID                    customerId;
    private String                  customerEmail;
    private LocalDate               startDate;
    private LocalDate               endDate;
    private Integer                 totalDays;
    private BigDecimal              rentalAmount;
    private BigDecimal              depositAmount;
    private BigDecimal              totalAmount;
    private Booking.BookingStatus   status;
    private String                  notes;
    private LocalDateTime           pickupTime;
    private LocalDateTime           returnTime;
    private String                  bookingSource;
    private LocalDateTime           createdAt;
    private LocalDateTime           confirmedAt;
    private LocalDateTime           completedAt;
    private LocalDateTime           cancelledAt;

    /** Factory method — converts Booking entity → BookingResponse */
    public static BookingResponse from(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .toolId(b.getTool() != null ? b.getTool().getId() : null)
                .toolName(b.getTool() != null ? b.getTool().getName() : null)
                .customerId(b.getCustomer() != null ? b.getCustomer().getId() : null)
                .customerEmail(b.getCustomer() != null ? b.getCustomer().getEmail() : null)
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .totalDays(b.getTotalDays())
                .rentalAmount(b.getRentalAmount())
                .depositAmount(b.getDepositAmount())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus())
                .notes(b.getNotes())
                .pickupTime(b.getPickupTime())
                .returnTime(b.getReturnTime())
                .bookingSource(b.getBookingSource())
                .createdAt(b.getCreatedAt())
                .confirmedAt(b.getConfirmedAt())
                .completedAt(b.getCompletedAt())
                .cancelledAt(b.getCancelledAt())
                .build();
    }
}
