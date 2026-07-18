package com.RFE.backend.RemoteFileExplorer.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Jobs")
public class Jobs {
    @Id
    private String jobID;
    @Indexed
    private String deviceID;
    private Type type;
    private String payload;
    private Status status;
    private String errorMessage;
    private String resultPayload;
    private Instant createdAt;



    public enum Type {
        DOWNLOAD, UPLOAD, SYNC
    }
    public enum Status{
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}
