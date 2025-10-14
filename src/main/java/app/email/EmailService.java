package app.email;

import app.event.SuccessfulChargeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @EventListener
    public  void sendEmail(SuccessfulChargeEvent event){

        System.out.printf("Sending Email for new payment for user with [s%]", event.getEmail());
    }
}
