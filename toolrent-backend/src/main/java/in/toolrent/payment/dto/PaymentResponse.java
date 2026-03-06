package in.toolrent.payment.dto;

import in.toolrent.payment.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Response DTO for Payment — safe to serialise */
@Data
@Builder
public class PaymentResponse {

    private UUID                   id;
    private UUID                   bookingId;
    private String                 razorpayOrderId;
    private String                 razorpayPaymentId;
    private BigDecimal             rentalAmount;
    private BigDecimal             depositAmount;
    private BigDecimal             totalAmount;
    private String                 currency;
    private BigDecimal             gatewayFee;
    private BigDecimal             platformFee;
    private Payment.PaymentStatus  status;
    private LocalDateTime          createdAt;

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .bookingId(p.getBooking() != null ? p.getBooking().getId() : null)
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .rentalAmount(p.getRentalAmount())
                .depositAmount(p.getDepositAmount())
                .totalAmount(p.getTotalAmount())
                .currency(p.getCurrency())
                .gatewayFee(p.getGatewayFee())
                .platformFee(p.getPlatformFee())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
