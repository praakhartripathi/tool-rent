package in.toolrent.payment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Incoming request body for POST /payments/verify */
@Data
public class PaymentVerifyRequest {

    @NotBlank
    @JsonAlias("razorpay_order_id")
    private String razorpayOrderId;

    @NotBlank
    @JsonAlias("razorpay_payment_id")
    private String razorpayPaymentId;

    @NotBlank
    @JsonAlias("razorpay_signature")
    private String razorpaySignature;
}
