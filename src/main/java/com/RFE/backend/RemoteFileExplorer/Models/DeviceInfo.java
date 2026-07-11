package com.RFE.backend.RemoteFileExplorer.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collation = "Device Info")
public class DeviceInfo {
    @Id
    private String deviceID;
    private String deviceName;
    private String FCMToken;
    private Instant lastSeen;
    private Status status;

    public enum Status{
        ONLINE, OFFLINE, IDLE
    }

}