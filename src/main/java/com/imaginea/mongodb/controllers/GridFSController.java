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
package com.imaginea.mongodb.controllers;

import java.io.File;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.imaginea.mongodb.domain.DocumentUserQueryData;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.imaginea.mongodb.controllers.BaseController.ResponseCallback;
import com.imaginea.mongodb.controllers.BaseController.ResponseTemplate;
import com.imaginea.mongodb.exceptions.ApplicationException;
import com.imaginea.mongodb.exceptions.DocumentException;
import com.imaginea.mongodb.exceptions.ErrorCodes;
import com.imaginea.mongodb.exceptions.InvalidMongoCommandException;
import com.imaginea.mongodb.services.GridFSService;
import com.imaginea.mongodb.services.impl.GridFSServiceImpl;
import com.imaginea.mongodb.utils.ApplicationUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Defines handlers for performing CRUD operations on files stored in GridFS.
 *
 * @author Srinath Anantha
 */
@Path("/{dbName}/gridfs")
@Api(value = "/{dbName}/gridfs", description = "GridFS operations service")
public class GridFSController extends BaseController {
    private final static Logger logger = Logger.getLogger(GridFSController.class);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getGridFSBuckets(@PathParam("dbName") final String dbName,
                                   @QueryParam("connectionId") final String connectionId,
                                   @Context final HttpServletRequest request) {
        String response =
                new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
                        return gridFSService.getAllBuckets(dbName);
                    }
                });
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{bucketName}/create")
    public String createGridFSStore(@PathParam("dbName") final String dbName,
                                    @PathParam("bucketName") final String bucketName,
                                    @QueryParam("connectionId") final String connectionId,
                                    @Context final HttpServletRequest request) {
        String response =
                new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
                        return gridFSService.createStore(dbName, bucketName);
                    }
                });
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{bucketName}/count")
    public String getCount(@PathParam("dbName") final String dbName,
                           @PathParam("bucketName") final String bucketName,
                           @QueryParam("connectionId") final String connectionId,
                           @Context final HttpServletRequest request) {
        String response =
                new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
                        return gridFSService.getCount(dbName, bucketName);
                    }
                });
        return response;
    }

    /**
     * Request handler for getting the list of files stored in GridFS of specified database.
     *
     * @param dbName       Name of Database
     * @param bucketName   Name of GridFS Bucket
     * @param connectionId Mongo Db Configuration provided by user to connect to.
     * @param request      Get the HTTP request context to extract session parameters
     * @return JSON representation of list of all files as a String.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{bucketName}/getfiles")
    @ApiOperation(value = "getfiles", notes = "To get all files containing by specified GridFSBucket")
    public String getFileList(@PathParam("dbName") final String dbName,
                              @PathParam("bucketName") final String bucketName,
                              @QueryParam("connectionId") final String connectionId,
                              final DocumentUserQueryData queryData,
                              @Context final HttpServletRequest request) {
        String response =
                new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
                        int startIndex = queryData.getQuery().indexOf("("), endIndex = queryData.getQuery().lastIndexOf(")");
                        String cmdStr = queryData.getQuery().substring(0, startIndex);
                        int lastIndexOfDot = cmdStr.lastIndexOf(".");
                        if (lastIndexOfDot + 1 == cmdStr.length()) {
                            // In this case the cmsStr = db.gridFSName.
                            throw new InvalidMongoCommandException(ErrorCodes.COMMAND_EMPTY, "Command is empty");
                        }
                        int firstIndexOfDot = cmdStr.indexOf(".");
                        int indexOfDotAtCollectionName = cmdStr.lastIndexOf(".", lastIndexOfDot - 1);
                        String bucket = cmdStr.substring(firstIndexOfDot + 1, indexOfDotAtCollectionName);
                        String collectionName =
                                cmdStr.substring(indexOfDotAtCollectionName + 1, lastIndexOfDot);
                        String command = cmdStr.substring(lastIndexOfDot + 1, cmdStr.length());
                        if (firstIndexOfDot != lastIndexOfDot) {
                            // when commands are of the form db.bucketName.find
                        } else {
                            throw new InvalidMongoCommandException(ErrorCodes.INVALID_COMMAND, "Invalid command");
                        }
                        String jsonStr = queryData.getQuery().substring(startIndex + 1, endIndex);


                        return gridFSService.executeQuery(dbName, bucket, collectionName, command, jsonStr,
                                queryData.getSkip(), queryData.getLimit(), queryData.getSortBy());
                    }
                });
        return response.replace("\\", "").replace("\"{", "{").replace("}\"", "}");
    }

    /**
     * Request handler for retrieving the specified file stored in GridFS.
     *
     * @param dbName       Name of Database
     * @param bucketName   Name of GridFS Bucket
     * @param id           ObjectId of the file to be retrieved
     * @param download     is download request
     * @param connectionId Mongo Db Configuration provided by user to connect to.
     * @return Requested multipartfile for viewing or download based on 'download' param.
     */
    @GET
    @Path("/{bucketName}/getfile")
    @ApiOperation(value = "getfile", notes = "To get GridFS File in Specified GridFSBucket by specifying fileId")
    public Response getFile(@PathParam("dbName") final String dbName,
                            @PathParam("bucketName") final String bucketName, @ApiParam(value = "GridFS fileId") @QueryParam("id") final String id,
                            @QueryParam("download") final boolean download,
                            @QueryParam("connectionId") final String connectionId) throws ApplicationException {
        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
        File fileObject = null;
        try {
            fileObject = gridFSService.getFile(dbName, bucketName, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String contentType = ApplicationUtils.getContentType(fileObject);
        Response.ResponseBuilder response = Response.ok(fileObject, contentType);
        if (download) {
            response.header("Content-Disposition", "attachment; filename='" + fileObject.getName() + "'");
        } else {
            response.header("Content-Disposition", "filename='" + fileObject.getName() + "'");
        }
        return response.build();
    }

    /**
     * Request handler for uploading a file to GridFS.
     *
     * @param dbName       Name of Database
     * @param bucketName   Name of GridFS Bucket
     * @param formData     formDataBodyPart of the uploaded file
     * @param inputStream  inputStream of the uploaded file
     * @param connectionId Mongo Db Configuration provided by user to connect to.
     * @param request      HTTP request context to extract session parameters
     * @return Success message with additional file details such as name, size, download url &
     * deletion url as JSON Array string.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{bucketName}/uploadfile")
    public String uploadFile(@PathParam("dbName") final String dbName,
                             @PathParam("bucketName") final String bucketName,
                             @FormDataParam("files") final FormDataContentDisposition formData,
                             @FormDataParam("files") final InputStream inputStream,
                             @QueryParam("connectionId") final String connectionId,
                             @Context final HttpServletRequest request) {


        String response =
                new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
                        return gridFSService.insertFile(dbName, bucketName, connectionId, inputStream,
                                formData);
                    }
                }, false);
        return response;
    }

    /**
     * Request handler for dropping a file from GridFS.
     *
     * @param dbName       Name of Database
     * @param bucketName   Name of GridFS Bucket
     * @param _id          Object id of file to be deleted
     * @param connectionId Mongo Db Configuration provided by user to connect to.
     * @param request      Get the HTTP request context to extract session parameters
     * @return Status message.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{bucketName}/dropfile")
    @ApiOperation(value = "dropfile", notes = "To drop file from GridFSBucket by specifying fileId")
    public String dropFile(@PathParam("dbName") final String dbName,
                           @PathParam("bucketName") final String bucketName, @ApiParam("GridFS fileId") @QueryParam("id") final String _id,
                           @QueryParam("connectionId") final String connectionId,
                           @Context final HttpServletRequest request) {

        String response =
                new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
                        String result = null;
                        if ("".equals(_id)) {
                            ApplicationException e = new DocumentException(ErrorCodes.DOCUMENT_DOES_NOT_EXIST,
                                    "File Data Missing in Request Body");
                            result = formErrorResponse(logger, e);
                        } else {
                            result = gridFSService.deleteFile(dbName, bucketName, _id);
                        }
                        return result;
                    }
                });
        return response;
    }

    /**
     * Request handler for dropping all files from a GridFS bucket.
     *
     * @param dbName       Name of Database
     * @param bucketName   Name of GridFS Bucket
     * @param connectionId Mongo Db Configuration provided by user to connect to.
     * @param request      Get the HTTP request context to extract session parameters
     * @return String with Status of operation performed.
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{bucketName}/dropbucket")
    public String dropBucket(@PathParam("dbName") final String dbName,
                             @PathParam("bucketName") final String bucketName,
                             @QueryParam("connectionId") final String connectionId,
                             @Context final HttpServletRequest request) {
        String response =
                new ResponseTemplate().execute(logger, connectionId, request, new ResponseCallback() {
                    public Object execute() throws Exception {
                        GridFSService gridFSService = new GridFSServiceImpl(connectionId);
                        return gridFSService.dropBucket(dbName, bucketName);
                    }
                });
        return response;
    }
}
