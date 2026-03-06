package in.toolrent.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Incoming request body for POST /payments/verify */
@Data
public class PaymentVerifyRequest {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;
}
