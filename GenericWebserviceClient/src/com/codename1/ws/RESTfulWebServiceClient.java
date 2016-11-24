/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.ws;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.processing.Result;
import com.codename1.ui.Display;
import com.codename1.util.SuccessCallback;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A generic webservice client to a consume a typical web service as generated by the 
 * Netbeans RESTful Webservice wizard.
 * @author shannah
 */
public class RESTfulWebServiceClient {
    
    /**
     * The endpoint url for the web service.
     */
    private final String endpointURL;
    
    public RESTfulWebServiceClient(String endpointURL) {
        this.endpointURL = endpointURL;
    }
    
    /**
     * To be overridden by subclasses that need to customize the connection request... e.g. for adding auth tokens.
     * @param req 
     */
    protected void setupConnectionRequest(ConnectionRequest req) {
        
    }
    
    /**
     * Deletes a record with a given ID
     * @param id The ID of the record to delete.
     * @param callback Callback executed when request is complete.  Parameter will be true on success. false on fail.
     */
    public void delete(String id, SuccessCallback<Boolean> callback) {
        ConnectionRequest req = new ConnectionRequest() {
            @Override
            protected void handleErrorResponseCode(int code, String message) {
                if (code == 401 || code == 400 || code == 500 || code == 405) {
                    return;
                }
                super.handleErrorResponseCode(code, message); 
            }
        };
        setupConnectionRequest(req);
        req.setPost(true);
        req.setHttpMethod("DELETE");
        req.setReadResponseForErrors(true);
        req.setUrl(endpointURL+"/"+id);
        req.addResponseListener(e->{
            callback.onSucess(e.getResponseCode() >= 200 && e.getResponseCode() < 300);
            
        });
        NetworkManager.getInstance().addToQueue(req);
    }
    
    /**
     * Creates a new record with the data included in the map
     * @param data Data of the record.
     * @param callback Callback executed when request is complete.  Parameter will be true on success, false on fail.
     */
    public void create(Map data, SuccessCallback<Boolean> callback) {
        ConnectionRequest req = new ConnectionRequest() {

            @Override
            protected void handleErrorResponseCode(int code, String message) {
                if (code == 401 || code == 400 || code == 500 || code == 405 || code == 404) {
                    return;
                }
                super.handleErrorResponseCode(code, message); 
            }
            
            @Override
            protected void buildRequestBody(OutputStream os) throws IOException {
                Result res = Result.fromContent(data);
                os.write(res.toString().getBytes("UTF-8"));
            }
            
        };
        setupConnectionRequest(req);
        req.addRequestHeader("Content-Type", "application/json");
        req.setReadResponseForErrors(true);
        req.setUrl(endpointURL);
        req.setPost(true);
        req.setHttpMethod("POST");
        
        
        req.addResponseListener(e->{
            callback.onSucess(req.getResponseCode() >= 200 && req.getResponseCode() < 300);
            
        });
        NetworkManager.getInstance().addToQueue(req);
    }
    
    /**
     * Updates an existing record.
     * @param id The ID of the record.
     * @param data The data to update in the record.
     * @param callback Callback called when executioln complete.  Param will be true on success.  false on fail.
     */
    public void edit(String id, Map data, SuccessCallback<Boolean> callback) {
        ConnectionRequest req = new ConnectionRequest() {

            @Override
            protected void handleErrorResponseCode(int code, String message) {
                if (code == 401 || code == 400 || code == 500 || code == 405) {
                    return;
                }
                super.handleErrorResponseCode(code, message); 
            }
            
            @Override
            protected void buildRequestBody(OutputStream os) throws IOException {
                Result res = Result.fromContent(data);
                os.write(res.toString().getBytes("UTF-8"));
            }
            
        };
        setupConnectionRequest(req);
        req.setUrl(endpointURL + "/"+ id);
        req.setPost(true);
        req.setReadResponseForErrors(true);
        req.setHttpMethod("PUT");
        req.addRequestHeader("Content-Type", "application/json");
        req.addResponseListener(e->{
            callback.onSucess(e.getResponseCode() >= 200 && e.getResponseCode() < 300);
            
        });
        NetworkManager.getInstance().addToQueue(req);
    }
    
    /**
     * Counts the number of records in the endpoint.
     * @param callback Callback called with result.  Param may be null if error occurred.
     */
    public void count(SuccessCallback<Integer> callback) {
        ConnectionRequest req = new ConnectionRequest() {
            @Override
            protected void handleErrorResponseCode(int code, String message) {
                if (code == 401 || code == 400 || code == 500 || code == 405) {
                    return;
                }
                super.handleErrorResponseCode(code, message); 
            }
        };
        setupConnectionRequest(req);
        req.setPost(false);
        req.setHttpMethod("GET");
        req.setReadResponseForErrors(true);
        req.setUrl(endpointURL+"/count");
        req.addResponseListener(e->{
            try {
                callback.onSucess(Integer.parseInt(new String(req.getResponseData(), "UTF-8")));
            } catch (Exception ex) {
                Log.e(ex);
                callback.onSucess(null);
            }
            
        });
        NetworkManager.getInstance().addToQueue(req);
    }
    
