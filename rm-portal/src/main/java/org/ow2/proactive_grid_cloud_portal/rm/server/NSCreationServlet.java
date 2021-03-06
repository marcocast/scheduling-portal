/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.rm.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.ow2.proactive_grid_cloud_portal.common.shared.RestServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NS Creation requires reading one or multiple files from the client,
 * which cannot be done client-side
 *
 * @author ActiveEon Team
 */
@SuppressWarnings("serial")
public class NSCreationServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(NSCreationServlet.class);

    public static final int MAX_UPLOAD_SIZE = 1048576; // in Bytes

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        createNs(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        createNs(request, response);
    }

    private void createNs(HttpServletRequest request, HttpServletResponse response) {

        String sessionId = "";
        String callbackName = "";
        String nsName = "";
        String infra = "";
        String policy = "";

        ArrayList<String> infraParams = new ArrayList<>();
        ArrayList<String> infraFileParams = new ArrayList<>();
        ArrayList<String> policyParams = new ArrayList<>();
        ArrayList<String> policyFileParams = new ArrayList<>();

        boolean readingInfraParams = false;
        boolean readingPolicyParams = false;

        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(4096);
            factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(MAX_UPLOAD_SIZE);

            List<?> fileItems = upload.parseRequest(request);
            Iterator<?> i = fileItems.iterator();
            while (i.hasNext()) {
                FileItem fi = (FileItem) i.next();
                String fieldName = fi.getFieldName();
                if (fi.isFormField()) {
                    if (fieldName.equals("sessionId")) {
                        sessionId = fi.getString();
                    } else if (fieldName.equals("nsCallback")) {
                        callbackName = fi.getString();
                    } else if (fieldName.equals("nsName")) {
                        nsName = fi.getString();
                    } else if (fieldName.equals("infra")) {
                        infra = fi.getString();
                        readingInfraParams = true;
                    } else if (fieldName.equals("policy")) {
                        policy = fi.getString();
                        readingPolicyParams = true;
                        readingInfraParams = false;
                    } else if (readingInfraParams) {
                        infraParams.add(fi.getString());
                    } else if (readingPolicyParams) {
                        policyParams.add(fi.getString());
                    } else {
                        LOGGER.warn("Unexpected param " + fieldName);
                    }
                } else {
                    if (readingInfraParams) {
                        byte[] bytes = IOUtils.toByteArray(fi.getInputStream());
                        infraFileParams.add(new String(bytes));
                    } else if (readingPolicyParams) {
                        byte[] bytes = IOUtils.toByteArray(fi.getInputStream());
                        policyFileParams.add(new String(bytes));
                    } else {
                        LOGGER.warn("Unexpected param " + fieldName);
                    }
                }
            }
            String failFast = null;
            if (nsName.length() == 0) {
                failFast = "You need to pick a name for the new Node Source";
            } else if (policy.length() == 0 || policy.equals("undefined")) {
                failFast = "No Policy selected";
            } else if (infra.length() == 0 || infra.equals("undefined")) {
                failFast = "No Infrastructure selected";
            }

            if (failFast != null) {
                throw new RestServerException(failFast);
            }

            String jsonResult = ((RMServiceImpl) RMServiceImpl.get()).createNodeSource(sessionId,
                                                                                       nsName,
                                                                                       infra,
                                                                                       toArray(infraParams),
                                                                                       toArray(infraFileParams),
                                                                                       policy,
                                                                                       toArray(policyParams),
                                                                                       toArray(policyFileParams));

            if (jsonResult.equals("true")) {
                jsonResult = createNonEscapedSimpleJsonPair("result", "true");
            }

            write(response, createJavascriptPayload(callbackName, jsonResult));
        } catch (Throwable t) {
            write(response,
                  createJavascriptPayload(callbackName, createEscapedSimpleJsonPair("errorMessage", t.getMessage())));
        }
    }

    private void write(HttpServletResponse response, String s) {
        try {
            response.getWriter().write(s);
        } catch (Throwable t) {
            LOGGER.warn("Failed to write script back to client", t);
        }
    }

    private String createEscapedSimpleJsonPair(String key, String value) {
        return createSimpleJsonPair(key, value, true);
    }

    private String createNonEscapedSimpleJsonPair(String key, String value) {
        return createSimpleJsonPair(key, value, false);
    }

    private String createSimpleJsonPair(String key, String value, boolean escapeValue) {
        if (escapeValue) {
            value = "\"" + value + "\"";
        }

        return "{ \"" + key + "\" : " + value + " }";
    }

    private String createJavascriptPayload(String callbackName, String json) {
        return "<script type='text/javascript'>window.opener.focus(); window.opener." + callbackName + "(" + json +
               "); window.close();</script>";
    }

    private String[] toArray(ArrayList<String> list) {
        return list.toArray(new String[list.size()]);
    }

}
