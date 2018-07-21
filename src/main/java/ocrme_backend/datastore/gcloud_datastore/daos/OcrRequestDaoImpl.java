package ocrme_backend.datastore.gcloud_datastore.daos;

import com.google.appengine.api.datastore.*;
import ocrme_backend.datastore.gcloud_datastore.objects.OcrRequest;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import ocrme_backend.servlets.ocr.OcrResponse;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 6/21/17.
 */
public class OcrRequestDaoImpl implements OcrRequestDao {
    public static final int requestCountLimit = 12; //divided by 2 and 3 - good for small screens (2 per row)
    private static final String OCR_REQUEST_KIND = "OcrRequest";
    private static Logger logger;
    private DatastoreService datastore;
    // and tablet screens (3 per row)

    public OcrRequestDaoImpl() {
        datastore = DatastoreServiceFactory.getDatastoreService(); // Authorized Datastore service
        logger = Logger.getLogger(OcrRequestDaoImpl.class.getName());
    }

    public OcrRequest entityToOCRRequest(Entity entity) {
        return new OcrRequest.Builder()
                .id(entity.getKey().getId())
                .languages((List<String>) entity.getProperty(OcrRequest.LANGUAGES))
                .sourceImageUrl((String) entity.getProperty(OcrRequest.SOURCE_IMAGE_URL))
                .textResult(Optional.ofNullable(((Text) entity.getProperty(OcrRequest.TEXT_RESULT)).getValue()))
                .pdfResultGsUrl((String) entity.getProperty(OcrRequest.PDF_RESULT_GS_URL))
                .pdfResultMediaUrl((String) entity.getProperty(OcrRequest.PDF_RESULT_MEDIA_URL))
                .pdfImageResultGsUrl((String) entity.getProperty(OcrRequest.PDF_IMAGE_RESULT_GS_URL))
                .pdfImageResultMediaUrl((String) entity.getProperty(OcrRequest.PDF_IMAGE_RESULT_MEDIA_URL))
                .createdBy((String) entity.getProperty(OcrRequest.CREATED_BY))
                .createdById((String) entity.getProperty(OcrRequest.CREATED_BY_ID))
                .timeStamp((String) entity.getProperty(OcrRequest.TIME_STAMP))
                .status((String) entity.getProperty(OcrRequest.STATUS))
                .build();
    }

    @Override
    public Long create(OcrRequest request) {
        Entity entity = new Entity(OCR_REQUEST_KIND);  // Key will be assigned once written
        entity.setProperty(OcrRequest.SOURCE_IMAGE_URL, request.getSourceImageUrl());
        entity.setProperty(OcrRequest.TEXT_RESULT, request.getTextResult());
        entity.setProperty(OcrRequest.PDF_RESULT_GS_URL, request.getPdfResultGsUrl());
        entity.setProperty(OcrRequest.PDF_RESULT_MEDIA_URL, request.getPdfResultMediaUrl());
        entity.setProperty(OcrRequest.PDF_IMAGE_RESULT_GS_URL, request.getPdfImageResultGsUrl());
        entity.setProperty(OcrRequest.PDF_IMAGE_RESULT_MEDIA_URL, request.getPdfImageResultMediaUrl());
        entity.setProperty(OcrRequest.CREATED_BY, request.getCreatedBy());
        entity.setProperty(OcrRequest.CREATED_BY_ID, request.getCreatedById());
        entity.setProperty(OcrRequest.TIME_STAMP, request.getTimeStamp());
        entity.setProperty(OcrRequest.STATUS, request.getStatus());
        entity.setProperty(OcrRequest.LANGUAGES, request.getLanguages());

        Key ocrRequestKey = datastore.put(entity); // Save the Entity

        logger.log(Level.INFO, "ocrRequest \n{0} \ncreated", request);
        return ocrRequestKey.getId();                     // The ID of the Key
    }

