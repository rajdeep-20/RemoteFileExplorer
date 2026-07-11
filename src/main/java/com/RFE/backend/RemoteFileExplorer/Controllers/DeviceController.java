package com.RFE.backend.RemoteFileExplorer.Controllers;

import com.RFE.backend.RemoteFileExplorer.Models.DeviceInfo;
import com.RFE.backend.RemoteFileExplorer.Services.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<DeviceInfo> registerDevice(@RequestBody Map<String, String> payload){
        String deviceID = payload.get("deviceID");
        String FCMToken = payload.get("FCMToken");
        String deviceName = payload.get("deviceName");
        return ResponseEntity.ok(deviceService.deviceRegister(deviceID, FCMToken, deviceName));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void>  heartbeat(@RequestBody Map<String, String> payload){
        String deviceID = payload.get("deviceID");
        deviceService.heartbeat(deviceID);
        return ResponseEntity.ok().build();
    }
}
