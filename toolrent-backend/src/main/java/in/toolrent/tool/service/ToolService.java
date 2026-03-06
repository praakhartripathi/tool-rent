package in.toolrent.tool.service;

import in.toolrent.tenant.context.TenantContext;
import in.toolrent.tenant.entity.Tenant;
import in.toolrent.tenant.repository.TenantRepository;
import in.toolrent.tool.dto.ToolRequest;
import in.toolrent.tool.dto.ToolResponse;
import in.toolrent.tool.entity.Tool;
import in.toolrent.booking.repository.BookingRepository;
import in.toolrent.tool.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository   toolRepository;
    private final TenantRepository tenantRepository;
    private final BookingRepository bookingRepository;

    private Tenant currentTenant() {
        String subdomain = TenantContext.getCurrentTenant();
        return tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + subdomain));
    }

    public List<ToolResponse> getAllTools() {
        return toolRepository.findByTenant(currentTenant())
                .stream().map(ToolResponse::from).collect(Collectors.toList());
    }

    public List<ToolResponse> getAvailableTools() {
        return toolRepository.findByTenantAndIsAvailableTrue(currentTenant())
                .stream().map(ToolResponse::from).collect(Collectors.toList());
    }

    public ToolResponse getToolById(UUID id) {
        return ToolResponse.from(findToolOrThrow(id));
    }

    @Transactional
    public ToolResponse createTool(ToolRequest request) {
        Tenant tenant = currentTenant();
        Tool tool = toolRepository.save(Tool.builder()
                .tenant(tenant)
                .name(request.getName())
                .description(request.getDescription())
                .pricePerDay(request.getPricePerDay())
                .depositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .availableQuantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .category(request.getCategory())
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .replacementCost(request.getReplacementCost())
                .purchaseDate(request.getPurchaseDate())
                .build());
        return ToolResponse.from(tool);
    }

    @Transactional
    public ToolResponse updateTool(UUID id, ToolRequest request) {
        Tool tool = findToolOrThrow(id);
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setPricePerDay(request.getPricePerDay());
        tool.setDepositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO);
        if (request.getQuantity() != null) tool.setQuantity(request.getQuantity());
        tool.setCategory(request.getCategory());
        tool.setSku(request.getSku());
        tool.setBarcode(request.getBarcode());
        tool.setReplacementCost(request.getReplacementCost());
        tool.setPurchaseDate(request.getPurchaseDate());
        return ToolResponse.from(toolRepository.save(tool));
    }

    public Map<String, Object> checkAvailability(UUID id, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }
        Tool tool = findToolOrThrow(id);
        long overlapping = bookingRepository.countOverlappingBookings(tool, startDate, endDate);
        int quantity = tool.getQuantity() == null ? 0 : tool.getQuantity();
        int remaining = Math.max(0, quantity - (int) overlapping);
        return Map.of(
                "toolId", tool.getId(),
                "startDate", startDate,
                "endDate", endDate,
                "available", remaining > 0 && tool.isAvailable(),
                "remainingQuantity", remaining,
                "totalQuantity", quantity
        );
    }

    @Transactional
    public void deleteTool(UUID id) {
        toolRepository.delete(findToolOrThrow(id));
    }

    // Internal helper — returns the entity (used by booking service)
    public Tool findToolEntity(UUID id) {
        return findToolOrThrow(id);
    }

    private Tool findToolOrThrow(UUID id) {
        return toolRepository.findByIdAndTenant(id, currentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + id));
    }
}
