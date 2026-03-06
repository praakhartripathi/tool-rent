package in.toolrent.tenant.filter;

import in.toolrent.tenant.context.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Extracts the tenant subdomain from the request and stores it in TenantContext.
 *
 * Resolution strategy:
 *   1. X-Tenant-ID header (for API clients / mobile apps)
 *   2. Subdomain extracted from Host header (e.g. abc.toolrent.in → "abc")
 */
@Component
@Order(1)
@Slf4j
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String BASE_DOMAIN   = "toolrent.in";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        try {
            String tenantId = resolveTenant(httpReq);
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setCurrentTenant(tenantId);
                log.debug("Tenant resolved: {}", tenantId);
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenant(HttpServletRequest request) {
        // 1. Explicit header (API clients, mobile)
        String header = request.getHeader(TENANT_HEADER);
        if (header != null && !header.isBlank()) {
            return header.trim().toLowerCase();
        }
        // 2. Subdomain from Host header
        String host = request.getServerName();
        if (host != null && host.endsWith("." + BASE_DOMAIN)) {
            return host.replace("." + BASE_DOMAIN, "").trim().toLowerCase();
        }
        return null;
    }
}
