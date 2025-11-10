package app.job;

import app.email.EmailService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminReminderScheduler {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public AdminReminderScheduler(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @Async
    @Scheduled(fixedDelay = 60_000)
    public void sendReminderToAdmins() throws InterruptedException {

        List<User> admins = userService.getAll()
                .stream()
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .toList();

        admins.forEach(emailService::sendReminderEmail);
    }
}
