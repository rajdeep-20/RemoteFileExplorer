package com.RFE.backend.RemoteFileExplorer.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileTransferService {
   public static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "rfe_uploads";

   public FileTransferService(){
       File dir = new File(TEMP_DIR);

       if(!dir.exists()){
           dir.mkdirs();
       }
   }

   public void storeTemporarily(String jobID, MultipartFile file) throws IOException{
       Path path = Paths.get(TEMP_DIR, jobID);
       Files.write(path, file.getBytes());
   }


   public File getTemporarilyStoredFile(String jobID)
   {
       return new File(TEMP_DIR, jobID);
   }
}