    @Override
    public OcrRequest read(Long OCRRequestId) {
        try {
            Entity OCRRequestEntity = datastore.get(KeyFactory.createKey(OCR_REQUEST_KIND, OCRRequestId));
            return entityToOCRRequest(OCRRequestEntity);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public void update(OcrRequest request) {
        Key key = KeyFactory.createKey(OCR_REQUEST_KIND, request.getId());  // From a OcrRequest, create a Key
        Entity entity = new Entity(key);         // Convert OcrRequest to an Entity
        entity.setProperty(OcrRequest.SOURCE_IMAGE_URL, request.getSourceImageUrl());
        entity.setProperty(OcrRequest.TEXT_RESULT, request.getTextResult());
        entity.setProperty(OcrRequest.PDF_RESULT_GS_URL, request.getPdfResultGsUrl());
        entity.setProperty(OcrRequest.PDF_RESULT_MEDIA_URL, request.getPdfResultMediaUrl());
        entity.setProperty(OcrRequest.PDF_IMAGE_RESULT_GS_URL, request.getPdfImageResultGsUrl());
        entity.setProperty(OcrRequest.PDF_IMAGE_RESULT_MEDIA_URL, request.getPdfImageResultMediaUrl());
        entity.setProperty(OcrRequest.CREATED_BY, request.getCreatedBy());
        entity.setProperty(OcrRequest.CREATED_BY_ID, request.getCreatedById());
        entity.setProperty(OcrRequest.TIME_STAMP, request.getTimeStamp());
        entity.setProperty(OcrRequest.STATUS, request.getStatus());
        entity.setProperty(OcrRequest.LANGUAGES, request.getLanguages());

        datastore.put(entity);                   // Update the Entity
        logger.log(Level.INFO, "ocrRequest \n{0} \nupdated", request);

    }


    @Override
    public void delete(Long requestId) {
        Key key = KeyFactory.createKey(OCR_REQUEST_KIND, requestId);        // Create the Key
        datastore.delete(key);                      // Delete the Entity
        logger.log(Level.INFO, "ocrRequest \n{0} \ndeleted", requestId);
    }

    @Override
    public void delete(List<Long> ocrRequestIds) {
        List<Key> keys = new ArrayList<>();
        for (Long id : ocrRequestIds) {
            logger.log(Level.INFO, "ocrRequest \n{0} \n will be deleted", id);
            keys.add(KeyFactory.createKey(OCR_REQUEST_KIND, id));
        }
        datastore.delete(keys);

    }

    public List<OcrRequest> entitiesToOCRRequests(Iterator<Entity> results) {
        List<OcrRequest> resultOcrRequests = new ArrayList<>();
        while (results.hasNext()) {  // We still have data
            resultOcrRequests.add(entityToOCRRequest(results.next()));      // Add the OcrRequest to the List
        }
        return resultOcrRequests;
    }

    @Override
    public Result<OcrRequest> listOCRRequests(String startCursorString) {
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(requestCountLimit); // Only show requestCountLimit at a time
        if (startCursorString != null && !startCursorString.equals("")) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursorString)); // Where we left off
        }
        Query query = new Query(OCR_REQUEST_KIND) // We only care about ocr Requests
                .addSort(OcrRequest.TIME_STAMP, Query.SortDirection.ASCENDING); // Use default Index "time span"
        PreparedQuery preparedQuery = datastore.prepare(query);
        QueryResultIterator<Entity> results = preparedQuery.asQueryResultIterator(fetchOptions);

        List<OcrRequest> ocrRequests = entitiesToOCRRequests(results);     // Retrieve and convert Entities
        Cursor cursor = results.getCursor();              // Where to start next time
        if (cursor != null && ocrRequests.size() == requestCountLimit) {         // Are we paging? Save Cursor
            String cursorString = cursor.toWebSafeString();               // Cursors are WebSafe
            return new Result<>(ocrRequests, cursorString);
        } else {
            return new Result<>(ocrRequests);
        }
    }

    /**
     * get requests by user with OK status only
     *
     * @param userId
     * @param startCursorString
     * @return
     */
    @Override
    public Result<OcrRequest> listOCRRequestsByUser(String userId, String startCursorString) {
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(requestCountLimit); // Only show requestCountLimit at a time
        if (startCursorString != null && !startCursorString.equals("")) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursorString)); // Where we left off
            logger.log(Level.INFO, "start cursor: " + startCursorString);
        }

        Query.FilterPredicate userIdFilter = new Query.FilterPredicate(
                OcrRequest.CREATED_BY_ID, Query.FilterOperator.EQUAL, userId);
        Query.FilterPredicate okStatusFilter = new Query.FilterPredicate(
                OcrRequest.STATUS, Query.FilterOperator.EQUAL, OcrResponse.Status.OK.name());
        Query.CompositeFilter compositeFilter =
                new Query.CompositeFilter(
                        Query.CompositeFilterOperator.AND, Arrays.asList(userIdFilter, okStatusFilter));

        Query query = new Query(OCR_REQUEST_KIND) // We only care about OCRRequests
                // Only for this user
                .setFilter(compositeFilter)
                // a custom datastore index is required since you are filtering by one property
                // but ordering by another
                .addSort(OcrRequest.TIME_STAMP, Query.SortDirection.DESCENDING);
        PreparedQuery preparedQuery = datastore.prepare(query);
        QueryResultIterator<Entity> results = preparedQuery.asQueryResultIterator(fetchOptions);

        List<OcrRequest> resultOcrRequests = entitiesToOCRRequests(results);     // Retrieve and convert Entities
        Cursor cursor = results.getCursor();              // Where to start next time
        if (cursor != null && resultOcrRequests.size() == requestCountLimit) {         // Are we paging? Save Cursor
            String cursorString = cursor.toWebSafeString();               // Cursors are WebSafe
            logger.log(Level.INFO, "end cursor: " + cursorString);
            return new Result<>(resultOcrRequests, cursorString);
        } else {
            return new Result<>(resultOcrRequests);
        }
    }
}
