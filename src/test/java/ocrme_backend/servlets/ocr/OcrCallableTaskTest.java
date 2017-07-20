package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import ocrme_backend.file_builder.pdfbuilder.PdfBuilderInputData;
import org.apache.commons.fileupload.FileItemIterator;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by iuliia on 7/3/17.
 */
public class OcrCallableTaskTest {
    @Test
    public void testCall() throws Exception {
        FileItemIterator mockFileItemIterator = mock(FileItemIterator.class);
        when(mockFileItemIterator.next()).thenReturn(FileProvider.getItemStreamFile());
        when(mockFileItemIterator.hasNext()).thenReturn(true).thenReturn(false);

        ExecutorService service = Executors.newFixedThreadPool(2);
        Future<PdfBuilderInputData> result = service.submit(
                new OcrCallableTask(mockFileItemIterator, null));
        Assert.assertTrue(result.get() != null);
        Assert.assertTrue(result.get().getText().size() > 0);
        service.shutdown();
    }
}