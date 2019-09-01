package ocrme_backend.datastore.gcloud_datastore.daos;

import com.google.appengine.api.datastore.*;
import ocrme_backend.datastore.gcloud_datastore.objects.Result;
import ocrme_backend.datastore.gcloud_datastore.objects.TranslateRequest;
import ocrme_backend.servlets.translate.translate.TranslateResponse;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by iuliia on 6/21/17.
 */
public class TranslateRequestDaoImpl implements TranslateRequestDao {
    public static final int requestCountLimit = 12; //divided by 2 and 3 - good for small screens (2 per row) and tablet screens (3 per row)
    private static final String TRANSLATE_REQUEST_KIND = "TranslateRequest";
    private static Logger logger;
    private DatastoreService datastore;

    public TranslateRequestDaoImpl() {
        datastore = DatastoreServiceFactory.getDatastoreService(); // Authorized Datastore service
        logger = Logger.getLogger(TranslateRequestDaoImpl.class.getName());
    }

    public TranslateRequest entityToTranslateRequest(Entity entity) {
        return new TranslateRequest.Builder()
                .id(entity.getKey().getId())
                .sourceLanguageCode((String) entity.getProperty(TranslateRequest.SOURCE_LANGUAGE_CODE))
                .targetLanguageCode((String) entity.getProperty(TranslateRequest.TARGET_LANGUAGE_CODE))
                .sourceText(Optional.ofNullable(((Text) entity.getProperty(TranslateRequest.SOURCE_TEXT)).getValue()))
                .targetText(Optional.ofNullable(((Text) entity.getProperty(TranslateRequest.TARGET_TEXT)).getValue()))
                .createdBy((String) entity.getProperty(TranslateRequest.CREATED_BY))
                .createdById((String) entity.getProperty(TranslateRequest.CREATED_BY_ID))
                .timeStamp((String) entity.getProperty(TranslateRequest.TIME_STAMP))
                .status((String) entity.getProperty(TranslateRequest.STATUS))
                .build();
    }

    @Override
    public Long create(TranslateRequest request) {
        Entity entity = new Entity(TRANSLATE_REQUEST_KIND);  // Key will be assigned once written
        entity.setProperty(TranslateRequest.SOURCE_LANGUAGE_CODE, request.getSourceLanguageCode());
        entity.setProperty(TranslateRequest.TARGET_LANGUAGE_CODE, request.getTargetLanguageCode());
        entity.setProperty(TranslateRequest.SOURCE_TEXT, request.getSourceText());
        entity.setProperty(TranslateRequest.TARGET_TEXT, request.getTargetText());
        entity.setProperty(TranslateRequest.CREATED_BY, request.getCreatedBy());
        entity.setProperty(TranslateRequest.CREATED_BY_ID, request.getCreatedById());
        entity.setProperty(TranslateRequest.TIME_STAMP, request.getTimeStamp());
        entity.setProperty(TranslateRequest.STATUS, request.getStatus());

        Key TranslateRequestKey = datastore.put(entity); // Save the Entity

        logger.log(Level.INFO, "TranslateRequest \n{0} \ncreated", request);
        return TranslateRequestKey.getId();                     // The ID of the Key
    }

