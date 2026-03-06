package in.toolrent.tool.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ToolRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal pricePerDay;

    private BigDecimal depositAmount;

    private Integer quantity;

    private String category;

    private String sku;

    private String barcode;

    @DecimalMin("0.0")
    private BigDecimal replacementCost;

    private LocalDate purchaseDate;
}
