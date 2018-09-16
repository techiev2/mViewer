/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd. Hyderabad, India
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.imaginea.mongodb.services;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.mongodb.exceptions.ApplicationException;
import com.imaginea.mongodb.exceptions.CollectionException;
import com.imaginea.mongodb.exceptions.DatabaseException;
import com.imaginea.mongodb.exceptions.DocumentException;
import com.imaginea.mongodb.exceptions.GridFSException;
import com.imaginea.mongodb.exceptions.ValidationException;

/**
 * Declares service methods for performing CRUD operations on files stored in GridFS.
 *
 * @author Srinath Anantha
 */
public interface GridFSService {

  /**
   * Service handler for creating GridFS store in the specified database.
   *
   * @param dbName Name of Database
   * @param bucketName Name of GridFS Bucket
   * @return Status message.
   */
  
  
  public String createStore(String dbName, String bucketName)
      throws DatabaseException, CollectionException, GridFSException;

  /**
   * Service handler for getting the list of files stored in GridFS of specified database.
   *
   * @param dbName Name of Database
   * @param bucketName Name of GridFS Bucket
   * @param bucketType
   * @param command
   * @param query
   * @param skip
   * @param limit
   * @param sortBy @return JSON representation of list of all files as a String.
   */
  public JSONObject executeQuery(String dbName, String bucketName, String bucketType,
      String command, String query, String skip, String limit, String sortBy)
      throws ApplicationException, JSONException;

  /**
   * Service handler for retrieving the specified file stored in GridFS.
   *
   * @param dbName Name of Database
   * @param bucketName Name of GridFS Bucket
   * @param id ObjectId of the file to be retrieved
   * @return Requested multipartfile for viewing or download based on 'download' param.
   */
  public File getFile(String dbName, String bucketName, String id)
      throws ValidationException, DatabaseException, CollectionException;

  /**
   * Service handler for uploading a file to GridFS.
   *
   * @param dbName Name of Database
   * @param bucketName Name of GridFS Bucket
   * @param formData formDataBodyPart of the uploaded file
   * @param inputStream inputStream of the uploaded file
   * @param connectionId ConnectionId of the connection
   * @return Success message with additional file details such as name, size, download url &
   *         deletion url as JSON Array string.
   */
  public JSONArray insertFile(String dbName, String bucketName, String connectionId,
      InputStream inputStream, FormDataContentDisposition fileData) throws ApplicationException;

  /**
   * Service handler for dropping a file from GridFS.
   *
   * @param dbName Name of Database
   * @param bucketName Name of GridFS Bucket
   * @param _id Object id of file to be deleted
   * @return Status message.
   */
  public String deleteFile(String dbName, String bucketName, String _id)
      throws DatabaseException, DocumentException, ValidationException, CollectionException;

  /**
   * Service handler for dropping all files from a GridFS bucket.
   *
   * @param dbName Name of Database
   * @param bucketName Name of GridFS Bucket
   * @return Status message.
   */
  public String dropBucket(String dbName, String bucketName) throws DatabaseException,
      DocumentException, ValidationException, CollectionException, JSONException;

  /**
   * Service handler for getting count of all files in a GridFS bucket.
   *
   * @param dbName Name of Database
   * @param bucketName Name of GridFS Bucket
   * @return Status message.
   */
  public JSONObject getCount(String dbName, String bucketName)
      throws DatabaseException, DocumentException, ValidationException, CollectionException;

  /**
   * returns all the gridfs buckets for the db
   *
   * @param dbName
   * @return
   * @throws DatabaseException
   * @throws CollectionException
   */
  Set<String> getAllBuckets(String dbName) throws DatabaseException, CollectionException;
}