    @Override
    public TranslateRequest read(Long TranslateRequestId) {
        try {
            Entity TranslateRequestEntity =
                    datastore.get(KeyFactory.createKey(TRANSLATE_REQUEST_KIND, TranslateRequestId));
            return entityToTranslateRequest(TranslateRequestEntity);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    @Override
    public void update(TranslateRequest request) {
        Key key = KeyFactory.createKey(TRANSLATE_REQUEST_KIND, request.getId());  // From a TranslateRequest, create a Key
        Entity entity = new Entity(key);         // Convert TranslateRequest to an Entity
        entity.setProperty(TranslateRequest.SOURCE_LANGUAGE_CODE, request.getSourceLanguageCode());
        entity.setProperty(TranslateRequest.TARGET_LANGUAGE_CODE, request.getTargetLanguageCode());
        entity.setProperty(TranslateRequest.SOURCE_TEXT, request.getSourceText());
        entity.setProperty(TranslateRequest.TARGET_TEXT, request.getTargetText());
        entity.setProperty(TranslateRequest.CREATED_BY, request.getCreatedBy());
        entity.setProperty(TranslateRequest.CREATED_BY_ID, request.getCreatedById());
        entity.setProperty(TranslateRequest.TIME_STAMP, request.getTimeStamp());
        entity.setProperty(TranslateRequest.STATUS, request.getStatus());
        datastore.put(entity);                   // Update the Entity
        logger.log(Level.INFO, "TranslateRequest \n{0} \nupdated", request);

    }


    @Override
    public void delete(Long requestId) {
        Key key = KeyFactory.createKey(TRANSLATE_REQUEST_KIND, requestId);        // Create the Key
        datastore.delete(key);                      // Delete the Entity
        logger.log(Level.INFO, "TranslateRequest \n{0} \ndeleted", requestId);
    }

    @Override
    public void delete(List<Long> TranslateRequestIds) {
        List<Key> keys = new ArrayList<>();
        for (Long id : TranslateRequestIds) {
            logger.log(Level.INFO, "TranslateRequest \n{0} \n will be deleted", id);
            keys.add(KeyFactory.createKey(TRANSLATE_REQUEST_KIND, id));
        }
        datastore.delete(keys);

    }

    public List<TranslateRequest> entitiesToTranslateRequests(Iterator<Entity> results) {
        List<TranslateRequest> resultTranslateRequests = new ArrayList<>();
        while (results.hasNext()) {  // We still have data
            resultTranslateRequests.add(entityToTranslateRequest(results.next()));      // Add the TranslateRequest to the List
        }
        return resultTranslateRequests;
    }

    @Override
    public Result<TranslateRequest> listTranslateRequests(String startCursorString) {
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(requestCountLimit); // Only show requestCountLimit at a time
        if (startCursorString != null && !startCursorString.equals("")) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursorString)); // Where we left off
        }
        Query query = new Query(TRANSLATE_REQUEST_KIND) // We only care about Books
                .addSort(TranslateRequest.TIME_STAMP, Query.SortDirection.ASCENDING); // Use default Index "time span"
        PreparedQuery preparedQuery = datastore.prepare(query);
        QueryResultIterator<Entity> results = preparedQuery.asQueryResultIterator(fetchOptions);

        List<TranslateRequest> resultBooks = entitiesToTranslateRequests(results);     // Retrieve and convert Entities
        Cursor cursor = results.getCursor();              // Where to start next time
        if (cursor != null && resultBooks.size() == requestCountLimit) {         // Are we paging? Save Cursor
            String cursorString = cursor.toWebSafeString();               // Cursors are WebSafe
            return new Result<>(resultBooks, cursorString);
        } else {
            return new Result<>(resultBooks);
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
    public Result<TranslateRequest> listTranslateRequestsByUser(String userId, String startCursorString) {
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(requestCountLimit); // Only show requestCountLimit at a time
        if (startCursorString != null && !startCursorString.equals("")) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursorString)); // Where we left off
            logger.log(Level.INFO, "start cursor: " + startCursorString);
        }

        Query.FilterPredicate userIdFilter = new Query.FilterPredicate(
                TranslateRequest.CREATED_BY_ID, Query.FilterOperator.EQUAL, userId);
        Query.FilterPredicate okStatusFilter = new Query.FilterPredicate(
                TranslateRequest.STATUS, Query.FilterOperator.EQUAL, TranslateResponse.Status.OK.name());
        Query.CompositeFilter compositeFilter =
                new Query.CompositeFilter(
                        Query.CompositeFilterOperator.AND, Arrays.asList(userIdFilter, okStatusFilter));

        Query query = new Query(TRANSLATE_REQUEST_KIND) // We only care about TranslateRequests
                // Only for this user
                .setFilter(compositeFilter)
                // a custom datastore index is required since you are filtering by one property
                // but ordering by another
                .addSort(TranslateRequest.TIME_STAMP, Query.SortDirection.DESCENDING);
        PreparedQuery preparedQuery = datastore.prepare(query);
        QueryResultIterator<Entity> results = preparedQuery.asQueryResultIterator(fetchOptions);

        List<TranslateRequest> resultTranslateRequests = entitiesToTranslateRequests(results);     // Retrieve and convert Entities
        Cursor cursor = results.getCursor();              // Where to start next time
        if (cursor != null && resultTranslateRequests.size() == requestCountLimit) {         // Are we paging? Save Cursor
            String cursorString = cursor.toWebSafeString();               // Cursors are WebSafe
            logger.log(Level.INFO, "end cursor: " + cursorString);
            return new Result<>(resultTranslateRequests, cursorString);
        } else {
            return new Result<>(resultTranslateRequests);
        }
    }
}
