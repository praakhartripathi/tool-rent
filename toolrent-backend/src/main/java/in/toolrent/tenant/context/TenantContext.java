package in.toolrent.tenant.context;

/**
 * Thread-local holder for the current tenant's subdomain.
 * Set by TenantFilter on every incoming request.
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenant(String subdomain) {
        CURRENT_TENANT.set(subdomain);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
