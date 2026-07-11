package com.RFE.backend.RemoteFileExplorer.Services;

import com.RFE.backend.RemoteFileExplorer.Repositories.DeviceRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
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
                try{
                    Message message = Message.builder()
                            .putData("type", "CHECK_JOBS")
                            .setToken(deviceInfo.getFCMToken())
                            .build();
                    String response = FirebaseMessaging.getInstance().send(message);
                    log.info("Successfully Sent FCM message {}", response);
                }
                catch (Exception e){
                    log.error("Error in sending FCM Message {}", deviceID, e);
                }
            }
        });

    }
}
