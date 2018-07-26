package ninja.majewski.springbootsharepointrest.dropbox;


import ninja.majewski.springbootsharepointrest.SpringBootSharepointRestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootSharepointRestApplication.class)
public class DropboxIntTest {

    @Test
    public void downloadFile_shouldReturnNotEmptyInputStream() {

    }

    @Test
    public void downloadFile_shouldThrowExceptionIfFileNotExists() {

    }

    @Test
    public void uploadFile_shouldReturnUploadedFileDetails() {

    }

    @Test
    public void uploadFile_shouldThrowExceptionIfFolderNotExists() {

    }

    @Test
    public void getFolderDetails_shouldReturnFolderDetails() {

    }

    @Test
    public void getFolderDetails_shouldThrowExceptionIfFolderNotExists() {

    }

    @Test
    public void getFileDetails_shouldReturnFileDetails() {

    }

    @Test
    public void getFileDetails_shouldThrowExceptionIfFileNotExists() {

    }

    // todo recursive
    // todo limit
    @Test
    public void listFolder_shouldReturnFolderItems() {
    }

}
