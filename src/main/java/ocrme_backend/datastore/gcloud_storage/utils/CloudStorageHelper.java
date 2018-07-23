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
import com.google.auth.appengine.AppEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import org.apache.commons.fileupload.FileItemStream;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloudStorageHelper {

    private Storage storage;
    private final Logger logger = Logger.getLogger(CloudStorageHelper.class.getName());

    public CloudStorageHelper() {
        GoogleCredentials credentials;
        try {
            credentials = AppEngineCredentials.getApplicationDefault();
            storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "CloudStorageHelper credentials error " + e.getMessage());
        }
    }

    /**
     * Uploads a file to Google Cloud Storage to the bucket specified in the BUCKET_NAME
     * environment variable, appending a timestamp to end of the uploaded filename.
     */
    @Deprecated
    public String uploadFile(FileItemStream fileStream, final String bucketName)
            throws IOException, ServletException {
        checkFileExtension(fileStream.getName());

        String timeStamp = getTimeStamp();
        final String fileName = timeStamp + fileStream.getName();

        // the inputstream is closed by default, so we don't need to close it here
        BlobInfo blobInfo =
                storage.create(
                        BlobInfo
                                .newBuilder(bucketName, fileName)
                                // Modify access list to allow all users with link to read file
                                .setAcl(new ArrayList<>(Collections.singletonList(Acl.of(User.ofAllUsers(), Role.READER))))
                                .build(),
                        fileStream.openStream());
        logger.log(Level.INFO, "File {0} uploaded as {1}", new Object[]{
                fileStream.getName(), fileName});
        // return the public download link
        return blobInfo.getMediaLink();
    }

    /**
     * preferred way to upload the file
     * <p>
     * Uploads a file to Google Cloud Storage to the bucket specified in the BUCKET_NAME
     * environment variable, appending a timestamp to end of the uploaded filename.
     */
    public String uploadFile(InputStream fileStream, String fileName, final String bucketName) {

        String timeStamp = getTimeStamp();

        fileName = timeStamp + fileName;

        // the inputstream is closed by default, so we don't need to close it here
        BlobInfo blobInfo =
                storage.create(
                        BlobInfo
                                .newBuilder(bucketName, fileName)
                                // Modify access list to allow all users with link to read file
                                .setAcl(new ArrayList<>(Collections.singletonList(Acl.of(User.ofAllUsers(), Role.READER))))
                                .build(),
                        fileStream);
        logger.log(Level.INFO, "File uploaded as " + fileName);

        // return the public download link
        return blobInfo.getMediaLink();
    }

    /**
     * Uploads a file to Google Cloud Storage to the bucket specified in the BUCKET_NAME
     * environment variable, appending a timestamp to end of the uploaded filename.
     */
    public String uploadFile(byte[] bytes, String fileName, final String bucketName) {
        return uploadFile(bytes, fileName, "", bucketName);
    }

    /**
     * Uploads a file to Google Cloud Storage to the bucket specified in the BUCKET_NAME
     * environment variable, appending a timestamp to end of the uploaded filename.
     */
    public String uploadFile(byte[] bytes, String fileName, String directoryName, final String bucketName) {

        return uploadFileForBlob(bytes, fileName, directoryName, bucketName).getMediaLink();
    }

    /**
     * Uploads a file to Google Cloud Storage to the bucket specified in the BUCKET_NAME
     * environment variable, appending a timestamp to end of the uploaded filename.
     *
     * @param bytes         file bytes
     * @param fileName      file name - must be unique
     * @param directoryName string or "" if none
     * @param bucketName
     * @return Blob
     */
    public Blob uploadFileForBlob(byte[] bytes, String fileName, String directoryName, final String bucketName) {
        String timeStamp = getTimeStamp();

        final String destinationFilename = directoryName + "/" + timeStamp + fileName;

        BlobInfo blobInfo = BlobInfo
                .newBuilder(bucketName, destinationFilename)
                // Modify access list to allow all users with link to read file
                .setAcl(new ArrayList<>(Collections.singletonList(Acl.of(User.ofAllUsers(), Role.READER))))
                .build();

        // create the blob in one request.
        Blob blob = storage.create(blobInfo, bytes);

        logger.log(Level.INFO, "File uploaded as " + destinationFilename);

        return blob;
    }

    @Nullable
    public byte[] downloadFile(String gcsUri) throws IOException {
        Blob blob = getBlob(gcsUri);

        ReadChannel reader;
        byte[] result = null;
        if (blob != null) {
            reader = blob.reader();
            ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);

            while (reader.read(bytes) > 0) {
                bytes.flip();
                result = bytes.array();
                bytes.clear();
            }
        }
        return result;
    }

    @Nullable
    Blob getBlob(String gcsUri) {
        //gcsUri is "gs://" + blob.getBucket() + "/" + blob.getName(),
        //example "gs://ocrme-77a2b.appspot.com/ocr_request_images/000c121b-357d-4ac0-a3f2-24e0f6d5cea185dffb40-e754-478f-b5b7-850fab211438.jpg"

        String bucketName = parseGcsUriForBucketName(gcsUri);
        String fileName = parseGcsUriForFilename(gcsUri);

        if (bucketName != null && fileName != null) {
            return storage.get(BlobId.of(bucketName, fileName));
        } else {
            return null;
        }
    }

    @Nullable
    String parseGcsUriForFilename(String gcsUri) {
        String fileName = null;
        String prefix = "gs://";
        if (gcsUri.startsWith(prefix)) {
            int startIndexForBucket = gcsUri.indexOf(prefix) + prefix.length() + 1;
            int startIndex = gcsUri.indexOf("/", startIndexForBucket) + 1;
            fileName = gcsUri.substring(startIndex);
        }
        return fileName;
    }

    @Nullable
    String parseGcsUriForBucketName(String gcsUri) {
        String bucketName = null;
        String prefix = "gs://";
        if (gcsUri.startsWith(prefix)) {
            int startIndex = gcsUri.indexOf(prefix) + prefix.length();
            int endIndex = gcsUri.indexOf("/", startIndex);
            bucketName = gcsUri.substring(startIndex, endIndex);
        }
        return bucketName;
    }

    /**
     * Upload file from path
     *
     * @param uploadFrom path
     * @param bucketName
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @Deprecated
    public String uploadFile(Path uploadFrom, final String bucketName)
            throws IOException, ServletException {

        String originalFilename = uploadFrom.getFileName().toString();
        checkFileExtension(originalFilename);

        String timeStamp = getTimeStamp();
        final String destinationFilename = timeStamp + originalFilename;

        BlobInfo blobInfo = BlobInfo
                .newBuilder(bucketName, destinationFilename)
                // Modify access list to allow all users with link to read file
                .setAcl(new ArrayList<>(Collections.singletonList(Acl.of(User.ofAllUsers(), Role.READER))))
                .build();

        if (Files.size(uploadFrom) > 1_000_000) {
            // When content is not available or large (1MB or more) it is recommended
            // to write it in chunks via the blob's channel writer.
            try (WriteChannel writer = storage.writer(blobInfo)) {
                byte[] buffer = new byte[1024];
                try (InputStream input = Files.newInputStream(uploadFrom)) {
                    int limit;
                    while ((limit = input.read(buffer)) >= 0) {
                        try {
                            writer.write(ByteBuffer.wrap(buffer, 0, limit));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } else {
            byte[] bytes = Files.readAllBytes(uploadFrom);
            // create the blob in one request.
            storage.create(blobInfo, bytes);
        }
        logger.log(Level.INFO, "File {0} uploaded as {1}", new Object[]{
                originalFilename, destinationFilename});
        // return the public download link
        return storage.get(blobInfo.getBlobId()).getMediaLink();
    }


    /**
     * create bucket if not exists
     *
     * @param bucketName
     */
    public void createBucket(String bucketName) {
        try {
            Bucket bucket = storage.get(bucketName, Storage.BucketGetOption.fields());

            //if exists
            if (bucket != null) {
                logger.log(Level.INFO,
                        "Bucket " + bucketName + " was not created, because it already exists.");
            } else {
                // Creates the new bucket
                storage.create(BucketInfo.of(bucketName));
                logger.log(Level.INFO, "Bucket " + bucketName + " created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * Checks that the file extension is supported.
     */
    private void checkFileExtension(String fileName) throws ServletException {
        if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
            String[] allowedExt = {".jpg", ".JPG", ".jpeg", ".png", ".gif", ".pdf"};
            for (String ext : allowedExt) {
                if (fileName.endsWith(ext)) {
                    return;
                }
            }
            throw new ServletException("file must be an image or .pdf");
        }
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS-").format(new Date());
    }
}
