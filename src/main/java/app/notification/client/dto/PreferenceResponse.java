package app.notification.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferenceResponse {

    // For "smart-wallet" we use only Email. Current Api return to us 3 items, but we use only 2 with correct names.
    // private NotificationType type;

    private boolean notificationEnabled;

    private String contactInfo;
}
