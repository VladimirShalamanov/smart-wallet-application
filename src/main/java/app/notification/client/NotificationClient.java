package app.notification.client;

import app.notification.client.dto.Email;
import app.notification.client.dto.EmailRequest;
import app.notification.client.dto.PreferenceResponse;
import app.notification.client.dto.UpsertPreferenceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

// name - Metadata (Service name)
// url - base URL
// When we use Feign library and annotates interface with annotation @FeignClient,
// the library will create runtime Spring component (implementation/object) that it inject
// in the others Spring components (@Service, @Component, @Rest/Controller)
@FeignClient(name = "notification-svc", url = "http://localhost:8081/api/v1")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity<Void> upsertPreference(@RequestBody UpsertPreferenceRequest requestBody);

    @GetMapping("/preferences")
    ResponseEntity<PreferenceResponse> getPreferenceByUserId(@RequestParam("userId") UUID userId);

    @GetMapping("/notifications")
    ResponseEntity<List<Email>> getNotificationHistory(@RequestParam("userId") UUID userId);

    @PostMapping("/notifications")
    ResponseEntity<Void> sendEmail(@RequestBody EmailRequest requestBody);
}
