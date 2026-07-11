package com.RFE.backend.RemoteFileExplorer.Repositories;

import com.RFE.backend.RemoteFileExplorer.Models.DeviceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends MongoRepository<DeviceInfo, String>{
    
}
