/**
 * Copyright (C) Zoomdata, Inc. 2012-2016. All rights reserved.
 */
package zoomdata.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by qcluu on 2/27/15.
 */
public class HttpUtil {
    private final static Logger logger = Logger.getLogger(HttpUtil.class);

    static final String USER_AGENT = "Mozilla/5.0";

    static {
        //for localhost testing only
        HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {

                    public boolean verify(String hostname,
                                          javax.net.ssl.SSLSession sslSession) {
                        return true;
                        /*
                        if (hostname.equals("ec2-52-5-35-247.compute-1.amazonaws.com")) {
                            return true;
                        }
                        return false;
                        */
                    }

                });
    }

    // HTTP GET request
    static public String sendGet(final String url) throws Exception {

        final Map<String, String> headers = null;
        return sendGetWithHeader(url, headers);

    }

    static public String httpGetWithRetries(final String url, final int retries) throws Exception {
        URL obj = new URL(url);
        boolean done = false;
        HttpURLConnection con = null;
        int retry = 0;
        int responseCode = 0;
        while ((retry < retries) && (done == false)) {
            if (retry > 0) {
                Thread.sleep(500);
                System.out.println("**** Last responseCode = " + responseCode + ". Retrying (" + retry + " of " + retries + ") ...");
            }
            con = (HttpURLConnection) obj.openConnection();
            final String USER_AGENT = "Mozilla/5.0";

            // optional default is GET
            con.setRequestMethod("GET");
            con.setReadTimeout(5000);
            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            // System.out.println("Response Code : " + responseCode);
            if (responseCode == 200) {
                done = true;
                if (retry > 0) {
                    System.out.println("***** Retried (#" + retry + " of " + retries + ") OK *****");
                }
            }
            retry++;
        }
        if ((done == false) && (retry >= retries)) {
            logger.info("**** Time out after " + retry + " of retries. URL: " + url);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println("got data: " + response.toString());
        return response.toString();

    }

    static public String httpGet(final String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        final String USER_AGENT = "Mozilla/5.0";

        // optional default is GET
        con.setRequestMethod("GET");
        con.setReadTimeout(5000);
        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
     //   System.out.println("\nSending 'GET' request to URL : " + url);
     //   System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
    //    System.out.println("got data: " + response.toString());
        return response.toString();

    }

    static public String httpDeleteWithHeader(final String url, final Map<String, String> headers) throws Exception {
        final URL obj = new URL(url);
        final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("DELETE");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }
        con.setRequestProperty("X-WWW-Authenticate", "true");
        int responseCode = con.getResponseCode();
        if (logger.isDebugEnabled()) {
            logger.debug("\nSending 'GET' request to URL : " + url);
            logger.debug("Response Code : " + responseCode);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();

    }

    static public String sendDeleteWithHeader(final String url, final Map<String, String> headers) throws Exception {
        if (url.startsWith("https") == false){
            return httpDeleteWithHeader(url, headers);
        }

        final URL obj = new URL(url);
        final HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("DELETE");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }
        con.setRequestProperty("X-WWW-Authenticate", "true");
        int responseCode = con.getResponseCode();
        if (logger.isDebugEnabled()) {
            logger.debug("\nSending 'GET' request to URL : " + url);
            logger.debug("Response Code : " + responseCode);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();

    }

    static public String httpGetWithHeader(final String url, Map<String, String> headers) throws Exception {

        final URL obj = new URL(url);
        final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }
        con.setRequestProperty("X-WWW-Authenticate", "true");
        int responseCode = con.getResponseCode();
        if (logger.isDebugEnabled()) {
            logger.debug("\nSending 'GET' request to URL : " + url);
            logger.debug("Response Code : " + responseCode);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();

    }


    static public String sendGetWithHeader(final String url, Map<String, String> headers) throws Exception {
        if (url.startsWith("https") == false) {
            return httpGetWithHeader(url, headers);
        }
        final URL obj = new URL(url);
        final HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }
        con.setRequestProperty("X-WWW-Authenticate", "true");
        int responseCode = con.getResponseCode();
        if (logger.isDebugEnabled()) {
            logger.debug("\nSending 'GET' request to URL : " + url);
            logger.debug("Response Code : " + responseCode);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();

    }

    static public String sendSecuredGetWithHeader(final String url, Map<String, String> headers) throws Exception {

        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

// Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
/*
// Now you can access an https URL without having the certificate in the truststore
        try {
            URL url = new URL("https://hostname/index.html");
        } catch (MalformedURLException e) {
        }
        */
        final URL obj = new URL(url);
        final HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }

        int responseCode = con.getResponseCode();
        logger.info("\nSending 'GET' request to URL : " + url);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();

    }

    public static String millisToLongDHMS(long duration) {

        String sign = "";
        if (duration < 0) {
            duration *= -1;
            sign = "-";
        }
        final long ONE_SECOND = 1000;
        final long SECONDS = 60;

        final long ONE_MINUTE = ONE_SECOND * 60;
        final long MINUTES = 60;

        final long ONE_HOUR = ONE_MINUTE * 60;
        final long HOURS = 24;

        final long ONE_DAY = ONE_HOUR * 24;

        final long ms = duration % 1000;

        /**
         * converts time (in milliseconds) to human-readable format
         *  "<w> days, <x> hours, <y> minutes and (z) seconds"
         */
        StringBuffer res = new StringBuffer(sign);

        if (duration < 1000) {
            res.append(duration).append(" msec");
            return res.toString();
        }

        long temp = 0;
        if (duration >= ONE_SECOND) {
            temp = duration / ONE_DAY;
            if (temp > 0) {
                duration -= temp * ONE_DAY;
                res.append(temp).append(" day").append(temp > 1 ? "s" : "")
                        .append(duration >= ONE_MINUTE ? ", " : "");
            }

            temp = duration / ONE_HOUR;
            if (temp > 0) {
                duration -= temp * ONE_HOUR;
                res.append(temp).append(" hour").append(temp > 1 ? "s" : "")
                        .append(duration >= ONE_MINUTE ? ", " : "");
            }

            temp = duration / ONE_MINUTE;
            if (temp > 0) {
                duration -= temp * ONE_MINUTE;
                res.append(temp).append(" minute").append(temp > 1 ? "s" : "");
            }

            if (!res.toString().equals("") && duration >= ONE_SECOND) {
                res.append(" and ");
            }

            temp = duration / ONE_SECOND;
            if (temp > 0) {
                res.append(temp).append(" second").append(temp > 1 ? "s" : "");
            }
            if (ms > 0) {
                res.append(" and ");


            }
            temp = duration / ONE_SECOND;
            if (temp > 0) {
                res.append(temp).append(" second").append(temp > 1 ? "s" : "");
            }
            if (ms > 0) {
                res.append(" and ");
                res.append(ms).append(" msec");
            }
            return res.toString();
        } else {
            return "0 second";
        }
    }

    public static String sendPost(final String url, final String postData, final Map<String, String> headers) throws Exception {

        // String url = "https://selfsolve.apple.com/wcResults.do";

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Content-Type", "application/json");

        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                post.setHeader(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }
        /*
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
        urlParameters.add(new BasicNameValuePair("cn", ""));
        urlParameters.add(new BasicNameValuePair("locale", ""));
        urlParameters.add(new BasicNameValuePair("caller", ""));
        urlParameters.add(new BasicNameValuePair("num", "12345"));
        */
        post.setEntity(new StringEntity(postData));

        HttpResponse response = client.execute(post);
        logger.info("Sending 'POST' request to URL : " + url);
        logger.info("Post parameters : " + post.getEntity());
        logger.info("Response Code : " +
                response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        logger.info(result.toString());
        return result.toString();

    }


    static public Map<String, Object> httpsPost(final String url, final String data, final Map<String, String> headers) throws Exception {
/*

        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

// Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
*/

        final long start = System.currentTimeMillis();
        final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setDoInput(true);
        con.setUseCaches(false);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }

        // con.setRequestProperty("Content-Length", "10"); // Integer.toString(data.length()));
        // con.setReadTimeout(10000);
        // con.connect();
        // OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        final DataOutputStream out = new DataOutputStream(con.getOutputStream());

        out.writeBytes(data);
        // System.err.println("data=" + data);
        // System.err.println("code=" + con.getResponseCode());
        // System.err.println("msg=" + con.getResponseMessage());
        out.flush();
        out.close();

        final long end = System.currentTimeMillis();

        final Map<String, Object> results = new HashMap<>();
        // Get response data.
        String line = null;
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (IOException ex) {
            System.err.println("***Error:*** Unable to connect to url " + url + ". post data=" + data);
            ex.printStackTrace();
        }
        if (rd == null) {
            return results;
        }
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String respStr = sb.toString();
        results.put("status", new Integer(con.getResponseCode()));
        results.put("message", con.getResponseMessage());
        results.put("data", respStr);
        return results;
    }

    static public Map<String, Object> httpsSend(final String url, final String data,
                                                final Map<String, String> headers, final String method) throws Exception {

        final long start = System.currentTimeMillis();
        final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setDoInput(true);
        con.setUseCaches(false);
        con.setDoOutput(true);
        if (method.equals("PATCH")) {
            con.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            con.setRequestMethod("POST");
        } else {
            con.setRequestMethod(method);
        }
        con.setRequestProperty("Content-Type", "application/json");

        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }

        // con.setRequestProperty("Content-Length", "10"); // Integer.toString(data.length()));
        // con.setReadTimeout(10000);
        // con.connect();
        // OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        final DataOutputStream out = new DataOutputStream(con.getOutputStream());

        out.writeBytes(data);
        // System.err.println("data=" + data);
        // System.err.println("code=" + con.getResponseCode());
        // System.err.println("msg=" + con.getResponseMessage());
        out.flush();
        out.close();

        final long end = System.currentTimeMillis();

        final Map<String, Object> results = new HashMap<>();
        // Get response data.
        String line = null;
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (IOException ex) {
            System.err.println("***Error:*** Unable to connect to url " + url + ". post data=" + data);
            ex.printStackTrace();
        }
        if (rd == null) {
            return results;
        }
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String respStr = sb.toString();
        results.put("status", new Integer(con.getResponseCode()));
        results.put("message", con.getResponseMessage());
        results.put("data", respStr);
        return results;
    }

    static public Map<String, Object> httpsPut(final String url, final String data, final Map<String, String> headers) throws Exception {
        final long start = System.currentTimeMillis();
        final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        con.setDoInput(true);
        con.setUseCaches(false);
        con.setDoOutput(true);
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");

        con.setRequestProperty("User-Agent", USER_AGENT);
        if (headers != null) {
            final Set<String> keys = headers.keySet();
            for (final String key : keys) {
                con.setRequestProperty(key, headers.get(key).toString());
            }
        } else {
            // Ignore header
        }

        // con.setRequestProperty("Content-Length", "10"); // Integer.toString(data.length()));
        // con.setReadTimeout(10000);
        // con.connect();
        // OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        final DataOutputStream out = new DataOutputStream(con.getOutputStream());

        out.writeBytes(data);
        // System.err.println("data=" + data);
        // System.err.println("code=" + con.getResponseCode());
        // System.err.println("msg=" + con.getResponseMessage());
        out.flush();
        out.close();

        final long end = System.currentTimeMillis();

        final Map<String, Object> results = new HashMap<>();
        // Get response data.
        String line = null;
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (IOException ex) {
            System.err.println("***Error:*** Unable to connect to url " + url + ". post data=" + data);
            ex.printStackTrace();
        }
        if (rd == null) {
            return results;
        }
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String respStr = sb.toString();
        results.put("status", new Integer(con.getResponseCode()));
        results.put("message", con.getResponseMessage());
        results.put("data", respStr);
        return results;
    }

    private void sendPost2(final String url, final String data, final Map<String, String> headers) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        logger.info("\nSending 'POST' request to URL : " + url);
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        logger.info(response.toString());

    }
}
