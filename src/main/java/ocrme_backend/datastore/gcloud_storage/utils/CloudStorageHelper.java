/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ocrme_backend.datastore.gcloud_storage.utils;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import org.apache.commons.fileupload.FileItemStream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloudStorageHelper {

    private final Logger logger = Logger.getLogger(CloudStorageHelper.class.getName());
    private static Storage storage = null;

    static {
        storage = StorageOptions.getDefaultInstance().getService();
    }

    /**
     * Uploads a file to Google Cloud Storage to the bucket specified in the BUCKET_NAME
     * environment variable, appending a timestamp to end of the uploaded filename.
     */
    public String uploadFile(FileItemStream fileStream, final String bucketName)
            throws IOException, ServletException {
        checkFileExtension(fileStream.getName());

        DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd-HHmmssSSS-");
        DateTime dt = DateTime.now(DateTimeZone.UTC);
        String dtString = dt.toString(dtf);
        final String fileName = dtString + fileStream.getName();

        // the inputstream is closed by default, so we don't need to close it here
        BlobInfo blobInfo =
                storage.create(
                        BlobInfo
                                .newBuilder(bucketName, fileName)
                                // Modify access list to allow all users with link to read file
                                .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
                                .build(),
                        fileStream.openStream());
        logger.log(Level.INFO, "File {0} uploaded as {1}", new Object[]{
                fileStream.getName(), fileName});
        // return the public download link
        return blobInfo.getMediaLink();
    }


    public void createBucket(String bucketName) {
        // Instantiates a client
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Creates the new bucket
        Bucket bucket = storage.create(BucketInfo.of(bucketName));

        logger.log(Level.INFO, "Bucket %s created.%n", bucket.getName());
    }

    /**
     * delete all blobs in bucket
     */
    public void clearBucket(Bucket bucket) {
        Page<Blob> blobs = bucket.list();
        for (Blob blob : blobs.iterateAll()) {
            blob.delete();
        }
    }

    public void deleteBucket(String bucketName) {
        Bucket bucket = storage.get(bucketName, Storage.BucketGetOption.fields());

        //if exists
        if (bucket != null) {
            clearBucket(bucket);

            Storage storage = StorageOptions.getDefaultInstance().getService();
            storage.delete(bucketName);

            logger.log(Level.INFO, "Bucket %s deleted.%n", bucketName);
        }
    }


    /**
     * Example of listing buckets, specifying the page size and a name prefix.
     */
    // [TARGET list(BucketListOption...)]
    // [VARIABLE "bucket_"]
    public Page<Bucket> listBucketsWithSizeAndPrefix(String prefix) {
        // [START listBucketsWithSizeAndPrefix]
        Page<Bucket> buckets = storage.list(Storage.BucketListOption.pageSize(100),
                Storage.BucketListOption.prefix(prefix));
        for (Bucket bucket : buckets.iterateAll()) {
            // do something with the bucket
        }
        // [END listBucketsWithSizeAndPrefix]
        return buckets;
    }

    /**
     * Checks that the file extension is supported.
     */
    private void checkFileExtension(String fileName) throws ServletException {
        if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
            String[] allowedExt = {".jpg", ".JPG", ".jpeg", ".png", ".gif"};
            for (String ext : allowedExt) {
                if (fileName.endsWith(ext)) {
                    return;
                }
            }
            throw new ServletException("file must be an image");
        }
    }
}
