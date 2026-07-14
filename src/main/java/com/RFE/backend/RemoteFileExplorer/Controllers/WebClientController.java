package com.RFE.backend.RemoteFileExplorer.Controllers;

import com.RFE.backend.RemoteFileExplorer.Models.DeviceInfo;
import com.RFE.backend.RemoteFileExplorer.Models.FileMetaData;
import com.RFE.backend.RemoteFileExplorer.Models.Jobs;
import com.RFE.backend.RemoteFileExplorer.Services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/web")
public class WebClientController {

    private final DeviceService deviceService;
    private final JobCoordinatorService jobCoordinatorService;
    private final FileTransferService fileTransferService;
    private final MetaDataService metaDataService;


    @GetMapping("/devices")
    public ResponseEntity<List<DeviceInfo>> listDevices(){
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileMetaData>> listFiles(@RequestParam String deviceID, @RequestParam String path){
        return ResponseEntity.ok(metaDataService.getFilesByPath(deviceID, path));
    }

    @PostMapping("/request/downloads")
    public ResponseEntity<Jobs> requestDownload(@RequestBody Map<String, String> payload){
        String deviceID = payload.get("deviceID");
        String filePath = payload.get("path");
        return ResponseEntity.ok(jobCoordinatorService.createDownloadJob(deviceID, filePath));
    }

    @PostMapping("/download/{jobID}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String jobID){
        File file = fileTransferService.getTemporarilyStoredFile(jobID);
        if(!file.exists()){
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename\"" + file.getName() + "\"")
                .body(resource);
    }

    @PostMapping("/jobs")
    public ResponseEntity<Jobs> createJob(@RequestBody Map<String, String> payload){
        String deviceID = payload.get("deviceID");
        Jobs.Type type = Jobs.Type.valueOf(payload.get("type"));
        String jobPayload = payload.get("payload");
        return ResponseEntity.ok(jobCoordinatorService.createJob(deviceID, type, jobPayload));
    }

}
