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

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HTTPCookie {

    private String name;

    private String value;

    private URL url;

    private String domain;

    private Date expires;

    private String path;

    public HTTPCookie(URL url, String header) {

        String attributes[] = header.split(";");
        String nameValue = attributes[0].trim();

        this.url = url;
        this.name = nameValue.substring(0, nameValue.indexOf('='));
        this.value = nameValue.substring(nameValue.indexOf('=') + 1);
        this.path = "/";
        this.domain = url.getHost();

        for (int i = 1; i < attributes.length; i++) {

            nameValue = attributes[i].trim();
            int equals = nameValue.indexOf('=');
            if (equals == -1) {
                continue;
            }

            String name = nameValue.substring(0, equals);
            String value = nameValue.substring(equals + 1);

            if (name.equalsIgnoreCase("domain")) {
                String uriDomain = url.getHost();
                if (uriDomain.equals(value)) {
                    this.domain = value;
                } else {
                    if (!value.startsWith(".")) {
                        value = "." + value;
                    }
                    uriDomain = uriDomain.substring(uriDomain.indexOf('.'));
                    if (!uriDomain.equals(value)) {
                        throw new IllegalArgumentException(
                                "Trying to set foreign cookie");
                    }
                    this.domain = value;
                }

            } else if (name.equalsIgnoreCase("path")) {
                this.path = value;

            } else if (name.equalsIgnoreCase("expires")) {

                DateFormat[] expiresFormat = new SimpleDateFormat[] {
                    new SimpleDateFormat("E, dd MMM yyyy k:m:s 'GMT'",
                            Locale.US),
                    new SimpleDateFormat("E, dd-MMM-yyyy k:m:s 'GMT'",
                            Locale.US)
                };

                for(int e = 0; e < expiresFormat.length; e++) {
                    try {
                        this.expires = expiresFormat[e].parse(value);
                        break;
                    } catch (ParseException pe) {
                        ;
                    }
                }

            }
        }
    }

    public URL getURL() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }

    public String getDomain() {
        return domain;
    }

    public boolean hasExpired() {
        if (expires == null) {
            return false;
        }
        Date now = new Date();
        return now.after(expires);
    }

    public boolean matches(URL url) {

        if (hasExpired()) {
            return false;
        }

        String path = url.getPath();
        if (path == null) {
            path = "/";
        }

        return path.startsWith(this.path);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(name);
        result.append("=");
        result.append(value);
        return result.toString();
    }

}
