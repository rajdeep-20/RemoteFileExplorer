package com.RFE.backend.RemoteFileExplorer.Controllers;

import com.RFE.backend.RemoteFileExplorer.Exceptions.JobNotFoundException;
import com.RFE.backend.RemoteFileExplorer.Models.Jobs;
import com.RFE.backend.RemoteFileExplorer.Services.FileTransferService;
import com.RFE.backend.RemoteFileExplorer.Services.JobCoordinatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final FileTransferService fileTransferService;
    private final JobCoordinatorService jobCoordinatorService;


    @GetMapping("/pending")
    public ResponseEntity<List<Jobs>> getPendingJobs(@RequestHeader("X-Device-ID") String deviceID){
        return ResponseEntity.ok(jobCoordinatorService.getPendingJobs(deviceID));
    }


    @PostMapping("/claim")
    public ResponseEntity<Jobs> claimNextJob(@RequestHeader("X-Device-ID") String deviceID){
        Jobs jobs = jobCoordinatorService.claimNextJob(deviceID);
        return jobs != null ? ResponseEntity.ok(jobs) : ResponseEntity.noContent().build();
    }


    @PostMapping("/{jobID}/complete")
    public ResponseEntity<Void> completeJob(@PathVariable String jobID, @RequestBody Map<String, String> payload){
        Jobs.Status jobStatus = Jobs.Status.valueOf(payload.get("status"));
        String errorMessage = payload.get("errorMessage");
        String resultPayload = payload.get("resultPayload");
        jobCoordinatorService.updateJobStatus(jobID, jobStatus, errorMessage, resultPayload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload/{jobID}")
    public ResponseEntity<Void> uploadFile(@PathVariable String jobID, @RequestParam("file")MultipartFile file){
        try{
           fileTransferService.storeTemporarily(jobID, file);
           String originalFile = file.getOriginalFilename();
           jobCoordinatorService.updateJobStatus(jobID, Jobs.Status.COMPLETED, null, originalFile);
           return ResponseEntity.ok().build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e){
            try{
                jobCoordinatorService.updateJobStatus(jobID, Jobs.Status.FAILED, e.getMessage(), null);
            }
            catch(JobNotFoundException ignored) {}
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/download/{jobID}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String jobID){
        File file = fileTransferService.getTemporarilyStoredFile(jobID);
        if(!file.exists()){
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}
