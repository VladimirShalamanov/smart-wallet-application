package app.web;

import app.notification.service.NotificationService;
import app.security.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 1.Browser -> Smart Wallet
    // 2.Smart Wallet -> notification-svc via Feign call
    // 3.notification-svc return response to Smart Wallet
    // 4.Smart Wallet display the response to the Browser
    @GetMapping
    public ModelAndView getNotificationPage(@AuthenticationPrincipal UserData user) {

        ModelAndView modelAndView = new ModelAndView("notifications");
        modelAndView.addObject("preference", notificationService.getPreferenceByUserId(user.getUserId()));
        modelAndView.addObject("lastEmails", notificationService.getUserLastEmails(user.getUserId()));

        return modelAndView;
    }
}
