package com.syndicate.fileshare.service;

import com.syndicate.fileshare.entity.FileData;
import com.syndicate.fileshare.entity.FolderData;
import com.syndicate.fileshare.entity.MyUser;
import com.syndicate.fileshare.repository.FileRepository;
import com.syndicate.fileshare.repository.FolderRepository;
import com.syndicate.fileshare.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;

import java.util.*;

@Service
public class AdminService {

    @Autowired
    private AwsService awsService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(FileShareService.class);

    public List<FolderData> getAllFolders() {
        List<FolderData> folderDataList = folderRepository.findAll();
        return folderDataList;
    }

    public List<FileData> getAllFiles() {
        List<FileData> fileDataList = fileRepository.findAll();
        return fileDataList;
    }

    public List<FileData> getFilesByFolderAdmin(String folderId) throws Exception {
        Optional<FolderData> tempFolder = folderRepository.findById(folderId);
        if(!tempFolder.isPresent())
            throw new Exception("Folder not found in the DB!!!");
        List<FileData> fileDataList = new ArrayList<>();
        if(tempFolder.get().getFilesList().isEmpty())
            return fileDataList;
        for (String fileId:tempFolder.get().getFilesList())
        {
            Optional<FileData> tempFile = fileRepository.findById(fileId);
            if(!tempFile.isPresent())
                continue;
            fileDataList.add(tempFile.get());
        }
        return fileDataList;
    }

    public byte[] downloadFileAdmin(String fileId) throws Exception {
        Optional<FileData> tempFile = fileRepository.findById(fileId);
        if(!tempFile.isPresent()) {
            throw new Exception("File not Found!!!");
        }
        FileData fileObj = tempFile.get();
        // get folder data
        Optional<FolderData> tempFolder = folderRepository.findById(fileObj.getFolderId());
        if(!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();

        String fileName = folderObj.getFolderName() + "/" + fileObj.getFileName();
        byte[] file = awsService.downloadFileFromS3(fileName);
        return file;
    }

    public Object uploadFileAdmin(String currUsername, String folderId, MultipartFile file) throws Exception {
        // check if folder exists
        Optional<FolderData> tempFolder = folderRepository.findById(folderId);
        if(!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();

        // check if file name already exists
        Optional<FileData> tempFile = fileRepository.findByFileName(file.getOriginalFilename());
        if(tempFile.isPresent()) {
            throw new Exception("File name already exists!!!");
        }

        // upload to s3
        awsService.uploadFileInS3(currUsername, folderObj.getFolderName(), file);
        // create new fileData
        FileData newFile = new FileData();
        newFile.setFileName(file.getOriginalFilename());
        newFile.setFolderId(folderObj.getId());
        newFile.setCreator(currUsername);
        fileRepository.save(newFile);   // save fileData to DB
        LOGGER.info("New file Info: "+newFile.toString());
        // add to fileList
        Set<String> files = folderObj.getFilesList();
        if (files == null) {
            files = new HashSet<>();  // Initialize if null
        }
        files.add(newFile.getId());
        folderObj.setFilesList(files);
        // Save the updated folder back to the database
        return folderRepository.save(folderObj);
    }

    public String deleteFileAdmin(String fileId) throws Exception {
        Optional<FileData> tempFile = fileRepository.findById(fileId);
        if(!tempFile.isPresent()) {
            throw new Exception("File not Found!!!");
        }
        FileData fileObj = tempFile.get();

        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(fileObj.getFolderId());
        if(!tempFolder.isPresent()) {
            throw new Exception("Folder not found in the DB!!!");
        }
        FolderData folderObj = tempFolder.get();

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
        return fileName + " removed ...";
    }

    public void renameFileAdmin(String fileId, String newFileName) throws Exception {
        Optional<FileData> tempFile = fileRepository.findById(fileId);
        if(!tempFile.isPresent()) {
            throw new Exception("File not Found!!!");
        }
        FileData fileObj = tempFile.get();

        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(fileObj.getFolderId());
        if(!tempFolder.isPresent()) {
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

    public void createFolderAdmin(String managerId, String folderName) throws Exception {
        Optional<MyUser> tempManager = userRepository.findById(managerId);
        if (!tempManager.isPresent()) {
            throw new Exception("User not found!!!");
        }
        MyUser managerObj = tempManager.get();

        if(!managerObj.getRole().equals("MANAGER"))
        {
            throw new Exception("The given user is not a Manager!!!");
        }
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
    }

    public void renameFolderAdmin(String folderId, String newFolderName) throws Exception {
        // get folder details
        Optional<FolderData> tempFolder = folderRepository.findById(folderId);
        if(!tempFolder.isPresent()) {
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

    public void deleteFolderAdmin(String folderId) throws Exception {
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
}
