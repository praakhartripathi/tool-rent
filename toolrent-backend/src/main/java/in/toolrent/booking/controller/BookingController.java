package in.toolrent.booking.controller;

import in.toolrent.booking.dto.BookingRequest;
import in.toolrent.booking.dto.BookingResponse;
import in.toolrent.booking.service.BookingService;
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

    /** GET /api/bookings — admin or customer (my bookings) */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getBookings(
            @RequestParam(defaultValue = "false") boolean myBookings) {
        return ResponseEntity.ok(
                myBookings ? bookingService.getMyBookings() : bookingService.getAllBookingsForAdmin());
    }

    /** POST /api/bookings — authenticated customer creates booking */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    /** PUT /api/bookings/{id}/confirm — admin only */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<BookingResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.updateStatus(id, "confirm"));
    }

    /** PUT /api/bookings/{id}/cancel — authenticated users */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.updateStatus(id, "cancel"));
    }

    /** PUT /api/bookings/{id}/complete — admin only */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<BookingResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.updateStatus(id, "complete"));
    }
}
