package in.toolrent.tenant.repository;

import in.toolrent.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySubdomain(String subdomain);
    Optional<Tenant> findByOwnerEmail(String ownerEmail);
    boolean existsBySubdomain(String subdomain);
    boolean existsByOwnerEmail(String ownerEmail);
}
