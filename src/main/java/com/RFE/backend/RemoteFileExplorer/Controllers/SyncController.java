package com.RFE.backend.RemoteFileExplorer.Controllers;

import ch.qos.logback.core.model.INamedModel;
import com.RFE.backend.RemoteFileExplorer.Models.FileMetaData;
import com.RFE.backend.RemoteFileExplorer.Services.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/sync")
public class SyncController {
    private final MetaDataService metaDataService;


    @PostMapping("/metadata")
    public ResponseEntity<Map<String, Integer>> syncMetaData
            (@RequestHeader("X-Device-id") String deviceID,
             @RequestBody List<FileMetaData> metaDataList){
        Map<String, Integer> stats = metaDataService.syncMetaData(deviceID, metaDataList);
        return  ResponseEntity.ok(stats);
    }
}
