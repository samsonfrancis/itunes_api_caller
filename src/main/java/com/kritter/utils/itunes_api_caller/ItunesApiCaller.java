package com.kritter.utils.itunes_api_caller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class is used to get the bundle id of the given ios app id.
 * It makes a synchronous call to itunes api and processes the results to extract the bundle id of the given app id
 */
public class ItunesApiCaller {
    private static final Logger logger = LoggerFactory.getLogger("itunes_api_caller");
    private static final String ituneApiUrl = "https://itunes.apple.com/lookup?";

    /**
     * This method constructs the 'URL' string that can be used for hitting the Itunes API
     * @param appId an interger(id) or a string(bundle id) for which we need the app info
     * @return url string that can be directly applied to hit the Itunes API
     */
    public static String getUrlString(String appId) {
        if (appId == null || appId.isEmpty()) {
            return null;
        }

        StringBuffer s = new StringBuffer("");
        s.append(ituneApiUrl);

        // if appId contains only numbers then its an id else it's bundle id
        String queryParameter;
        if (appId.matches("[0-9]+")) {
            queryParameter = "id=";
        } else {
            queryParameter = "bundleId=";
        }
        queryParameter = queryParameter + appId;
        s.append(queryParameter);
        return s.toString();
    }

    /**
     * This method process the raw json data returned by the api caller and extracts the bundle id
     * @param jsonString raw json string returned by the itunes api caller
     * @return bundle id
     */
    public static String getBundleIdFromJson(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(jsonString);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (rootNode == null) {
            return null;
        }

        int resultCount = 0;
        if (rootNode.get("resultCount") != null) {
            resultCount = rootNode.get("resultCount").intValue();
        }
        if (resultCount < 1) {
            return null;
        }

        String bundleId = null;
        if (rootNode.get("results") != null && rootNode.get("results").size() > 0 && rootNode.get("results").get(0) != null &&
                rootNode.get("results").get(0).get("bundleId") != null) {
            bundleId = rootNode.get("results").get(0).get("bundleId").textValue();
        }
        return bundleId;
    }

    /**
     * This method makes an API call to Itunes API and extracts the bundle id if present and returns - Synchronous Http call is used
     * @param appId an interger(id) or a string(bundle id) for which we need the app info
     * @return bundle id for the given app id
     */
    public static String getAppBundleIdForAppId(String appId) {
        String urlString = getUrlString(appId);
        if (urlString == null) {
            return null;
        }

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        URLConnection urlc = null;
        if (url != null) {
            try {
                urlc = url.openConnection();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (urlc != null) {
            urlc.setDoOutput(true);
        }

        PrintStream ps = null;
        if (urlc != null) {
            try {
                ps = new PrintStream(urlc.getOutputStream());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (ps != null) {
            ps.close();
        }

        BufferedReader br = null;
        if (urlc != null) {
            try {
                br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        String line;
        StringBuffer jsonApiCallResults = new StringBuffer("");
        if (br != null) {
            try {
                while ((line = br.readLine()) != null) {
                    if (!line.isEmpty()) {
                        jsonApiCallResults.append(line);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getBundleIdFromJson(jsonApiCallResults.toString());
    }

    /**
     * Main method for testing the functionality
     * @param args no args required
     */
    public static void main(String[] args) {
//        String bundleId = getAppBundleIdForAppId("557285579");
        String bundleId = getAppBundleIdForAppId("com.skout.SKOUT");
        System.out.println(bundleId);
    }
}
