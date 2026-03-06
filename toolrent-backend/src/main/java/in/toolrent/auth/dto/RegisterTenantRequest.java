package in.toolrent.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterTenantRequest {

    @NotBlank
    private String businessName;

    @NotBlank
    @Size(min = 3, max = 50)
    private String subdomain;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    private String fullName;
}
