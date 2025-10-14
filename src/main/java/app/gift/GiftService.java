package app.gift;

import app.event.SuccessfulChargeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class GiftService {

    @EventListener
    public void sendGift(SuccessfulChargeEvent event){
        System.out.printf("Sending 1 EUR for charge compensation for the user with email [s%]", event.getEmail());
    }
}
