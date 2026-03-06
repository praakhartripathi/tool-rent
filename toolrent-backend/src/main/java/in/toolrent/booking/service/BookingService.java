package in.toolrent.booking.service;

import in.toolrent.auth.entity.User;
import in.toolrent.auth.repository.UserRepository;
import in.toolrent.booking.dto.BookingRequest;
import in.toolrent.booking.dto.BookingResponse;
import in.toolrent.booking.entity.Booking;
import in.toolrent.booking.repository.BookingRepository;
import in.toolrent.tenant.context.TenantContext;
import in.toolrent.tenant.entity.Tenant;
import in.toolrent.tenant.repository.TenantRepository;
import in.toolrent.tool.entity.Tool;
import in.toolrent.tool.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ToolRepository    toolRepository;
    private final UserRepository    userRepository;
    private final TenantRepository  tenantRepository;

    private Tenant currentTenant() {
        return tenantRepository.findBySubdomain(TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findById((UUID) auth.getPrincipal())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<BookingResponse> getAllBookingsForAdmin() {
        return bookingRepository.findByTenantOrderByCreatedAtDesc(currentTenant())
                .stream().map(BookingResponse::from).collect(Collectors.toList());
    }

    public List<BookingResponse> getMyBookings() {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(currentUser().getId())
                .stream().map(BookingResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Tenant tenant   = currentTenant();
        User   customer = currentUser();

        Tool tool = toolRepository.findByIdAndTenant(request.getToolId(), tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        if (!tool.isAvailable()) {
            throw new IllegalStateException("Tool is not available for rental");
        }

        long overlapping = bookingRepository.countOverlappingBookings(
                tool, request.getStartDate(), request.getEndDate());
        if (overlapping >= tool.getQuantity()) {
            throw new IllegalStateException("Tool is not available for the selected dates");
        }

        long       totalDays     = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        BigDecimal rentalAmount  = tool.getPricePerDay().multiply(BigDecimal.valueOf(totalDays));
        BigDecimal depositAmount = tool.getDepositAmount();
        BigDecimal totalAmount   = rentalAmount.add(depositAmount);

        Booking booking = bookingRepository.save(Booking.builder()
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
                .pickupTime(request.getPickupTime())
                .returnTime(request.getReturnTime())
                .bookingSource(request.getBookingSource() == null || request.getBookingSource().isBlank()
                        ? "web"
                        : request.getBookingSource().trim().toLowerCase())
                .status(Booking.BookingStatus.PENDING)
                .build());

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse updateStatus(UUID bookingId, String action) {
        Booking booking = bookingRepository.findByIdAndTenant(bookingId, currentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        switch (action.toLowerCase()) {
            case "confirm"  -> { booking.setStatus(Booking.BookingStatus.CONFIRMED);  booking.setConfirmedAt(LocalDateTime.now()); }
            case "complete" -> { booking.setStatus(Booking.BookingStatus.COMPLETED);  booking.setCompletedAt(LocalDateTime.now()); }
            case "cancel"   -> { booking.setStatus(Booking.BookingStatus.CANCELLED);  booking.setCancelledAt(LocalDateTime.now()); }
            default         -> throw new IllegalArgumentException("Unknown action: " + action);
        }

        return BookingResponse.from(bookingRepository.save(booking));
    }
}
