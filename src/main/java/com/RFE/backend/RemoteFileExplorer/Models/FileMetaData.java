package com.RFE.backend.RemoteFileExplorer.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Files")
public class FileMetaData {
    @Id
    private String id;
    @Indexed
    private String deviceID;
    private String path;
    @Indexed
    private String parentPath;
    private String name;
    private long size;
    private long lastModified;
    private Boolean isDirectory;
}
