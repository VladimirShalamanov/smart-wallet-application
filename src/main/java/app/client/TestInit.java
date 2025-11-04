package app.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestInit implements ApplicationRunner {

    private final NotificationClien notificationClien;

    @Autowired
    public TestInit(NotificationClien notificationClien) {
        this.notificationClien = notificationClien;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        ResponseEntity<String> res = notificationClien.getHelloMessage("Vlad");
    }
}
