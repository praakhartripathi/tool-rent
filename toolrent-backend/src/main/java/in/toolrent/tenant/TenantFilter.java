package in.toolrent.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String BASE_DOMAIN = "toolrent.in";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String tenantId = resolveTenant(httpRequest);
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
        // 1. Check explicit header
        String headerTenant = request.getHeader(TENANT_HEADER);
        if (headerTenant != null && !headerTenant.isBlank()) {
            return headerTenant.trim().toLowerCase();
        }

        // 2. Extract from Host header subdomain
        String host = request.getServerName(); // e.g. abc.toolrent.in
        if (host != null && host.endsWith("." + BASE_DOMAIN)) {
            return host.replace("." + BASE_DOMAIN, "").trim().toLowerCase();
        }

        return null;
    }
}
