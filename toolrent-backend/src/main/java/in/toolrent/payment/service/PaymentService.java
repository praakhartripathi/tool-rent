package in.toolrent.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.toolrent.booking.entity.Booking;
import in.toolrent.booking.repository.BookingRepository;
import in.toolrent.payment.dto.PaymentResponse;
import in.toolrent.payment.entity.Payment;
import in.toolrent.payment.repository.PaymentRepository;
import in.toolrent.tenant.context.TenantContext;
import in.toolrent.tenant.entity.Tenant;
import in.toolrent.tenant.repository.TenantRepository;
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
    private final TenantRepository  tenantRepository;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    private Tenant currentTenant() {
        return tenantRepository.findBySubdomain(TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    /**
     * Creates a Razorpay order for a booking and persists a Payment record.
     */
    @Transactional
    public Map<String, Object> createOrder(UUID bookingId) {
        Tenant  tenant  = currentTenant();
        Booking booking = bookingRepository.findByIdAndTenant(bookingId, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        long amountInPaise = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderReq = new JSONObject();
            orderReq.put("amount",   amountInPaise);
            orderReq.put("currency", "INR");
            orderReq.put("receipt",  "booking_" + booking.getId().toString().substring(0, 8));

            Order  order          = razorpay.orders.create(orderReq);
            String razorpayOrderId = order.get("id");

            paymentRepository.save(Payment.builder()
                    .booking(booking)
                    .razorpayOrderId(razorpayOrderId)
                    .rentalAmount(booking.getRentalAmount())
                    .depositAmount(booking.getDepositAmount())
                    .totalAmount(booking.getTotalAmount())
                    .currency("INR")
                    .gatewayFee(BigDecimal.ZERO)
                    .platformFee(BigDecimal.ZERO)
                    .status(Payment.PaymentStatus.CREATED)
                    .build());

            return Map.of(
                    "orderId",   razorpayOrderId,
                    "amount",    amountInPaise,
                    "currency",  "INR",
                    "keyId",     razorpayKeyId,
                    "bookingId", bookingId.toString()
            );
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment gateway error: " + e.getMessage());
        }
    }

    /**
     * Verifies Razorpay payment signature and confirms the booking.
     */
    @Transactional
    public PaymentResponse verifyPayment(String orderId, String paymentId, String signature) {
        String expected = hmacSha256(orderId + "|" + paymentId, razorpayKeySecret);
        if (!expected.equals(signature)) {
            throw new SecurityException("Payment signature verification failed");
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

        payment.setRazorpayPaymentId(paymentId);
        payment.setRazorpaySignature(signature);
        payment.setStatus(Payment.PaymentStatus.CAPTURED);
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        log.info("Payment captured and booking confirmed: {}", booking.getId());
        return PaymentResponse.from(payment);
    }

    /**
     * Handles Razorpay async webhook events.
     */
    @Transactional
    public void handleWebhook(String event, String razorpayOrderId, String failureReason) {
        log.info("Webhook: event={}, orderId={}", event, razorpayOrderId);
        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            switch (event) {
                case "payment.captured" -> payment.setStatus(Payment.PaymentStatus.CAPTURED);
                case "payment.failed"   -> { payment.setStatus(Payment.PaymentStatus.FAILED); payment.setFailureReason(failureReason); }
                case "refund.created"   -> payment.setStatus(Payment.PaymentStatus.REFUNDED);
                default                 -> log.warn("Unhandled webhook event: {}", event);
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
