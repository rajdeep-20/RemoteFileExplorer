package com.RFE.backend.RemoteFileExplorer.Services;

import com.RFE.backend.RemoteFileExplorer.Models.FileMetaData;
import com.RFE.backend.RemoteFileExplorer.Repositories.FileMetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        int inserted = 0, updated = 0;
        for (FileMetaData newMetaData : fileMetaData) {
            newMetaData.setDeviceID(deviceID);
            newMetaData.setPath(normalizePath(newMetaData.getPath()));
            newMetaData.setParentPath(resolveParentPath(newMetaData));
            FileMetaData existing = existingFileMapping.get(newMetaData.getPath());

            if (existing != null) {
                boolean hasChanged = !Objects.equals(existing.getName(), newMetaData.getName()) ||
                        !Objects.equals(existing.getParentPath(), newMetaData.getParentPath()) ||
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
        }

        if (!toSave.isEmpty()) {
            fileMetaDataRepository.saveAll(toSave);
        }
        return Map.of(
                "inserted", inserted,
                "updated", updated,
                "deleted", 0);
    }

    public void syncDeltaUpsert(String deviceID, List<FileMetaData> deltaList) {
        List<FileMetaData> toSave = new ArrayList<>();
        for (FileMetaData newMetaData : deltaList) {
            newMetaData.setDeviceID(deviceID);
            newMetaData.setPath(normalizePath(newMetaData.getPath()));
            newMetaData.setParentPath(resolveParentPath(newMetaData));

            List<FileMetaData> existingFiles = fileMetaDataRepository.findByDeviceIDAndPath(deviceID, newMetaData.getPath());
            if (!existingFiles.isEmpty()) {
                FileMetaData existing = existingFiles.get(0);
                newMetaData.setId(existing.getId());
            }
            toSave.add(newMetaData);
        }
        if (!toSave.isEmpty()) {
            fileMetaDataRepository.saveAll(toSave);
        }
    }

    public void syncDeltaDelete(String deviceID, String path) {
        String normalizedPath = normalizePath(path);
        fileMetaDataRepository.deleteByDeviceIDAndPath(deviceID, normalizedPath);
        // Also delete children if it was a directory
        fileMetaDataRepository.deleteByDeviceIDAndPathStartingWith(deviceID, normalizedPath + "/");
    }

    public List<FileMetaData> getFilesByPath(String deviceID, String path) {
        return fileMetaDataRepository.findByDeviceIDAndParentPath(deviceID, normalizePath(path));
    }

    private String resolveParentPath(FileMetaData metaData) {
        if (metaData.getParentPath() != null && !metaData.getParentPath().isBlank()) {
            return normalizePath(metaData.getParentPath());
        }

        String path = normalizePath(metaData.getPath());
        if ("/".equals(path)) {
            return "/";
        }

        int lastSeparator = path.lastIndexOf("/");
        if (lastSeparator <= 0) {
            return "/";
        }
        return path.substring(0, lastSeparator);
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        String normalized = path.replace("\\", "/").trim();
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isBlank() ? "/" : normalized;
    }
}
