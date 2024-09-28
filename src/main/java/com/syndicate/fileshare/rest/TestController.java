package com.syndicate.fileshare.rest;


import com.syndicate.fileshare.entity.FileData;
import com.syndicate.fileshare.entity.FolderData;
import com.syndicate.fileshare.repository.FileRepository;
import com.syndicate.fileshare.repository.FolderRepository;
import com.syndicate.fileshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FileRepository fileRepository;

    @PostMapping("/test/folder")
    public ResponseEntity registerFolder(@RequestBody FolderData folderData){
        try {
            if (folderRepository.findByFolderName(folderData.getFolderName()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Folder already Exists. Please try with a new name");
            FolderData save = folderRepository.save(folderData);
            return ResponseEntity.ok(HttpStatus.CREATED);
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/test/file")
    public ResponseEntity registerFile(@RequestBody FileData fileData){
        try {
            if (fileRepository.findByFileName(fileData.getFileName()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Folder already Exists. Please try with a new name");
            FileData save = fileRepository.save(fileData);
            return ResponseEntity.ok(HttpStatus.CREATED);
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
