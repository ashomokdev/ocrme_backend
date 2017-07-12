package ocrme_backend.servlets.ocr;

import ocrme_backend.datastore.utils.FileProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by iuliia on 7/3/17.
 */
public class OcrCallableTaskTest {
    @Test
    public void testCall() throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(2);
        Future<String> result = service.submit(new OcrCallableTask(FileProvider.getImageFile().getFile(), null));
        Assert.assertTrue(result.get() != null);
        Assert.assertTrue(result.get().length() > 0);
        service.shutdown();
    }
}