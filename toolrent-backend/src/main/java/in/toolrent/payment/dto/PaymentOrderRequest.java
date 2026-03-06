package in.toolrent.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/** Incoming request body for POST /payments/create-order */
@Data
public class PaymentOrderRequest {

    @NotNull
    private UUID bookingId;
}