    /**
     * Queries the webservice and return a result set.
     * @param query
     * @param callback 
     */
    public void find(Query query, SuccessCallback<RowSet> callback) {
        ConnectionRequest req = new ConnectionRequest() {
            @Override
            protected void handleErrorResponseCode(int code, String message) {
                if (code == 401 || code == 400 || code == 500 || code == 405) {
                    return;
                }
                super.handleErrorResponseCode(code, message); 
            }
        };
        setupConnectionRequest(req);
        req.setReadResponseForErrors(true);
        query.setupConnectionRequest(this, req);
        req.addResponseListener(e->{
            if (e.getResponseCode() == 200) {
                Display.getInstance().scheduleBackgroundTask(()->{
                    JSONParser p = new JSONParser();
                    try {
                        Map<String,Object> m = p.parseJSON(new InputStreamReader(new ByteArrayInputStream(req.getResponseData()), "UTF-8"));
                        RowSet rs = null;
                        if (m.containsKey("root")) {
                            List<Map> rows = (List<Map>)m.get("root");
                            rs = new RowSet(rows);
                        } else {
                            List<Map> rows = new ArrayList<Map>();
                            rows.add(m);
                            rs = new RowSet(rows);
                            
                        }
                        rs.query = query;
                        rs.skip = query.skip;
                        rs.limit = query.limit;
                        RowSet finalRs = rs;
                        Display.getInstance().callSerially(()->{
                            callback.onSucess(finalRs);
                        });
                        
                    } catch (Exception ex) {
                        Log.e(ex);
                        Display.getInstance().callSerially(()->{
                            callback.onSucess(null);
                        });
                        
                        
                    }
                });
            } else {
                Log.p("Find request failed. Response code was "+e.getResponseCode());
                callback.onSucess(null);
            } 
        });
        NetworkManager.getInstance().addToQueue(req);
    }
    
    /**
     * Represents a query to find records in a web service
     */
    public static class Query {
        private int skip=0;
        private int limit=30;
        private String id;
        
        /**
         * Sets the number of records to skip (i.e. starting point).  Default is 0.
         * @param skip
         * @return 
         */
        public Query skip(int skip) {
            this.skip = skip;
            return this;
        }
        
        /**
         * Returns the starting point in the range to return.
         * @return The starting point of the returned range.
         */
        public int getSkip() {
            return skip;
        }
        
        /**
         * Sets the maximum number of records to return.
         * @param limit
         * @return 
         */
        public Query limit(int limit) {
            this.limit = limit;
            return this;
        }
        /**
         * Gets the maximum number of records to return.
         * @return 
         */
        public int getLimit() {
            return limit;
        }
        
        /**
         * Sets the ID of a single record to obtain.  If this is specified,
         * then skip and limit are ignored, and only the single record will be returned.
         * @param id The ID of the record to obtain.
         * @return 
         */
        public Query id(String id) {
            this.id = id;
            return this;
        }
        
        /**
         * Gets the ID of a single record to obtain.  
         * @return 
         */
        public String getId() {
            return id;
        }
        
        /**
         * Sets up a connection request to perform the query.  Subclasses that want to provide more elaborate 
         * searching ability should override this method to set up the connection request accordingly.
         * @param client
         * @param req 
         */
        protected void setupConnectionRequest(RESTfulWebServiceClient client, ConnectionRequest req) {
            req.addRequestHeader("Accept", "application/json");
            req.setHttpMethod("GET");
            req.setPost(false);
            if (id == null) {
                req.setUrl(client.endpointURL + "/" + skip + "/" + (skip+limit-1));
            } else {
                req.setUrl(client.endpointURL + "/" + id);
            }
            
        }
        
        
        
    }
    
    /**
     * Encapsulated a rowset of records returned from the webservice.
     */
    public static class RowSet implements Iterable<Map>{
        private Query query;
        private int skip;
        private int limit;
        private final List<Map> data;

        /**
         * Creates a new rowset that wraps the given data.
         * @param data 
         */
        RowSet(List<Map> data) {
            this.data = new ArrayList<Map>();
            this.data.addAll(data);
        }
        
        @Override
        public Iterator<Map> iterator() {
            return data.iterator();
        }

            /**
         * Gets the skip position of this rowset within the found set.  0 == no skip.
         * @return the skip
         */
        public int getSkip() {
            return skip;
        }

        /**
         * Skip position within the found set.  0 == no skip (i.e. first record of this rowset is the first record of the found set).
         * @param skip the skip to set
         */
        void setSkip(int skip) {
            this.skip = skip;
        }

        /**
         * Returns the query that was used to produce this set.
         * @return 
         */
        public Query getQuery() {
            return query;
        }

        /**
         * Sets the query that was used to get this set.
         * @param query 
         */
        private void setQuery(Query query) {
            this.query = query;
        }

        /**
         * Position of first record.  First record is 1.
         * getFirst() == getSkip() + 1
         * @return 
         */
        public int getFirst() {
            return getSkip() + 1;
        }

        /**
         * Position of last record.  First record is 1.  
         * min(getFound(), getSkip() + getLimit()) == getLast()
         * @return 
         */
        public int getLast() {
            return Math.min(getSkip() + getLimit(), getSkip() + data.size());
        }
        
        /**
         * Returns the limit of the query that produced this rowset.
         * @return 
         */
        public int getLimit() {
            return limit;
        }
        
        
        /**
         * Generates the query to produce the next page of results.
         * @return 
         */
        public Query getNextQuery() {
            Query out = new Query();
            out.skip = getLast();
            out.limit = limit;
            out.id = query.id;
            return out;
        }
        
        
                
    }
    
}
