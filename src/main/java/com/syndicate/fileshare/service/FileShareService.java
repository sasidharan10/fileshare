package com.syndicate.fileshare.service;

import com.syndicate.fileshare.entity.FileData;
import com.syndicate.fileshare.entity.FolderData;
import com.syndicate.fileshare.entity.MyUser;
import com.syndicate.fileshare.repository.FileRepository;
import com.syndicate.fileshare.repository.FolderRepository;
import com.syndicate.fileshare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class FileShareService {

    @Autowired
    private AwsService awsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(FileShareService.class);

    public String getFileNameById(String fileId) throws Exception {
        Optional<String> tempName = fileRepository.findFileNameById(fileId);
        if (!tempName.isPresent()) {
            throw new Exception("File not Found!!!");
        }
        return tempName.get();
    }

    public List<FolderData> getMyFolders(String currUsername) throws Exception {
        Optional<MyUser> userInfo = userRepository.findByUsername(currUsername);
        if (!userInfo.isPresent())
            throw new Exception("User not found!!!");
        MyUser userObj = userInfo.get();
//        LOGGER.info("service layer: "+ userObj.getFolderAccess().toString());
        List<FolderData> folderDataList = new ArrayList<>();
        for (String folderId : userObj.getFolderList()) {
            FolderData temp = folderRepository.findById(folderId).get();
            folderDataList.add(temp);
        }
        return folderDataList;
    }

    public List<FileData> getMyfiles(String currUsername) throws Exception {
        Optional<MyUser> userInfo = userRepository.findByUsername(currUsername);
        if (!userInfo.isPresent())
            throw new Exception("User not found!!!");
        List<FileData> fileDataList = new ArrayList<>();
        if (userInfo.get().getFolderList().isEmpty())
            return fileDataList;
        for (String folderId : userInfo.get().getFolderList()) {
            Optional<FolderData> tempFolder = folderRepository.findById(folderId);
            if (!tempFolder.isPresent())
                continue;
            for (String fileId : tempFolder.get().getFilesList()) {
                Optional<FileData> tempFile = fileRepository.findById(fileId);
                if (!tempFile.isPresent())
                    continue;
                fileDataList.add(tempFile.get());
            }
        }
        return fileDataList;
    }

    public List<FileData> getFilesByFolder(String currUsername, String folderId) throws Exception {
        Optional<MyUser> userInfo = userRepository.findByUsername(currUsername);
        if (!userInfo.isPresent())
            throw new Exception("User not found!!!");
        if (!userInfo.get().getFolderList().contains(folderId))
            throw new Exception("Folder is not accessible by the user!!!");
        Optional<FolderData> tempFolder = folderRepository.findById(folderId);
        if (!tempFolder.isPresent())
            throw new Exception("Folder not found in the DB!!!");
        List<FileData> fileDataList = new ArrayList<>();
        if (tempFolder.get().getFilesList().isEmpty())
            return fileDataList;
        for (String fileId : tempFolder.get().getFilesList()) {
            Optional<FileData> tempFile = fileRepository.findById(fileId);
            if (!tempFile.isPresent())
                continue;
            fileDataList.add(tempFile.get());
        }
        return fileDataList;
    }

    public FileData uploadFileInFolder(String currUsername, String folderId, MultipartFile file) throws Exception {
        Optional<MyUser> tempUser = userRepository.findByUsername(currUsername);
        if (!tempUser.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser userObj = tempUser.get();
        if (!userObj.getFolderList().contains(folderId)) {
            throw new Exception("User cannot access this folder!!!");
        }
        Optional<FolderData> tempFolder = folderRepository.findById(folderId);
        if (!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();
        // check if file name already exists
        Optional<FileData> tempFile = fileRepository.findByFileName(file.getOriginalFilename());
        if (tempFile.isPresent()) {
            throw new Exception("File name already exists!!!");
        }
        // upload to s3
        awsService.uploadFileInS3(currUsername, folderObj.getFolderName(), file);
        // create new fileData
        FileData newFile = new FileData();
        newFile.setFileName(file.getOriginalFilename());
        newFile.setFolderId(folderObj.getId());
        newFile.setCreator(currUsername);
        fileRepository.save(newFile); // save fileData to DB
        LOGGER.info("New file Info: " + newFile.toString());
        // add to fileList
        Set<String> files = folderObj.getFilesList();
        if (files == null) {
            files = new HashSet<>();  // Initialize if null
        }
        files.add(newFile.getId());
        folderObj.setFilesList(files);
        // Save the updated folder back to the database
        folderRepository.save(folderObj);
        return newFile;
    }

    public byte[] downloadFile(String currUsername, String fileId) throws Exception {
        Optional<MyUser> tempUser = userRepository.findByUsername(currUsername);
        if (!tempUser.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser userObj = tempUser.get();
        Optional<FileData> tempFile = fileRepository.findById(fileId);
        if (!tempFile.isPresent()) {
            throw new Exception("File not Found!!!");
        }
        FileData fileObj = tempFile.get();

        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(fileObj.getFolderId());
        if (!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();

        if (!userObj.getFolderList().contains(fileObj.getFolderId())) {
            throw new Exception("File is not accessible by the user!!!");
        }
        String fileName = folderObj.getFolderName() + "/" + fileObj.getFileName();
        byte[] file = awsService.downloadFileFromS3(fileName);
        return file;
    }

    public String deleteFileById(String currUsername, String fileId) throws Exception {
        // check if user available
        Optional<MyUser> tempUser = userRepository.findByUsername(currUsername);
        if (!tempUser.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser userObj = tempUser.get();

        Optional<FileData> tempFile = fileRepository.findById(fileId);
        if (!tempFile.isPresent()) {
            throw new Exception("File not Found!!!");
        }
        FileData fileObj = tempFile.get();

        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(fileObj.getFolderId());
        if (!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();

        // check if manager has access to delete it:
        if (!userObj.getFolderList().contains(fileObj.getFolderId())) {
            throw new Exception("User cannot access this folder!!!");
        }

        String fileName = folderObj.getFolderName() + "/" + fileObj.getFileName();
        // delete from S3:
        awsService.deleteFile(fileName);

        // remove fileId from its folder:
        Set<String> files = folderObj.getFilesList();
        if (files == null) {
            files = new HashSet<>();  // Initialize if null
        }
        files.remove(fileId);
        folderObj.setFilesList(files);
        folderRepository.save(folderObj);

        // delete file data from files collection
        fileRepository.deleteById(fileId);
        return fileName + " deleted successfully ...";
    }

    public void renameFile(String currUsername, String fileId, String newFileName) throws Exception {
        // duplicate check
        Optional<FileData> nameCheck = fileRepository.findByFileName(newFileName);
        if (nameCheck.isPresent()) {
            throw new Exception("Folder Name already exists!!!");
        }
        Optional<FileData> tempFile = fileRepository.findById(fileId);
        if (!tempFile.isPresent()) {
            throw new Exception("File not Found!!!");
        }
        FileData fileObj = tempFile.get();

        // check if user available
        Optional<MyUser> tempUser = userRepository.findByUsername(currUsername);
        if (!tempUser.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser userObj = tempUser.get();

        // check if user has to folder
        if (!userObj.getFolderList().contains(fileObj.getFolderId())) {
            throw new Exception("User cannot access this folder!!!");
        }

        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(fileObj.getFolderId());
        if (!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();
        String oldFullFileName = folderObj.getFolderName() + "/" + fileObj.getFileName();
        String newFullFileName = folderObj.getFolderName() + "/" + newFileName;
        // rename file in s3
        awsService.renameFileInS3(oldFullFileName, newFullFileName);

        // rename file name in db
        fileObj.setFileName(newFileName);
        fileRepository.save(fileObj);
    }

    public FolderData createFolder(String currUsername, String folderName) throws Exception {
        // duplicate check
        Optional<FolderData> nameCheck = folderRepository.findByFolderName(folderName);
        if (nameCheck.isPresent()) {
            throw new Exception("Folder Name already exists!!!");
        }
        Optional<MyUser> tempManager = userRepository.findByUsername(currUsername);
        if (!tempManager.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser managerObj = tempManager.get();

        // create folder in s3
        awsService.createFolderInS3(folderName);

        // save folder info in db
        FolderData newFolder = new FolderData();
        newFolder.setFolderName(folderName);
        newFolder.setOwner(managerObj.getUsername());
        newFolder.setFilesList(new HashSet<>());
        folderRepository.save(newFolder);

        // add folder id in manager's folderList
        Set<String> newFolderList = managerObj.getFolderList();
        newFolderList.add(newFolder.getId());
        managerObj.setFolderList(newFolderList);
        userRepository.save(managerObj);
        return newFolder;
    }

    public void renameFolder(String currUsername, String folderId, String newFolderName) throws Exception {
        // duplicate check
        Optional<FolderData> nameCheck = folderRepository.findByFolderName(newFolderName);
        if (nameCheck.isPresent()) {
            throw new Exception("Folder Name already exists!!!");
        }
        Optional<MyUser> tempManager = userRepository.findByUsername(currUsername);
        if (!tempManager.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser managerObj = tempManager.get();

        if (!managerObj.getFolderList().contains(folderId)) {
            throw new Exception("The User cannot access this folder!!!");
        }

        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(folderId);
        if (!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();

        // rename in s3
        String oldFolderName = folderObj.getFolderName();
        awsService.renameFolderInS3(oldFolderName, newFolderName);

        // rename in db
        folderObj.setFolderName(newFolderName);
        folderRepository.save(folderObj);
    }

    public void deleteFolder(String currUsername, String folderId) throws Exception {
        // check if manager has access to folder
        Optional<MyUser> tempManager = userRepository.findByUsername(currUsername);
        if (!tempManager.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser managerObj = tempManager.get();

        if (!managerObj.getFolderList().contains(folderId)) {
            throw new Exception("The User cannot access this folder!!!");
        }

        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(folderId);
        if(!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();
        String folderName = folderObj.getFolderName();

        // remove folder ids from all users.
        List<MyUser> userList = userRepository.findAll();
        for (MyUser userObj: userList)
        {
            if(userObj.getFolderList().contains(folderId))
            {
                Set<String> newFolderList = userObj.getFolderList();
                newFolderList.remove(folderId);
                userObj.setFolderList(newFolderList);
                userRepository.save(userObj);
            }
        }

        // remove all files from DB
        Set<String> filesList = folderObj.getFilesList();
        for (String fileId: filesList)
        {
            fileRepository.deleteById(fileId);
        }

        // delete folderData from DB
        folderRepository.deleteById(folderId);

        // delete the folder from S3
        awsService.deleteFolderInS3(folderName);
    }

    public void assignFolder(String currManager, String userId, String folderId) throws Exception {
        // check folder id is valid
        if (!folderRepository.findById(folderId).isPresent()) {
            throw new Exception("Folder Not Found!!!");
        }
        // check if manager has access to the folder
        Optional<MyUser> tempManager = userRepository.findByUsername(currManager);
        if (!tempManager.isPresent()) {
            throw new Exception("Manager not found!!!");
        }
        MyUser managerObj = tempManager.get();
        if(!managerObj.getFolderList().contains(folderId))
        {
            throw new Exception("Manager does not have access to this folder!!!");
        }
        // check if user is a designer
        Optional<MyUser> tempUser = userRepository.findById(userId);
        if (!tempUser.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser userObj = tempUser.get();
        if (!userObj.getRole().equals("DESIGNER")) {
            throw new Exception("Manager can assign folders to only Designers!!!");
        }

        // add folder id to user's folder list
        Set<String> newFolderList = userObj.getFolderList();
        newFolderList.add(folderId);
        userObj.setFolderList(newFolderList);
        userRepository.save(userObj);
    }

    public void unassignFolderAdmin(String currManager, String userId, String folderId) throws Exception {
        // check folder id is valid
        if (!folderRepository.findById(folderId).isPresent()) {
            throw new Exception("Folder Not Found!!!");
        }
        // check if manager has access to the folder
        Optional<MyUser> tempManager = userRepository.findByUsername(currManager);
        if (!tempManager.isPresent()) {
            throw new Exception("Manager not found!!!");
        }
        MyUser managerObj = tempManager.get();
        if(!managerObj.getFolderList().contains(folderId))
        {
            throw new Exception("Manager does not have access to this folder!!!");
        }

        // check if user is a designer
        Optional<MyUser> tempUser = userRepository.findById(userId);
        if (!tempUser.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser userObj = tempUser.get();
        if (!userObj.getRole().equals("DESIGNER")) {
            throw new Exception("Manager can unassign folders to only Designers!!!");
        }

        // remove folder id to user's folder list
        Set<String> newFolderList = userObj.getFolderList();
        newFolderList.remove(folderId);
        userObj.setFolderList(newFolderList);
        userRepository.save(userObj);
    }
}
