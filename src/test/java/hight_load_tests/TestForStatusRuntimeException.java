package hight_load_tests;

import ocrme_backend.ocr.OCRProcessorImplTest;
import ocrme_backend.translate.TranslatorImplTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Repeat;

/**
 * Created by iuliia on 6/23/17.
 */
public class TestForStatusRuntimeException {
    //todo
    //com.google.api.gax.grpc.ApiException: io.grpc.StatusRuntimeException:
    // RESOURCE_EXHAUSTED: Insufficient tokens for quota 'DefaultGroup' and limit 'USER-100s'
    // of service 'vision.googleapis.com' for consumer 'project_number:764086051850'.

    //call ocr many times to reproduce

    //error occurs because of limits https://cloud.google.com/vision/docs/limits
    //Requests per second	10

    //how to make it works
    //https://developers.google.com/analytics/devguides/config/provisioning/v3/limits-quotas


    @Before
    public void init() {

    }

    @Test
    @Repeat(1)
    public void loadTestOcr() throws Exception {

    }

    @Test
    @Repeat(15)
    public void loadTestTranslate() throws Exception {

    }
}
