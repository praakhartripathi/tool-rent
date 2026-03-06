package in.toolrent.tool.repository;

import in.toolrent.tenant.entity.Tenant;
import in.toolrent.tool.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ToolRepository extends JpaRepository<Tool, UUID> {
    List<Tool>     findByTenantAndIsAvailableTrue(Tenant tenant);
    List<Tool>     findByTenant(Tenant tenant);
    Optional<Tool> findByIdAndTenant(UUID id, Tenant tenant);
}
