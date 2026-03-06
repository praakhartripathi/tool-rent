package in.toolrent.booking;

import in.toolrent.auth.User;
import in.toolrent.auth.UserRepository;
import in.toolrent.tenant.Tenant;
import in.toolrent.tenant.TenantContext;
import in.toolrent.tenant.TenantRepository;
import in.toolrent.tool.Tool;
import in.toolrent.tool.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    private Tenant currentTenant() {
        String subdomain = TenantContext.getCurrentTenant();
        return tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = (UUID) auth.getPrincipal();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<Booking> getAllBookingsForAdmin() {
        return bookingRepository.findByTenantOrderByCreatedAtDesc(currentTenant());
    }

    public List<Booking> getMyBookings() {
        User user = currentUser();
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        Tenant tenant = currentTenant();
        User customer = currentUser();

        Tool tool = toolRepository.findByIdAndTenant(request.getToolId(), tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        if (!tool.isAvailable()) {
            throw new IllegalStateException("Tool is not available for rental");
        }

        // Availability check — prevent double booking
        long overlapping = bookingRepository.countOverlappingBookings(
                tool, request.getStartDate(), request.getEndDate());
        if (overlapping >= tool.getQuantity()) {
            throw new IllegalStateException("Tool is not available for the selected dates");
        }

        long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        BigDecimal rentalAmount = tool.getPricePerDay().multiply(BigDecimal.valueOf(totalDays));
        BigDecimal depositAmount = tool.getDepositAmount();
        BigDecimal totalAmount = rentalAmount.add(depositAmount);

        Booking booking = Booking.builder()
                .tenant(tenant)
                .tool(tool)
                .customer(customer)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays((int) totalDays)
                .rentalAmount(rentalAmount)
                .depositAmount(depositAmount)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .status(Booking.BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateStatus(UUID bookingId, String action) {
        Tenant tenant = currentTenant();
        Booking booking = bookingRepository.findByIdAndTenant(bookingId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        switch (action.toLowerCase()) {
            case "confirm" -> {
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                booking.setConfirmedAt(LocalDateTime.now());
            }
            case "complete" -> {
                booking.setStatus(Booking.BookingStatus.COMPLETED);
                booking.setCompletedAt(LocalDateTime.now());
            }
            case "cancel" -> {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                booking.setCancelledAt(LocalDateTime.now());
            }
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        }

        return bookingRepository.save(booking);
    }
}
