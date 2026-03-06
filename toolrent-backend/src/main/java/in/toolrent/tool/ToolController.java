package in.toolrent.tool;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    /** GET /api/tools — public endpoint (available tools for storefront) */
    @GetMapping
    public ResponseEntity<List<Tool>> getTools(
            @RequestParam(defaultValue = "false") boolean adminView) {
        List<Tool> tools = adminView
                ? toolService.getAllTools()
                : toolService.getAvailableTools();
        return ResponseEntity.ok(tools);
    }

    /** GET /api/tools/{id} — public */
    @GetMapping("/{id}")
    public ResponseEntity<Tool> getToolById(@PathVariable UUID id) {
        return ResponseEntity.ok(toolService.getToolById(id));
    }

    /** POST /api/tools — admin only */
    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Tool> createTool(@Valid @RequestBody ToolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toolService.createTool(request));
    }

    /** PUT /api/tools/{id} — admin only */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Tool> updateTool(@PathVariable UUID id,
                                            @Valid @RequestBody ToolRequest request) {
        return ResponseEntity.ok(toolService.updateTool(id, request));
    }

    /** DELETE /api/tools/{id} — admin only */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> deleteTool(@PathVariable UUID id) {
        toolService.deleteTool(id);
        return ResponseEntity.noContent().build();
    }
}
