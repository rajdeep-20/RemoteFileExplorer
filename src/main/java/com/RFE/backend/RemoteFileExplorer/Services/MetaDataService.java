package com.RFE.backend.RemoteFileExplorer.Services;

import com.RFE.backend.RemoteFileExplorer.Models.FileMetaData;
import com.RFE.backend.RemoteFileExplorer.Repositories.FileMetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MetaDataService {
    private final FileMetaDataRepository fileMetaDataRepository;

    public Map<String, Integer> syncMetaData(String deviceID, List<FileMetaData> fileMetaData) {
        List<FileMetaData> existingFiles = fileMetaDataRepository.findByDeviceID(deviceID);
        Map<String, FileMetaData> existingFileMapping = existingFiles.stream()
                .collect(Collectors.toMap(FileMetaData::getPath, m -> m));

        List<FileMetaData> toSave = new ArrayList<>();
        List<FileMetaData> toDelete = new ArrayList<>();

        int inserted = 0, updated = 0;
        for (FileMetaData newMetaData : fileMetaData) {
            newMetaData.setDeviceID(deviceID);
            FileMetaData existing = existingFileMapping.get(newMetaData.getPath());

            if (existing != null) {
                boolean hasChanged = !Objects.equals(existing.getName(), newMetaData.getName()) ||
                        !Objects.equals(existing.getSize(), newMetaData.getSize()) ||
                        !Objects.equals(existing.getIsDirectory(), newMetaData.getIsDirectory()) ||
                        !Objects.equals(existing.getLastModified(), newMetaData.getLastModified());

                if (hasChanged) {
                    newMetaData.setId(existing.getId());
                    toSave.add(newMetaData);
                    updated += 1;
                }
                existingFileMapping.remove(newMetaData.getPath());
            } else {
                toSave.add(newMetaData);
                inserted += 1;
            }
            toDelete.addAll(existingFileMapping.values());

            if (!toSave.isEmpty()) {
                fileMetaDataRepository.saveAll(toSave);
            }
            if (!toDelete.isEmpty()) {
                fileMetaDataRepository.deleteAll(toDelete);
            }
        }
        return Map.of(
                "inserted", inserted,
                "updated", updated,
                "deleted", toDelete.size());


    }

    public List<FileMetaData> getFilesByPath(String deviceID, String path) {
        return fileMetaDataRepository.findByDeviceIDAndPath(deviceID, path);
        // get the file from the server based on file Path
    }
}
