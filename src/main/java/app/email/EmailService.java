package app.email;

import app.event.SuccessfulChargeEvent;
import app.user.model.User;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class EmailService {

    @Async
    @Order(2)
    @EventListener
    public void sendEmail(SuccessfulChargeEvent event) {

        String threadName = Thread.currentThread().getName();
        System.out.println("Thread in EmailService.java: " + threadName);

        if (event.getEmail() != null) {
            System.out.printf("Sending Email for new payment for user with [%s]", event.getEmail());
        }
    }

    public void sendReminderEmail(User user) {
        System.out.printf("[%s] Email sent to [%s] with username [%s].\n", LocalTime.now(), user.getRole(), user.getUsername());
    }
}
