/**
* Copyright (c) 2014, Miguel Ángel Francisco Fernández
*
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
*
* 3. Neither the name of the copyright holder nor the names of its
* contributors may be used to endorse or promote products derived from this
* software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*
* Created: 2014-07-15
*/
package com.interoud.util.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HTTPUtils {

    public static final String ENCODING = "UTF-8";
    public static final int INITIAL_BUFFER_SIZE = 1024;
    public static final int MAX_COOKIES = 4;

    private static List<HTTPCookie> cookieJar;
    static {
        cookieJar = new LinkedList<HTTPCookie>();
        cookieJar = Collections.synchronizedList(cookieJar);
    }

    private static String getEncodedUrlWithParams(String url,
            Map<String, String[]> params) throws UnsupportedEncodingException {

        boolean firstElem = true;
        String encodedUrl = url;

        if (params != null) {
            for (String key : params.keySet()) {
                String[] values = params.get(key);
                if (values != null) {
                    for (String value : values) {
                        if (firstElem) {
                            encodedUrl += "?";
                            firstElem = false;
                        } else {
                            encodedUrl += "&";
                        }
                        encodedUrl += URLEncoder.encode(key, ENCODING) + "="
                                + URLEncoder.encode(value, ENCODING);
                    }
                }
            }
        }

        return encodedUrl;

    }

    public static String doGet(String urlStr, Map<String, String[]> params,
            Integer connectTimeout, Integer readTimeout) throws IOException {
        return doGet(urlStr, params, null, connectTimeout, readTimeout);
    }

    /**
     * Performs a GET HTTP request. Only intended for doing GET of "text/xml"
     * content.
     *
     * @param urlStr
     *            complete url of the server endpoint (including port and path).
     * @param params
     *            list of parameters to be appended in the GET request. This
     *            value can be null.
     * @return The xml answer.
     * @throws IOException
     */
    public static String doGet(String urlStr, Map<String, String[]> params,
            Map<String, String> headers, Integer connectTimeout,
            Integer readTimeout) throws IOException {

        /*
         * URL
         */
        String encodedUrl = getEncodedUrlWithParams(urlStr, params);
        URL url = new URL(encodedUrl);

        /*
         * Make request
         */
        HttpURLConnection hpConn = (HttpURLConnection) url.openConnection();

        /*
         * Get cookies
         */
        String cookie = getCookie(url);
        if (cookie != null) {
            hpConn.setRequestProperty("Cookie", cookie);
        }

        /*
         * Set the other headers
         */

        if (headers != null) {
            for (String key : headers.keySet()) {
                hpConn.setRequestProperty(key, headers.get(key));
            }
        }
        hpConn.setRequestMethod("GET");
        hpConn.setRequestProperty("Connection", "close");
        hpConn.setInstanceFollowRedirects(false);
        if (connectTimeout != null) {
            hpConn.setConnectTimeout(connectTimeout.intValue());
        }
        if (readTimeout != null) {
            hpConn.setReadTimeout(readTimeout.intValue());
        }

        /*
         * Read response
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                hpConn.getInputStream()));

        /*
         * Store cookies
         */
        putCookie(url, hpConn.getHeaderFields().get("Set-Cookie"));

        /*
         * Get result
         */
        StringBuilder result = new StringBuilder(INITIAL_BUFFER_SIZE);
        String input = null;

        while ((input = reader.readLine()) != null) {
            result.append(input + "\n");
        }

        return result.toString();

    }

    public static String doDelete(String urlStr, Map<String, String> headers,
            Integer connectTimeout, Integer readTimeout) throws IOException {

        /*
         * URL
         */
        URL url = new URL(urlStr);

        /*
         * Make request
         */
        HttpURLConnection hpConn = (HttpURLConnection) url.openConnection();

        /*
         * Get cookies
         */
        String cookie = getCookie(url);
        if (cookie != null) {
            hpConn.setRequestProperty("Cookie", cookie);
        }

        /*
         * Set the other headers
         */

        if (headers != null) {
            for (String key : headers.keySet()) {
                hpConn.setRequestProperty(key, headers.get(key));
            }
        }
        hpConn.setRequestMethod("DELETE");
        hpConn.setRequestProperty("Connection", "close");
        hpConn.setInstanceFollowRedirects(false);
        if (connectTimeout != null) {
            hpConn.setConnectTimeout(connectTimeout.intValue());
        }
        if (readTimeout != null) {
            hpConn.setReadTimeout(readTimeout.intValue());
        }

        /*
         * Read response
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                hpConn.getInputStream()));

        /*
         * Store cookies
         */
        putCookie(url, hpConn.getHeaderFields().get("Set-Cookie"));

        /*
         * Get result
         */
        StringBuilder result = new StringBuilder(INITIAL_BUFFER_SIZE);
        String input = null;

        while ((input = reader.readLine()) != null) {
            result.append(input + "\n");
        }

        return result.toString();

    }

    public static String doPost(String urlStr, String data,
            Integer connectTimeout, Integer readTimeout) throws IOException {
        return doPost(urlStr, data, "text/xml; charset=\"" + ENCODING + "\"",
                connectTimeout, readTimeout);
    }

    /**
     * Uses post method
     *
     * @param urlStr
     *            : url to post
     * @param data
     *            : data
     * @return
     * @throws IOException
     */
    public static String doPost(String urlStr, String data, String contentType,
            Integer connectTimeout, Integer readTimeout) throws IOException {

        return doPost(urlStr, data, null, contentType, connectTimeout,
                readTimeout);
    }

    public static String doPost(String urlStr, String data,
            Map<String, String> headers, String contentType,
            Integer connectTimeout, Integer readTimeout) throws IOException {

        HttpURLConnection hpConn = null;
        data = (data != null) ? data : "";

        try {

            /*
             * URL
             */
            URL url = new URL(urlStr);

            /*
             * Make request
             */
            hpConn = (HttpURLConnection) url.openConnection();

            /*
             * Get cookies
             */
            String cookie = getCookie(url);
            if (cookie != null) {
                hpConn.setRequestProperty("Cookie", cookie);
            }

            // hpConn.setRequestProperty("Content-Length",
            // Integer.toString(data.getBytes().length));
            // hpConn.setRequestProperty("Content-Type", contentType);
            if (headers != null) {
                for (String key : headers.keySet()) {
                    hpConn.setRequestProperty(key, headers.get(key));
                }
            }
            if (headers == null || !headers.containsKey("Content-Length")) {
                hpConn.setRequestProperty("Content-Length",
                        Integer.toString(data.getBytes().length));
            }
            if (headers == null || !headers.containsKey("Content-Type")) {
                hpConn.setRequestProperty("Content-Type", contentType);
            }
            hpConn.setRequestMethod("POST");
            hpConn.setRequestProperty("Connection", "close");
            hpConn.setInstanceFollowRedirects(false);
            hpConn.setDoInput(true);
            hpConn.setDoOutput(true);
            hpConn.setUseCaches(false);
            if (connectTimeout != null) {
                hpConn.setConnectTimeout(connectTimeout.intValue());
            }
            if (readTimeout != null) {
                hpConn.setReadTimeout(readTimeout.intValue());
            }

            /*
             * Do not use DataOutputStream for this, horrible things will
             * happen.
             */
            OutputStreamWriter out = new OutputStreamWriter(
                    hpConn.getOutputStream(), ENCODING);
            out.write(data);
            out.close();

            /*
             * Getting the response is required to force the request, otherwise
             * it might not even be sent at all.
             */
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    hpConn.getInputStream()));

            /*
             * Store cookies
             */
            putCookie(url, hpConn.getHeaderFields().get("Set-Cookie"));

            /*
             * Get result
             */
            String input;
            StringBuilder response = new StringBuilder(INITIAL_BUFFER_SIZE);

            while ((input = in.readLine()) != null) {
                response.append(input);
            }

            String result = response.toString();

            return result;

        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (hpConn != null) {
                hpConn.disconnect();
            }
        }
    }

    public static String doPut(String urlStr, String data, String contentType,
            Integer connectTimeout, Integer readTimeout) throws IOException {

        return doPut(urlStr, data, null, contentType, connectTimeout,
                readTimeout);
    }

    /**
     * Uses put method
     *
     * @param urlStr
     *            : url to post
     * @param data
     *            : data
     * @return
     * @throws IOException
     */
    public static String doPut(String urlStr, String data,
            Map<String, String> headers, String contentType,
            Integer connectTimeout, Integer readTimeout) throws IOException {

        HttpURLConnection hpConn = null;
        data = (data != null) ? data : "";

        try {

            /*
             * URL
             */
            URL url = new URL(urlStr);

            /*
             * Make request
             */
            hpConn = (HttpURLConnection) url.openConnection();

            /*
             * Get cookies
             */
            String cookie = getCookie(url);
            if (cookie != null) {
                hpConn.setRequestProperty("Cookie", cookie);
            }

            /*
             * Set the headers
             */
            if (headers != null) {
                for (String key : headers.keySet()) {
                    hpConn.setRequestProperty(key, headers.get(key));
                }
            }
            if (headers == null || !headers.containsKey("Content-Length")) {
                hpConn.setRequestProperty("Content-Length",
                        Integer.toString(data.getBytes().length));
            }
            if (headers == null || !headers.containsKey("Content-Type")) {
                hpConn.setRequestProperty("Content-Type", contentType);
            }
            hpConn.setRequestMethod("PUT");
            hpConn.setRequestProperty("Connection", "close");
            hpConn.setInstanceFollowRedirects(false);
            hpConn.setDoInput(true);
            hpConn.setDoOutput(true);
            hpConn.setUseCaches(false);
            if (connectTimeout != null) {
                hpConn.setConnectTimeout(connectTimeout.intValue());
            }
            if (readTimeout != null) {
                hpConn.setReadTimeout(readTimeout.intValue());
            }

            /*
             * Do not use DataOutputStream for this, horrible things will
             * happen.
             */
            OutputStreamWriter out = new OutputStreamWriter(
                    hpConn.getOutputStream(), ENCODING);
            out.write(data);
            out.close();

            /*
             * Getting the response is required to force the request, otherwise
             * it might not even be sent at all.
             */
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    hpConn.getInputStream()));

            /*
             * Store cookies
             */
            putCookie(url, hpConn.getHeaderFields().get("Set-Cookie"));

            /*
             * Get result
             */
            String input;
            StringBuilder response = new StringBuilder(INITIAL_BUFFER_SIZE);

            while ((input = in.readLine()) != null) {
                response.append(input);
            }

            String result = response.toString();

            return result;

        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (hpConn != null) {
                hpConn.disconnect();
            }
        }
    }

    /*
     * This method is synchornized to avoid concurrent modification exceptions
     * of cookieJar.
     */
    private static synchronized void putCookie(URL url, List<String> cookieList)
            throws IOException {

        if (cookieList != null) {
            for (String item : cookieList) {
                HTTPCookie cookie = new HTTPCookie(url, item);

                /*
                 * Remove duplicate cookies
                 */
                List<HTTPCookie> cookiesToRemove = new ArrayList<HTTPCookie>();
                for (HTTPCookie existingCookie : cookieJar) {
                    if ((cookie.getURL().equals(existingCookie.getURL()))
                            && (cookie.getName().equals(existingCookie
                                    .getName()))) {
                        cookiesToRemove.add(existingCookie);
                    }
                }

                for (HTTPCookie cookieToRemove : cookiesToRemove) {
                    cookieJar.remove(cookieToRemove);
                }

                /*
                 * Add new cookie
                 */
                cookieJar.add(cookie);
            }
        }

        /*
         * Remove cookies
         */
        if (cookieJar.size() > MAX_COOKIES) {
            cookieJar.remove(0);
        }

    }

    /*
     * This method is synchornized to avoid concurrent modification exceptions
     * of cookieJar.
     */
    private static synchronized String getCookie(URL url) throws IOException {

        /*
         * Remove expired cookies
         */
        List<HTTPCookie> cookiesToRemove = new ArrayList<HTTPCookie>();
        for (HTTPCookie existingCookie : cookieJar) {
            if (existingCookie.hasExpired()) {
                cookiesToRemove.add(existingCookie);
            }
        }

        for (HTTPCookie cookieToRemove : cookiesToRemove) {
            cookieJar.remove(cookieToRemove);
        }

        /*
         * Get cookie
         */
        List<String> cookies = new ArrayList<String>();
        for (HTTPCookie cookie : cookieJar) {
            /*
             * Add cookies
             */
            if (cookie.matches(url)) {
                cookies.add(cookie.toString());
            }
        }

        /*
         * Get String
         */
        StringBuffer result = new StringBuffer("");
        for (Iterator<String> it = cookies.iterator(); it.hasNext();) {
            String cookie = it.next();
            result.append(cookie.toString());
            if (it.hasNext()) {
                result.append("; ");
            }
        }

        /*
         * Return result
         */
        return result.toString();

    }

}
