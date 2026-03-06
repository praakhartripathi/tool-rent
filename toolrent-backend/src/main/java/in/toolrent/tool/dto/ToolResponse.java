package in.toolrent.tool.dto;

import in.toolrent.tool.entity.Tool;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for Tool — excludes the Tenant entity reference
 * to avoid circular JSON serialisation and over-exposure of tenant data.
 */
@Data
@Builder
public class ToolResponse {

    private UUID        id;
    private String      name;
    private String      description;
    private BigDecimal  pricePerDay;
    private BigDecimal  depositAmount;
    private Integer     quantity;
    private Integer     availableQuantity;
    private String      category;
    private boolean     isAvailable;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Factory method — converts Tool entity → ToolResponse */
    public static ToolResponse from(Tool tool) {
        return ToolResponse.builder()
                .id(tool.getId())
                .name(tool.getName())
                .description(tool.getDescription())
                .pricePerDay(tool.getPricePerDay())
                .depositAmount(tool.getDepositAmount())
                .quantity(tool.getQuantity())
                .availableQuantity(tool.getAvailableQuantity())
                .category(tool.getCategory())
                .isAvailable(tool.isAvailable())
                .imageUrls(tool.getImages() != null
                        ? tool.getImages().stream().map(i -> i.getUrl()).collect(Collectors.toList())
                        : List.of())
                .createdAt(tool.getCreatedAt())
                .updatedAt(tool.getUpdatedAt())
                .build();
    }
}
