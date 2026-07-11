package com.RFE.backend.RemoteFileExplorer.Repositories;

import com.RFE.backend.RemoteFileExplorer.Models.Jobs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JobsRepository extends MongoRepository<Jobs, String> {
    List<Jobs> findByDeviceIDAndStatus(String deviceID, Jobs.Status status);
}
