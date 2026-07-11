package com.RFE.backend.RemoteFileExplorer.Services;

import com.RFE.backend.RemoteFileExplorer.Models.DeviceInfo;
import com.RFE.backend.RemoteFileExplorer.Repositories.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;


    public DeviceInfo deviceRegister(String deviceID,String deviceName, String FCMToken){
        DeviceInfo deviceInfo = deviceRepository.findById(deviceID).orElse(new DeviceInfo());
        // check in the DB else make a new one


        deviceInfo.setDeviceID(deviceID);
        deviceInfo.setDeviceName(deviceName);
        deviceInfo.setStatus(DeviceInfo.Status.ONLINE);
        deviceInfo.setFCMToken(FCMToken);
        deviceInfo.setLastSeen(Instant.now());
        return deviceRepository.save(deviceInfo);
    }

    public void heartbeat(String deviceID){
       Optional<DeviceInfo> device = deviceRepository.findById(deviceID);
       if(device.isPresent())
       {
           DeviceInfo deviceInfo = device.get();
           deviceInfo.setLastSeen(Instant.now());
           deviceInfo.setStatus(DeviceInfo.Status.ONLINE);
           deviceRepository.save(deviceInfo);
       }
    }
    public List<DeviceInfo> getAllDevices(){
        return deviceRepository.findAll();
    }
}
