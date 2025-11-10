//package app.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//// This is a test file
//// When deployed, change url, unless secondary app run on local on cloud env !!!
//// url - base way to the microservice
//@FeignClient(name = "test-svc", url = "http://localhost:8086/api/v1")
//public interface TestClient {
//
//    // GET localhost:8081/api/v1/notifications/hello
//    @GetMapping("/notifications/hello")
//    ResponseEntity<String> getHelloMessage(@RequestParam("name") String name);
//}
//
