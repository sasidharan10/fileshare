package com.syndicate.fileshare.rest;

import com.syndicate.fileshare.entity.FileData;
import com.syndicate.fileshare.entity.FolderData;
import com.syndicate.fileshare.entity.MyUser;
import com.syndicate.fileshare.repository.UserRepository;
import com.syndicate.fileshare.service.AdminService;
import com.syndicate.fileshare.service.AwsService;
import com.syndicate.fileshare.service.FileShareService;
import com.syndicate.fileshare.service.MyUserDetailsService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @PostMapping("/user/add")
    public ResponseEntity<?> addUser(@RequestBody MyUser user){
        try {
            adminService.addUser(user);
            return new ResponseEntity<>("User Added to the DB", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/user/remove")
    public ResponseEntity<?> removeUser(@RequestParam(value = "userId") String userId){
        try {
            adminService.removeUser(userId);
            return new ResponseEntity<>("User Removed from the DB", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/folder/allFolders")
    public ResponseEntity<?> viewAllFoldersAdmin() {
        try {
            List<FolderData> folderDataList = adminService.getAllFolders();
            return new ResponseEntity<>(folderDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/folder/{folderId}/files")
    public ResponseEntity<?> viewFilesInFolderAdmin(@PathVariable(value = "folderId") String folderId) {
        try {
            List<FileData> folderDataList = adminService.getFilesByFolderAdmin(folderId);
            return new ResponseEntity<>(folderDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/file/allFiles")
    public ResponseEntity<?> viewAllFilesAdmin() {
        try {
            List<FileData> fileDataList = adminService.getAllFiles();
            return new ResponseEntity<>(fileDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/file/upload")
    public ResponseEntity<?> uploadFileAdmin(@RequestParam(value = "folderId") String folderId, @RequestParam(value = "file") MultipartFile file) {
        try {
            String currUsername = myUserDetailsService.getCurrentUser().getUsername();
            FileData newFile = adminService.uploadFileAdmin(currUsername, folderId, file);
            return new ResponseEntity<>(newFile, HttpStatus.OK);
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
            return new ResponseEntity("File renamed to " + newFileName  + "...", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/folder/create")
    public ResponseEntity<?> createFolderAdmin(@RequestParam("folderName") @Size(min = 2, max = 20, message = "Folder name must be between 2 and 20 characters")
                                                   @Pattern(regexp = "^[a-zA-Z0-9]+$", message ="Folder name must be alphanumeric") String folderName) {
        try {
            FolderData newFolder = adminService.createFolderAdmin(folderName);
            return new ResponseEntity(newFolder, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/folder/edit")
    public ResponseEntity<?> renameFolderAdmin(@RequestParam String folderId, @RequestParam @Size(min = 2, max = 20, message = "Folder name must be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message ="Folder name must be alphanumeric") String newFolderName) {
        try {
            adminService.renameFolderAdmin(folderId, newFolderName);
            return new ResponseEntity("Folder renamed to " + newFolderName + "...", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/folder/delete/{folderId}")
    public ResponseEntity<String> deleteFolderAdmin(@PathVariable String folderId) {
        try {
            adminService.deleteFolderAdmin(folderId);
            return new ResponseEntity<>("Folder Deleted successfully...", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/folder/assign")
    public ResponseEntity<?> assignFolderAdmin(@RequestParam String userId, @RequestParam String folderId) {
        try {
            adminService.assignFolderAdmin(userId,  folderId);
            return new ResponseEntity("Folder assigned to the User...", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/folder/unassign")
    public ResponseEntity<?> unassignFolderAdmin(@RequestParam String userId, @RequestParam String folderId) {
        try {
            adminService.unassignFolderAdmin(userId,  folderId);
            return new ResponseEntity("Folder Unassigned from the User...", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
