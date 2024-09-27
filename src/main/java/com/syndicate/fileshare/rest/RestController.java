package com.syndicate.fileshare.rest;

import com.syndicate.fileshare.entity.FileData;
import com.syndicate.fileshare.entity.FolderData;
import com.syndicate.fileshare.service.AwsService;
import com.syndicate.fileshare.service.FileShareService;
import com.syndicate.fileshare.service.MyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @Autowired
    private FileShareService fileShareservice;

    @Autowired
    private AwsService awsService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    private final Logger LOGGER = LoggerFactory.getLogger(RestController.class);

    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_DESIGNER')")
    @GetMapping("/folder/myFolders")
    public ResponseEntity<?> viewMyFolders() {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            List<FolderData> folderDataList = fileShareservice.getMyFolders(currUsername);
            return new ResponseEntity<>(folderDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_DESIGNER')")
    @GetMapping("/folder/{folderId}/file")
    public ResponseEntity<?> viewFilesInFolder(@PathVariable(value ="folderId") String folderId) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            List<FileData> folderDataList = fileShareservice.getFilesByFolder(currUsername, folderId);
            return new ResponseEntity<>(folderDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_DESIGNER')")
    @GetMapping("/file/myFiles")
    public ResponseEntity<?> viewMyFiles() {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            List<FileData> fileDataList = fileShareservice.getMyfiles(currUsername);
            return new ResponseEntity<>(fileDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_DESIGNER')")
    @PostMapping("/file/upload")
    public ResponseEntity<?> uploadFile(@RequestParam(value = "folderId") String folderId, @RequestParam(value = "file") MultipartFile file) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            return new ResponseEntity<>(fileShareservice.uploadFile(currUsername, folderId, file), HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_DESIGNER')")
    @GetMapping("/download/file/{fileId}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileId) {
        try
        {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            byte[] data = fileShareservice.downloadFile(currUsername, fileId);
            String fileName = fileShareservice.getFileNameById(fileId);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        }catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/delete/file/{fileId}")
    public ResponseEntity<?> deleteFileById(@PathVariable String fileId) {
        try{
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            String res = fileShareservice.deleteFileById(currUsername, fileId);
            return new ResponseEntity(res, HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/file/edit")
    public ResponseEntity<?> renameFileById(@RequestParam String fileId, @RequestParam String newFileName) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            fileShareservice.renameFile(currUsername, fileId, newFileName);
            return new ResponseEntity("File renamed successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/folder/create")
    public ResponseEntity<?> createFolder(@RequestParam("folderName") String folderName) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            fileShareservice.createFolder(currUsername, folderName);
            return new ResponseEntity("Folder " +folderName+"/ created successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/folder/edit")
    public ResponseEntity<?> renameFolder(@RequestParam String folderId, @RequestParam String newFolderName) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            fileShareservice.renameFolder(currUsername, folderId, newFolderName);
            return new ResponseEntity("Folder renamed successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/folder/delete/{folderId}")
    public ResponseEntity<String> deleteFolder(@PathVariable String folderId) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            fileShareservice.deleteFolder(currUsername, folderId);
            return new ResponseEntity<>("Folder Deleted successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/")
    public String homePage() {
        return "File Share Application";
    }
}
