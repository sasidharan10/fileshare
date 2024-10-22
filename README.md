# File Share App

A file sharing application developed using Spring Boot and MongoDB. The application allows designers to upload their designs to the website. User roles determine the level of access and management capabilities for files and folders.

# Technologies Used

- **Backend Framework:** Spring Boot
- **Database:** MongoDB
- **Cloud Storage:** Amazon S3

# Features

1. User authentication and authorization
2. Designers can upload and download files.
3. Admin dashboard for managing files and users.


# User Roles:
  - Admin: Can create, edit, delete, and manage all files and folders.
  - Manager: Can create, edit, delete, and manage files and folders within their assigned scope.
  - Designer: Can upload, download, and view files within their assigned scope.

# API Endpoints

### UserController

| Endpoint        | Method |    Description    | Access |
| :-------------- | :----: | :---------------: | :----: |
| `/` |  GET| Home Page | Public |
| `/folder/myFolders`  |  GET|   Retrieves folders owned by the current user   | Designer, Manager |
| `/folder/{folderId}/files` |  GET| Fetches files in the specified folder. | Designer, Manager |
| `/file/myFiles` |  GET| Retrieves files owned by the current user. | Designer, Manager |
| `/file/upload` |  POST| Uploads a file to the specified folder. | Designer, Manager |
| `/download/file/{fileId}` |  GET| Downloads a file by its ID. | Designer, Manager |
| `/delete/file/{fileId}` |  DELETE| Deletes a file by its ID. | Manager |
| `/file/edit` |  PUT| Renames a file by its ID | Manager |
| `/folder/create` |  POST| Creates a new folder | Manager |
| `/folder/edit` |  PUT| Renames a folder by its ID | Manager |
| `/folder/delete/{folderId}` |  DELETE| Deletes a folder by its ID | Manager |
| `/folder/assign` |  PUT| Assigns a folder to a Designer | Manager |
| `/folder/unassign` |  PUT| Unassigns a folder from a Designer | Manager |



### AdminController

| Endpoint        | Method |    Description    | Access |
| :-------------- | :----: | :---------------: | :----: |
| `/admin/user/add` |  POST| Adds a new user to the database | Admin |
| `/admin/user/remove` |  DELETE| Removes a user by their ID. | Admin |
| `/admin/folder/allFolders` |  GET| Retrieves all folders in the system. | Admin |
| `/admin/folder/{folderId}/files` |  GET| Retrieves files in a specific folder. | Admin |
| `/admin/file/allFiles` |  GET| Retrieves all files in the system | Admin |
| `/admin/file/upload` |  POST| Uploads a file to a folder. | Admin |
| `/admin/file/download/{fileId}` |  GET| Downloads a file by its ID. | Admin |
| `/admin/file/delete/{fileId}` |  DELETE| Deletes a file by its ID. | Admin |
| `/admin/file/edit` |  PUT| Renames a file by its ID | Admin |
| `/admin/folder/create` |  POST| Creates a new folder. | Admin |
| `/admin/folder/edit` |  PUT| Renames a folder by its ID. | Admin |
| `/admin/folder/delete/{folderId}` |  DELETE| Deletes a folder by its ID. | Admin |
| `/admin/folder/assign` |  PUT| Assigns a folder to a user. | Admin |
| `/admin/folder/unassign` |  PUT| Unassigns a folder from a user. | Admin |
