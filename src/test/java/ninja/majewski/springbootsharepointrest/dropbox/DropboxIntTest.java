package ninja.majewski.springbootsharepointrest.dropbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import ninja.majewski.springbootsharepointrest.SpringBootSharepointRestApplication;
import ninja.majewski.springbootsharepointrest.dropbox.exception.DropboxException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootSharepointRestApplication.class)
public class DropboxIntTest {

    private static final String TEST_FOLDER_PATH = "/Test Folder";
    private static final String TEST_FILE_PATH = String.format("%s/%s", TEST_FOLDER_PATH, "testFile.txt");
    private static final Integer TEST_FILE_SIZE = 17;

    @Autowired
    private DropboxService dropboxService;

    @Rule
    public final ExpectedException exceptions = ExpectedException.none();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void createTestFolder() throws Exception {
        dropboxService.createFolder(TEST_FOLDER_PATH);

        File tempUploadFile = temporaryFolder.newFile("testFile.txt");
        FileUtils.writeStringToFile(tempUploadFile, "test file content", "UTF-8");
        String testFilePath = String.format("%s/%s", TEST_FOLDER_PATH, "testFile.txt");
        dropboxService.uploadFile(testFilePath, new FileInputStream(tempUploadFile));
    }

    @After
    public void deleteTestFolder() {
        dropboxService.deleteFolder(TEST_FOLDER_PATH);
    }

    @Test
    public void downloadFile_shouldReturnNotEmptyInputStream() throws Exception {
        InputStream inputStream = dropboxService.downloadFile(TEST_FILE_PATH);
        assertThat(inputStream.available()).isEqualTo(TEST_FILE_SIZE);
    }

    @Test
    public void downloadFile_shouldThrowExceptionIfFileNotExists() {
        exceptions.expect(DropboxException.class);
        dropboxService.downloadFile("not-existing-file");
    }

    @Test
    public void uploadFile_shouldReturnUploadedFileDetails() throws Exception {
        File tempUploadFile = temporaryFolder.newFile("teamLogo.png");
        String filePath = String.format("%s/%s", TEST_FOLDER_PATH, tempUploadFile.getName());
        FileMetadata fileMetadata = dropboxService.uploadFile(filePath, new FileInputStream(tempUploadFile));
        assertThat(fileMetadata.getId()).isNotBlank();
    }

    @Test
    public void uploadFile_shouldCreateFolderIfNotExists() throws Exception {
        File tempUploadFile = temporaryFolder.newFile("teamLogo.png");
        String filePath = String.format("%s/%s/%s", TEST_FOLDER_PATH, "not existing folder", tempUploadFile.getName());
        dropboxService.uploadFile(filePath, new FileInputStream(tempUploadFile));

        FolderMetadata folderDetails = dropboxService
                .getFolderDetails(String.format("%s/%s", TEST_FOLDER_PATH, "not existing folder"));
        assertThat(folderDetails.getId()).isNotBlank();
    }

    @Test
    public void createFolder_shouldCreateFolder() {
        String folderPath = String.format("%s/%s", TEST_FOLDER_PATH, "new folder");
        CreateFolderResult folder = dropboxService.createFolder(folderPath);

        FolderMetadata folderDetails = dropboxService.getFolderDetails(folderPath);
        assertThat(folderDetails.getId()).isNotBlank();
        assertThat(folderDetails.getId()).isEqualToIgnoringCase(folder.getMetadata().getId());
    }

    @Test
    public void createFolder_shouldThrowExceptionIfFolderAlreadyExists() {
        String folderPath = String.format("%s/%s", TEST_FOLDER_PATH, "new folder");

        dropboxService.createFolder(folderPath);

        exceptions.expect(DropboxException.class);
        dropboxService.createFolder(folderPath);
    }

    @Test
    public void getFolderDetails_shouldReturnFolderDetails() {
        FolderMetadata folderDetails = dropboxService.getFolderDetails(TEST_FOLDER_PATH);

        assertThat(folderDetails.getId()).isNotBlank();
        assertThat(folderDetails.getName()).isNotBlank();
        assertThat(folderDetails.getPathDisplay()).isNotBlank();
    }

    @Test
    public void getFolderDetails_shouldThrowExceptionIfFolderNotExists() {
        exceptions.expect(DropboxException.class);
        dropboxService.getFolderDetails("/not existing folder");
    }

    @Test
    public void getFileDetails_shouldReturnFileDetails() {
        FileMetadata fileDetails = dropboxService.getFileDetails(TEST_FILE_PATH);

        assertThat(fileDetails.getId()).isNotBlank();
        assertThat(fileDetails.getPathDisplay()).isNotBlank();
        assertThat(fileDetails.getName()).isNotBlank();
        assertThat(fileDetails.getSize()).isEqualTo(TEST_FILE_SIZE.longValue());
        assertThat(fileDetails.getServerModified()).isNotNull();
        assertThat(fileDetails.getServerModified()).isBefore(new Date());
    }

