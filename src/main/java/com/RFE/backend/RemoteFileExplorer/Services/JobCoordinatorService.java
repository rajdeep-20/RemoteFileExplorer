package com.RFE.backend.RemoteFileExplorer.Services;

import com.RFE.backend.RemoteFileExplorer.Exceptions.JobNotFoundException;
import com.RFE.backend.RemoteFileExplorer.Models.Jobs;
import com.RFE.backend.RemoteFileExplorer.Repositories.JobsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobCoordinatorService {
    private final JobsRepository jobrepo;
    private final FCMNotificationService FCMnotificationService;


    public Jobs createJob(String deviceID, Jobs.Type type, String payload){
        Jobs job = new Jobs();
        job.setJobID(UUID.randomUUID().toString());
        job.setDeviceID(deviceID);
        job.setType(type);
        job.setPayload(payload);
        job.setCreatedAt(Instant.now());
        job.setStatus(Jobs.Status.PENDING);
        job = jobrepo.save(job);

        FCMnotificationService.sendWakeUpcall(deviceID);
        return job;
    }

    public Jobs createDownloadJob(String deviceID, String filePath)
    {
        return createJob(deviceID, Jobs.Type.DOWNLOAD, filePath);
    }

    public List<Jobs> getPendingJobs(String deviceID){
        return jobrepo.findByDeviceIDAndStatus(deviceID, Jobs.Status.PENDING);
    }

    public Jobs claimNextJob(String deviceID){
        List<Jobs> pendingJobs = jobrepo.findByDeviceIDAndStatus(deviceID, Jobs.Status.PENDING);

        if(!pendingJobs.isEmpty()){
            Jobs jobs = pendingJobs.get(0);
            jobs.setStatus(Jobs.Status.IN_PROGRESS);
            return jobrepo.save(jobs);
        }
        return null;
    }

    public void updateJobStatus(String jobID, Jobs.Status status, String errorMessage, String resultPayload){
        Jobs jobs = jobrepo.findById(jobID).
                orElseThrow(() -> new JobNotFoundException("Jobs with ID " + jobID + "not found"));

        jobs.setStatus(status);
        if(errorMessage != null)
        {
            jobs.setErrorMessage(errorMessage);
        }
        if(resultPayload != null)
        {
            jobs.setResultPayload(resultPayload);
        }

        jobrepo.save(jobs);
    }


}
