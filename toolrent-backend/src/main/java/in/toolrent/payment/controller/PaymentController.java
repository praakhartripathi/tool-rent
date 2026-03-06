package in.toolrent.payment.controller;

import in.toolrent.payment.dto.PaymentOrderRequest;
import in.toolrent.payment.dto.PaymentResponse;
import in.toolrent.payment.dto.PaymentVerifyRequest;
import in.toolrent.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/payments/create-order
     * Body: { "bookingId": "uuid" }
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @Valid @RequestBody PaymentOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(request.getBookingId()));
    }

    /**
     * POST /api/payments/verify
     * Body: { razorpayOrderId, razorpayPaymentId, razorpaySignature }
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verify(
            @Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        ));
    }

    /**
     * POST /api/payments/webhook — public (called by Razorpay)
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        try {
            String event = (String) payload.get("event");
            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>)
                    ((Map<String, Object>) ((Map<String, Object>) payload.get("payload")).get("payment")).get("entity");
            paymentService.handleWebhook(event, (String) entity.get("order_id"), (String) entity.get("error_description"));
        } catch (Exception e) {
            log.warn("Webhook parse error (ignored): {}", e.getMessage());
        }
        return ResponseEntity.ok().build(); // Always 200 to Razorpay
    }
}
