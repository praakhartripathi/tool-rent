package in.toolrent.tool;

import in.toolrent.tenant.Tenant;
import in.toolrent.tenant.TenantContext;
import in.toolrent.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository toolRepository;
    private final TenantRepository tenantRepository;

    private Tenant currentTenant() {
        String subdomain = TenantContext.getCurrentTenant();
        return tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + subdomain));
    }

    public List<Tool> getAllTools() {
        return toolRepository.findByTenant(currentTenant());
    }

    public List<Tool> getAvailableTools() {
        return toolRepository.findByTenantAndIsAvailableTrue(currentTenant());
    }

    public Tool getToolById(UUID id) {
        Tenant tenant = currentTenant();
        return toolRepository.findByIdAndTenant(id, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + id));
    }

    @Transactional
    public Tool createTool(ToolRequest request) {
        Tenant tenant = currentTenant();
        Tool tool = Tool.builder()
                .tenant(tenant)
                .name(request.getName())
                .description(request.getDescription())
                .pricePerDay(request.getPricePerDay())
                .depositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .availableQuantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .category(request.getCategory())
                .build();
        return toolRepository.save(tool);
    }

    @Transactional
    public Tool updateTool(UUID id, ToolRequest request) {
        Tool tool = getToolById(id);
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setPricePerDay(request.getPricePerDay());
        tool.setDepositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO);
        tool.setQuantity(request.getQuantity() != null ? request.getQuantity() : tool.getQuantity());
        tool.setCategory(request.getCategory());
        return toolRepository.save(tool);
    }

    @Transactional
    public void deleteTool(UUID id) {
        Tool tool = getToolById(id);
        toolRepository.delete(tool);
    }
}
