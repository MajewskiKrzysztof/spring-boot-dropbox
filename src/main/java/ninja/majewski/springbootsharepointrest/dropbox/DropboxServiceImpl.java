package ninja.majewski.springbootsharepointrest.dropbox;

import com.dropbox.core.v2.DbxClientV2;
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

    private DbxClientV2 client;

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
    public FolderMetadata getFolderDetails(String folderPath) throws DropboxException {
        return getMetadata(folderPath, FolderMetadata.class, String.format("%s is not a folder", folderPath));
    }

    @Override
    public FileMetadata getFileDetails(String filePath) throws DropboxException {
        return getMetadata(filePath, FileMetadata.class, String.format("%s is not a file", filePath));
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
    public ListFolderResult listFolderContinue(String cursorId) throws DropboxException {
        return handleDropboxAction(() -> client.files().listFolderContinue(cursorId), "Error listing folder");
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

    private <T> T handleDropboxAction(DropboxActionSupplier<T> action, String exceptionMessage) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new DropboxException(exceptionMessage, e);
        }
    }
}
