package com.RFE.backend.RemoteFileExplorer.Repositories;

import com.RFE.backend.RemoteFileExplorer.Models.FileMetaData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.net.FileNameMap;
import java.util.List;

@Repository
public interface FileMetaDataRepository extends MongoRepository<FileMetaData, String> {
    List<FileMetaData> findByDeviceIDAndPath(String deviceID, String path);
    List<FileMetaData> findByDeviceID(String deviceID);
    void deleteByDeviceID(String deviceID);
}
