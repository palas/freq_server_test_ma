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
* Created: 2014-08-04
*/
package com.interoud.freqserver.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXB;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.interoud.freqserver.test.parser.FreqServerResponse;
import com.lambdastream.util.net.HTTPUtils;

@SuppressWarnings("restriction")
public class FreqServerTest {

    private static final String BASEURL = "http://localhost:8080/freq_server/";
    private static final String OK_RESPONSE = "OK";
    private static final String ERROR_RESPONSE = "ERROR";
    private static final String ERROR_TYPE_ALREADY_STARTED = "ALREADY_STARTED";
    private static final String ERROR_TYPE_NOT_RUNNING = "NOT_RUNNING";
    private static final String ERROR_TYPE_NOT_ALLOCATED = "NOT_ALLOCATED";

    /*
     * List of allocated frequencies
     */
    private Collection<Integer> allocatedFrequencies;

    @Before
    public void setUp() throws IOException {
        allocatedFrequencies = new ArrayList<Integer>();
    }

    @After
    public void tearDown() throws IOException {
        /*
         * Deallocate all frequencies and stop the server
         */
        Collection<Integer> frequencies = new ArrayList<Integer>(
                allocatedFrequencies);
        for(Integer frequency : frequencies) {
            deallocateFrequency(frequency);
        }

        stopServer();
    }

    /* =========================================================================
     * Tests
     * =======================================================================*/
    @Test
    public void testStart() throws IOException {
        FreqServerResponse startServerResponse = startServer();
        checkNoErrors(startServerResponse);
    }

    @Test
    public void testStop() throws IOException {
        startServer();
        FreqServerResponse stopServerResponse = stopServer();
        checkNoErrors(stopServerResponse);
    }

    @Test
    public void testStartTwice() throws IOException {
        startServer();
        FreqServerResponse startServerResponse = startServer();
        checkNotAlreadyStartedError(startServerResponse);
    }

    @Test
    public void testStopTwice() throws IOException {
        startServer();
        stopServer();
        FreqServerResponse stopServerResponse = stopServer();
        checkNotRunningError(stopServerResponse);
    }

    @Test
    public void testAllocate() throws IOException {
        startServer();
        FreqServerResponse allocateFrequencyResponse = allocateFrequency();
        checkNoErrors(allocateFrequencyResponse);
        Assert.assertNotNull(allocateFrequencyResponse.getResult().getFrequencyAllocated());
    }

    @Test
    public void testAllocateTwice() throws IOException {
        startServer();
        FreqServerResponse allocateFrequencyResponse1 = allocateFrequency();
        checkNoErrors(allocateFrequencyResponse1);
        Assert.assertNotNull(allocateFrequencyResponse1.getResult().getFrequencyAllocated());

        FreqServerResponse allocateFrequencyResponse2 = allocateFrequency();
        checkNoErrors(allocateFrequencyResponse2);
        Assert.assertNotNull(allocateFrequencyResponse2.getResult().getFrequencyAllocated());

        Assert.assertTrue(!allocateFrequencyResponse1.getResult().getFrequencyAllocated().equals(
                allocateFrequencyResponse2.getResult().getFrequencyAllocated()));
    }


    @Test
    public void testAllocateNotStarted() throws IOException {
        FreqServerResponse allocateFrequencyResponse = allocateFrequency();
        checkNotRunningError(allocateFrequencyResponse);
    }

    @Test
    public void testDeallocate() throws IOException {
        startServer();
        FreqServerResponse allocateFrequencyResponse = allocateFrequency();
        Integer allocatedFrequency = allocateFrequencyResponse.getResult().getFrequencyAllocated();

        FreqServerResponse deallocateFrequencyResponse = deallocateFrequency(
                allocatedFrequency);
        checkNoErrors(deallocateFrequencyResponse);
    }

    @Test
    public void testDeallocateTwice() throws IOException {
        startServer();
        FreqServerResponse allocateFrequencyResponse = allocateFrequency();
        Integer allocatedFrequency = allocateFrequencyResponse.getResult().getFrequencyAllocated();

        deallocateFrequency(allocatedFrequency);

        FreqServerResponse deallocateFrequencyResponse = deallocateFrequency(
                allocatedFrequency);
        checkNotAllocatedError(deallocateFrequencyResponse);
    }

    @Test
    public void testDeallocateNotExistingFrequency() throws IOException {
        startServer();

        FreqServerResponse deallocateFrequencyResponse = deallocateFrequency(
                new Integer(0));
        checkNotAllocatedError(deallocateFrequencyResponse);
    }

    @Test
    public void testDeallocateNotStarted() throws IOException {
        FreqServerResponse deallocateFrequencyResponse = deallocateFrequency(
                new Integer(0));
        checkNotRunningError(deallocateFrequencyResponse);
    }

    /* =========================================================================
     * Check responses
     * =======================================================================*/
    private void checkNoErrors(FreqServerResponse response) {
        Assert.assertEquals(OK_RESPONSE, response.getState());
        Assert.assertTrue(response.getError().isEmpty());
    }

    private void checkNotAlreadyStartedError(FreqServerResponse response) {
        Assert.assertEquals(ERROR_RESPONSE, response.getState());
        Assert.assertEquals(1, response.getError().size());
        Assert.assertEquals(ERROR_TYPE_ALREADY_STARTED,
                response.getError().get(0).getErrorType());
    }

    private void checkNotRunningError(FreqServerResponse response) {
        Assert.assertEquals(ERROR_RESPONSE, response.getState());
        Assert.assertEquals(1, response.getError().size());
        Assert.assertEquals(ERROR_TYPE_NOT_RUNNING,
                response.getError().get(0).getErrorType());
    }

    private void checkNotAllocatedError(FreqServerResponse response) {
        Assert.assertEquals(ERROR_RESPONSE, response.getState());
        Assert.assertEquals(1, response.getError().size());
        Assert.assertEquals(ERROR_TYPE_NOT_ALLOCATED,
                response.getError().get(0).getErrorType());
    }

    /* =========================================================================
     * API operations
     * =======================================================================*/
    private FreqServerResponse startServer() throws IOException {
        return httpPost(BASEURL + "StartServer");
    }

    private FreqServerResponse stopServer() throws IOException {
        return httpPost(BASEURL + "StopServer");
    }

    private FreqServerResponse allocateFrequency() throws IOException {
        FreqServerResponse response = httpPost(BASEURL + "AllocateFrequency");
        if(response.getResult() != null &&
                response.getResult().getFrequencyAllocated() != null) {
            allocatedFrequencies.add(response.getResult().getFrequencyAllocated());
        }
        return response;
    }

    private FreqServerResponse deallocateFrequency(Integer frequency)
            throws IOException {
        String body = null;
        if(frequency != null) {
            body = frequency.toString();
        }
        FreqServerResponse response = httpPost(BASEURL + "DeallocateFrequency",
                body);
        if(OK_RESPONSE.equals(response.getState())) {
            allocatedFrequencies.remove(frequency);
        }
        return response;
    }

    /* =========================================================================
     * Utilities
     * =======================================================================*/
    private FreqServerResponse httpPost(String url) throws IOException {
        return httpPost(url, null);
    }

	private FreqServerResponse httpPost(String url, String body) throws IOException {
        String result = HTTPUtils.doPost(url, body, new Integer(5000),
                new Integer(5000));
        return JAXB.unmarshal(
                new ByteArrayInputStream(result.getBytes()),
                FreqServerResponse.class);
    }
}
