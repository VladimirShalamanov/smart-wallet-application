package app.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// When deployed, change url, unless secondary app run on local on cloud env !!!
// url - base way to the microservice
@FeignClient(name = "notification-svc", url = "http://localhost:8081/api/v1")
public interface NotificationClient {

    // GET localhost:8081/api/v1/notifications/hello
    @GetMapping("/notifications/hello")
    ResponseEntity<String> getHelloMessage(@RequestParam("name") String name);
}

