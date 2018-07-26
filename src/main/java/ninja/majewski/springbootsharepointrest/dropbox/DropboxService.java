package ninja.majewski.springbootsharepointrest.dropbox;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import java.io.InputStream;
import ninja.majewski.springbootsharepointrest.dropbox.exception.DropboxException;

public interface DropboxService {

    InputStream downloadFile(String filePath) throws DropboxException;

    FileMetadata uploadFile(String filePath, InputStream fileStream) throws DropboxException;

    FolderMetadata getFolderDetails(String folderPath) throws DropboxException;

    FileMetadata getFileDetails(String filePath) throws DropboxException;

    ListFolderResult listFolder(String folderPath, Boolean recursiveListing, Long limit) throws DropboxException;

    ListFolderResult listFolderContinue(String cursorId) throws DropboxException;
}
