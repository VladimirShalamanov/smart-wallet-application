package app.notification.client;

import app.notification.client.dto.UpsertPreferenceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name - Metadata (Service name)
// url - base URL
// When we use Feign library and annotates interface with annotation @FeignClient,
// the library will create runtime Spring component (implementation/object) that it inject
// in the others Spring components (@Service, @Component, @Rest/Controller)
@FeignClient(name = "notification-svc", url = "http://localhost:8081/api/v1")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity<Void> upsertPreference(@RequestBody UpsertPreferenceRequest requestBody);
}
