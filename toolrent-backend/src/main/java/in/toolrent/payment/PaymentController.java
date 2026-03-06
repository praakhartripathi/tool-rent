package in.toolrent.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/payments/create-order
     * Body: { "bookingId": "uuid" }
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, String> body) {
        UUID bookingId = UUID.fromString(body.get("bookingId"));
        return ResponseEntity.ok(paymentService.createOrder(bookingId));
    }

    /**
     * POST /api/payments/verify
     * Body: { razorpay_order_id, razorpay_payment_id, razorpay_signature }
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(paymentService.verifyPayment(
                body.get("razorpay_order_id"),
                body.get("razorpay_payment_id"),
                body.get("razorpay_signature")
        ));
    }

    /**
     * POST /api/payments/webhook  (public — called by Razorpay)
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        try {
            String event = (String) payload.get("event");
            @SuppressWarnings("unchecked")
            Map<String, Object> paymentEntity = (Map<String, Object>)
                    ((Map<String, Object>) payload.get("payload")).get("payment");
            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>) paymentEntity.get("entity");
            String orderId = (String) entity.get("order_id");
            String failureReason = (String) entity.get("error_description");
            paymentService.handleWebhook(event, orderId, failureReason);
        } catch (Exception e) {
            // Swallow to always return 200 to Razorpay
        }
        return ResponseEntity.ok().build();
    }
}
