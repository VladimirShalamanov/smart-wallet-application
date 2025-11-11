package app.notification.service;

import app.notification.client.NotificationClient;
import app.notification.client.dto.Email;
import app.notification.client.dto.PreferenceResponse;
import app.notification.client.dto.UpsertPreferenceRequest;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

//8.Exercise: Microservice Architecture
@Slf4j
@Service
public class NotificationService {

    private final NotificationClient client;

    @Autowired
    public NotificationService(NotificationClient client) {
        this.client = client;
    }

    public void upsertPreference(UUID userId, boolean notificationsEnabled, String email) {

        UpsertPreferenceRequest dto = UpsertPreferenceRequest.builder()
                .userId(userId)
                .notificationEnabled(notificationsEnabled)
                .contactInfo(email)
                .build();

        // if status code is 200-299 - successful
        // if status code is another - Feign library throw error/exception
        try {
            client.upsertPreference(dto);
        } catch (FeignException e) {
            // case 1: log message
            log.error("[S2S Call]: Failed due to %s".formatted(e.getMessage()));
            // case 2: throw error and brake main operation (ex. Register)
        }
    }

    public PreferenceResponse getPreferenceByUserId(UUID userId) {

        return client.getPreferenceByUserId(userId).getBody();
    }

    public List<Email> getUserLastEmails(UUID userId) {

        ResponseEntity<List<Email>> response = client.getNotificationHistory(userId);

        return response.getBody() != null
                ? response.getBody().stream().limit(5).toList()
                : Collections.emptyList();
    }
}