    @Test
    public void getFileDetails_shouldThrowExceptionIfFileNotExists() {
        exceptions.expect(DropboxException.class);
        dropboxService.getFileDetails("/not-existing-file.pdf");
    }

    @Test
    public void listFolder_shouldReturnFolderItems() throws Exception {
        File tempUploadFile1 = temporaryFolder.newFile("testFile2.txt");
        FileUtils.writeStringToFile(tempUploadFile1, "test file content", "UTF-8");
        String testFilePath1 = String.format("%s/%s", TEST_FOLDER_PATH, "testFile2.txt");
        dropboxService.uploadFile(testFilePath1, new FileInputStream(tempUploadFile1));

        File tempUploadFile2 = temporaryFolder.newFile("testFile3.txt");
        FileUtils.writeStringToFile(tempUploadFile2, "test file content", "UTF-8");
        String testFilePath2 = String.format("%s/%s/%s", TEST_FOLDER_PATH, "inner folder", "testFile3.txt");
        dropboxService.uploadFile(testFilePath2, new FileInputStream(tempUploadFile2));

        ListFolderResult listFolderResult = dropboxService.listFolder(TEST_FOLDER_PATH, true, 10L);
        assertThat(listFolderResult.getEntries()).hasSize(5);

        List<FileMetadata> files = listFolderResult.getEntries().stream()
                .filter(entity -> entity instanceof FileMetadata)
                .map(entity -> (FileMetadata) entity)
                .collect(Collectors.toList());
        assertThat(files).hasSize(3);

        List<FolderMetadata> folders = listFolderResult.getEntries().stream()
                .filter(entity -> entity instanceof FolderMetadata)
                .map(entity -> (FolderMetadata) entity)
                .collect(Collectors.toList());
        assertThat(folders).hasSize(2);
    }

    @Test
    public void listFolder_shouldTrowExceptionIfFolderNotExists() {
        exceptions.expect(DropboxException.class);
        dropboxService.listFolder("/not existing folder", true, 10L);
    }

    @Test
    public void listFolderContinue_shouldListNextPathOfItems() throws Exception {
        File tempUploadFile = temporaryFolder.newFile("testFile2.txt");
        FileUtils.writeStringToFile(tempUploadFile, "test file content", "UTF-8");
        String testFilePath1 = String.format("%s/%s", TEST_FOLDER_PATH, "testFile2.txt");
        dropboxService.uploadFile(testFilePath1, new FileInputStream(tempUploadFile));

        ListFolderResult listFolderResult = dropboxService.listFolder(TEST_FOLDER_PATH, false, 1L);
        assertThat(listFolderResult.getEntries()).hasSize(1);

        String cursor = listFolderResult.getCursor();
        listFolderResult = dropboxService.listFolderContinue(cursor);
        assertThat(listFolderResult.getEntries()).hasSize(1);

        cursor = listFolderResult.getCursor();
        listFolderResult = dropboxService.listFolderContinue(cursor);
        assertThat(listFolderResult.getEntries()).hasSize(0);
    }

    @Test
    public void listFolderContinue_shouldThrowExceptionIfWrongCursorProvided() {
        exceptions.expect(DropboxException.class);
        dropboxService.listFolderContinue(UUID.randomUUID().toString());
    }

    @Test
    public void deleteFile_shouldDeleteFile() {
        FileMetadata fileDetails = dropboxService.getFileDetails(TEST_FILE_PATH);
        assertThat(fileDetails.getId()).isNotBlank();

        dropboxService.deleteFile(TEST_FILE_PATH);

        exceptions.expect(DropboxException.class);
        dropboxService.getFileDetails(TEST_FILE_PATH);
    }

    @Test
    public void deleteFile_shouldThrowExceptionIfFileNotExists() {
        exceptions.expect(DropboxException.class);
        dropboxService.deleteFolder("/not-existing-file");
    }

    @Test
    public void deleteFolder_shouldDeleteFolder() {
        String testFolder = String.format("%s/%s", TEST_FOLDER_PATH, "test folder");
        dropboxService.createFolder(testFolder);
        
        FolderMetadata folderDetails = dropboxService.getFolderDetails(testFolder);
        assertThat(folderDetails.getId()).isNotBlank();
        
        dropboxService.deleteFolder(testFolder);

        exceptions.expect(DropboxException.class);
        dropboxService.getFolderDetails(testFolder);
    }

    @Test
    public void deleteFolder_shouldThrowExceptionIfFolderNotExists() {
        exceptions.expect(DropboxException.class);
        dropboxService.deleteFolder("/not-existing-folder");
    }

}
