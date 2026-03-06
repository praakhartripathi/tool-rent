package in.toolrent.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** GET /api/bookings — admin sees all; customer sees own */
    @GetMapping
    public ResponseEntity<List<Booking>> getBookings(
            @RequestParam(defaultValue = "false") boolean myBookings) {
        List<Booking> bookings = myBookings
                ? bookingService.getMyBookings()
                : bookingService.getAllBookingsForAdmin();
        return ResponseEntity.ok(bookings);
    }

    /** POST /api/bookings — authenticated customer creates booking */
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    /** PUT /api/bookings/{id}/confirm */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Booking> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.updateStatus(id, "confirm"));
    }

    /** PUT /api/bookings/{id}/cancel */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.updateStatus(id, "cancel"));
    }

    /** PUT /api/bookings/{id}/complete */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Booking> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.updateStatus(id, "complete"));
    }
}
