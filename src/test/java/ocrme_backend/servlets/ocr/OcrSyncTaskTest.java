package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by iuliia on 7/3/17.
 */
public class OcrSyncTaskTest {
    @Test
    public void testExecute() throws Exception {

        OcrData result = new OcrSyncTask(FileProvider.getImageFile().getImageBytes(), null).execute();
        Assert.assertTrue(result != null);
        Assert.assertTrue(result.getPdfBuilderInputData().getText().size() > 0);
    }
}