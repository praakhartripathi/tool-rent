package in.toolrent.payment;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.toolrent.booking.Booking;
import in.toolrent.booking.BookingRepository;
import in.toolrent.tenant.Tenant;
import in.toolrent.tenant.TenantContext;
import in.toolrent.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TenantRepository tenantRepository;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    private Tenant currentTenant() {
        return tenantRepository.findBySubdomain(TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    /**
     * POST /payments/create-order
     * Creates a Razorpay order for a booking and stores the payment record.
     */
    @Transactional
    public Map<String, Object> createOrder(UUID bookingId) {
        Tenant tenant = currentTenant();
        Booking booking = bookingRepository.findByIdAndTenant(bookingId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        long amountInPaise = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + booking.getId().toString().substring(0, 8));

            Order order = razorpay.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            // Store payment record
            Payment payment = Payment.builder()
                    .booking(booking)
                    .razorpayOrderId(razorpayOrderId)
                    .rentalAmount(booking.getRentalAmount())
                    .depositAmount(booking.getDepositAmount())
                    .totalAmount(booking.getTotalAmount())
                    .currency("INR")
                    .status(Payment.PaymentStatus.CREATED)
                    .build();
            paymentRepository.save(payment);

            return Map.of(
                    "orderId", razorpayOrderId,
                    "amount", amountInPaise,
                    "currency", "INR",
                    "keyId", razorpayKeyId,
                    "bookingId", bookingId
            );
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new RuntimeException("Payment gateway error: " + e.getMessage());
        }
    }

    /**
     * POST /payments/verify
     * Verifies razorpay_signature after successful payment.
     */
    @Transactional
    public Map<String, String> verifyPayment(String razorpayOrderId,
                                              String razorpayPaymentId,
                                              String razorpaySignature) {
        String expectedSignature = hmacSha256(razorpayOrderId + "|" + razorpayPaymentId, razorpayKeySecret);

        if (!expectedSignature.equals(razorpaySignature)) {
            throw new SecurityException("Payment signature verification failed");
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found"));

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(Payment.PaymentStatus.CAPTURED);
        paymentRepository.save(payment);

        // Auto-confirm booking after payment
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        log.info("Payment verified and booking confirmed: {}", booking.getId());
        return Map.of("status", "success", "bookingId", booking.getId().toString());
    }

    /**
     * POST /payments/webhook
     * Handles Razorpay payment events asynchronously.
     */
    @Transactional
    public void handleWebhook(String event, String razorpayOrderId, String failureReason) {
        log.info("Webhook received: event={}, orderId={}", event, razorpayOrderId);

        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            switch (event) {
                case "payment.captured" -> payment.setStatus(Payment.PaymentStatus.CAPTURED);
                case "payment.failed" -> {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    payment.setFailureReason(failureReason);
                }
                case "refund.created" -> payment.setStatus(Payment.PaymentStatus.REFUNDED);
                default -> log.warn("Unhandled webhook event: {}", event);
            }
            paymentRepository.save(payment);
        });
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}
