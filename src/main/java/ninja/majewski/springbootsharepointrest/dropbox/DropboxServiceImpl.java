package ninja.majewski.springbootsharepointrest.dropbox;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import java.io.InputStream;
import java.util.Objects;
import ninja.majewski.springbootsharepointrest.dropbox.exception.DropboxException;
import org.springframework.stereotype.Service;

@Service
class DropboxServiceImpl implements DropboxService {

    private final DbxClientV2 client;

    public DropboxServiceImpl(DbxClientV2 client) {
        this.client = client;
    }

    @Override
    public InputStream downloadFile(String filePath) throws DropboxException {
        return handleDropboxAction(() -> client.files().download(filePath).getInputStream(),
                String.format("Error downloading file: %s", filePath));
    }

    @Override
    public FileMetadata uploadFile(String filePath, InputStream fileStream) throws DropboxException {
        return handleDropboxAction(() -> client.files().uploadBuilder(filePath).uploadAndFinish(fileStream),
                String.format("Error uploading file: %s", filePath));
    }

    @Override
    public CreateFolderResult createFolder(String folderPath) {
        return handleDropboxAction(() -> client.files().createFolderV2(folderPath), "Error creating folder");
    }

    @Override
    public FolderMetadata getFolderDetails(String folderPath) throws DropboxException {
        return getMetadata(folderPath, FolderMetadata.class, String.format("Error getting folder details: %s", folderPath));
    }

    @Override
    public FileMetadata getFileDetails(String filePath) throws DropboxException {
        return getMetadata(filePath, FileMetadata.class, String.format("Error getting file details: %s", filePath));
    }

    @Override
    public ListFolderResult listFolder(String folderPath, Boolean recursiveListing, Long limit) throws DropboxException {
        ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder(folderPath);
        if (Objects.nonNull(recursiveListing)) {
            listFolderBuilder.withRecursive(recursiveListing);
        }
        if (Objects.nonNull(limit)) {
            listFolderBuilder.withLimit(limit);
        }

        return handleDropboxAction(listFolderBuilder::start, String.format("Error listing folder: %s", folderPath));
    }

    @Override
    public ListFolderResult listFolderContinue(String cursor) throws DropboxException {
        return handleDropboxAction(() -> client.files().listFolderContinue(cursor), "Error listing folder");
    }

    @Override
    public void deleteFile(String filePath) {
        handleDropboxAction(() -> client.files().deleteV2(filePath), String.format("Error deleting file: %s", filePath));
    }

    @Override
    public void deleteFolder(String folderPath) {
        handleDropboxAction(() -> client.files().deleteV2(folderPath), String.format("Error deleting folder: %s", folderPath));
    }

    private <T> T handleDropboxAction(DropboxActionResolver<T> action, String exceptionMessage) {
        try {
            return action.perform();
        } catch (Exception e) {
            String messageWithCause = String.format("%s with cause: %s", exceptionMessage, e.getMessage());
            throw new DropboxException(messageWithCause, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getMetadata(String path, Class<T> type, String message) {
        Metadata metadata = handleDropboxAction(() -> client.files().getMetadata(path),
                String.format("Error accessing details of: %s", path));

        checkIfMetadataIsInstanceOfGivenType(metadata, type, message);
        return (T) metadata;
    }

    private <T> void checkIfMetadataIsInstanceOfGivenType(Metadata metadata, Class<T> validType, String exceptionMessage) {
        boolean isValidType = validType.isInstance(metadata);
        if (!isValidType) {
            throw new DropboxException(exceptionMessage);
        }
    }
}
