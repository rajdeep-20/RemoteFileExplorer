package com.RFE.backend.RemoteFileExplorer.Services;

import com.RFE.backend.RemoteFileExplorer.Repositories.DeviceRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class FCMNotificationService {

    private final DeviceRepository deviceRepository;

    public void sendWakeUpcall(String deviceID){
        deviceRepository.findById(deviceID).ifPresent(deviceInfo -> {
            if(deviceInfo.getFCMToken() != null && !deviceInfo.getFCMToken().isEmpty())
            {
                try {
                    Message message = Message.builder()
                            .putData("type", "CHECK_JOBS")
                            .setToken(deviceInfo.getFCMToken())
                            .setAndroidConfig(AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .build())
                            .build();
                    String response = FirebaseMessaging.getInstance().send(message);
                    log.info("Successfully Sent FCM message {}", response);
                } catch (FirebaseMessagingException e) {
                    log.error("FirebaseMessagingException for device {}: {}", deviceID, e.getMessagingErrorCode());
                    if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                        e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                        log.warn("FCM token is invalid or unregistered. Clearing token for device {}", deviceID);
                        deviceInfo.setFCMToken(null);
                        deviceRepository.save(deviceInfo);
                    }
                } catch (Exception e) {
                    log.error("Error in sending FCM Message {}", deviceID, e);
                }
            }
        });

    }
}
