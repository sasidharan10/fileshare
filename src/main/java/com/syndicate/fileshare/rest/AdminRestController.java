package com.syndicate.fileshare.rest;

import com.syndicate.fileshare.entity.FileData;
import com.syndicate.fileshare.entity.FolderData;
import com.syndicate.fileshare.service.AdminService;
import com.syndicate.fileshare.service.AwsService;
import com.syndicate.fileshare.service.FileShareService;
import com.syndicate.fileshare.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminRestController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private FileShareService fileShareservice;

    @Autowired
    private AwsService awsService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/folder/allFolders")
    public ResponseEntity<?> viewAllFoldersAdmin() {
        try {
            List<FolderData> folderDataList = adminService.getAllFolders();
            return new ResponseEntity<>(folderDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/folder/{folderId}/file")
    public ResponseEntity<?> viewFilesInFolderAdmin(@PathVariable(value = "folderId") String folderId) {
        try {
            List<FileData> folderDataList = adminService.getFilesByFolderAdmin(folderId);
            return new ResponseEntity<>(folderDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/file/allFiles")
    public ResponseEntity<?> viewAllFilesAdmin() {
        try {
            List<FileData> fileDataList = adminService.getAllFiles();
            return new ResponseEntity<>(fileDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/file/upload")
    public ResponseEntity<?> uploadFileAdmin(@RequestParam(value = "folderId") String folderId, @RequestParam(value = "file") MultipartFile file) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            return new ResponseEntity<>(adminService.uploadFileAdmin(currUsername, folderId, file), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<?> downloadFileAdmin(@PathVariable String fileId) {
        try {
            byte[] data = adminService.downloadFileAdmin(fileId);
            String fileName = fileShareservice.getFileNameById(fileId);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/file/delete/{fileId}")
    public ResponseEntity<?> deleteFileByIdAdmin(@PathVariable String fileId) {
        try {
            String res = adminService.deleteFileAdmin(fileId);
            return new ResponseEntity(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/file/edit")
    public ResponseEntity<?> renameFileAdmin(@RequestParam String fileId, @RequestParam String newFileName) {
        try {
            adminService.renameFileAdmin(fileId, newFileName);
            return new ResponseEntity("File renamed successfully as: " + newFileName, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/folder/create")
    public ResponseEntity<?> createFolderAdmin(@RequestParam("folderName") String folderName, @RequestParam("managerId") String managerId) {
        try {
            adminService.createFolderAdmin(managerId, folderName);
            return new ResponseEntity("Folder " +folderName+"/ created successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/folder/edit")
    public ResponseEntity<?> renameFolderAdmin(@RequestParam String folderId, @RequestParam String newFolderName) {
        try {
            adminService.renameFolderAdmin(folderId, newFolderName);
            return new ResponseEntity("Folder renamed successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/folder/delete/{folderId}")
    public ResponseEntity<String> deleteFolderAdmin(@PathVariable String folderId) {
        try {
            adminService.deleteFolderAdmin(folderId);
            return new ResponseEntity<>("Folder Dleted successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
