package in.toolrent.auth.repository;

import in.toolrent.auth.entity.User;
import in.toolrent.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailAndTenant(String email, Tenant tenant);
    Optional<User> findByEmail(String email);
    boolean existsByEmailAndTenant(String email, Tenant tenant);
}
