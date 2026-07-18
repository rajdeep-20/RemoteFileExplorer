package com.RFE.backend.RemoteFileExplorer.Controllers;

import com.RFE.backend.RemoteFileExplorer.Models.FileMetaData;
import com.RFE.backend.RemoteFileExplorer.Services.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sync")
public class SyncController {
    private final MetaDataService metaDataService;


    @PostMapping("/metadata")
    public ResponseEntity<Map<String, Integer>> syncMetaData
            (@RequestHeader("X-Device-ID") String deviceID,
             @RequestBody List<FileMetaData> metaDataList){
        Map<String, Integer> stats = metaDataService.syncMetaData(deviceID, metaDataList);
        return  ResponseEntity.ok(stats);
    }

    @PostMapping("/delta/upsert")
    public ResponseEntity<Void> syncDeltaUpsert
            (@RequestHeader("X-Device-ID") String deviceID,
             @RequestBody List<FileMetaData> deltaList){
        metaDataService.syncDeltaUpsert(deviceID, deltaList);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delta/delete")
    public ResponseEntity<Void> syncDeltaDelete
            (@RequestHeader("X-Device-ID") String deviceID,
             @RequestHeader("X-File-Path") String path){
        metaDataService.syncDeltaDelete(deviceID, path);
        return ResponseEntity.ok().build();
    }
}
