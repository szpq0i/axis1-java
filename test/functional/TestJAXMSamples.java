/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package test.functional;

import junit.framework.TestCase;
import org.apache.axis.AxisFault;
import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;
import samples.jaxm.DelayedStockQuote;
import samples.jaxm.SOAPFaultTest;
import samples.jaxm.UddiPing;

import javax.xml.messaging.URLEndpoint;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import java.net.SocketException;


/**
 * Test the JAX-RPC compliance samples.
 */
public class TestJAXMSamples extends TestCase {
    static Log log = LogFactory.getLog(TestJAXMSamples.class.getName());

    public TestJAXMSamples(String name) {
        super(name);
    } // ctor

    public void testSOAPFaultTest () throws Exception {
        try {
            SOAPFaultTest.main(new String[0]);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception("Fault returned from test: " + t);
        }
    }

    public void testUddiPing() throws Exception {
        try {
            log.info("Testing JAXM UddiPing sample.");
            UddiPing.searchUDDI("IBM", "http://www-3.ibm.com/services/uddi/testregistry/inquiryapi");
            log.info("Test complete.");
        } catch (javax.xml.soap.SOAPException e) {
            Throwable t = e.getCause();
            if (t != null) {
                t.printStackTrace();
                if (t instanceof AxisFault) {
                    AxisFault af = (AxisFault) t;
                    if ((af.detail instanceof SocketException) ||
                        (af.getFaultCode().getLocalPart().equals("HTTP")) ) {
                        System.out.println("Connect failure caused JAXM UddiPing to be skipped.");
                        return;
                    }
                }
                throw new Exception("Fault returned from test: " + t);
            } else {
                e.printStackTrace();
                throw new Exception("Exception returned from test: " + e);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception("Fault returned from test: " + t);
        }
    } // testGetQuote

    public void testDelayedStockQuote() throws Exception {
        try {
            log.info("Testing JAXM DelayedStockQuote sample.");
            DelayedStockQuote stockQuote = new DelayedStockQuote();
            System.out.print("The last price for SUNW is " + stockQuote.getStockQuote("SUNW"));
            log.info("Test complete.");
        } catch (javax.xml.soap.SOAPException e) {
            Throwable t = e.getCause();
            if (t != null) {
                t.printStackTrace();
                if (t instanceof AxisFault) {
                    if (((AxisFault) t).detail instanceof SocketException) {
                        System.out.println("Connect failure caused JAXM DelayedStockQuote to be skipped.");
                        return;
                    }
                }
                throw new Exception("Fault returned from test: " + t);
            } else {
                e.printStackTrace();
                throw new Exception("Exception returned from test: " + e);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Exception("Fault returned from test: " + t);
        }
    } // testGetQuote
    
    public void testJWSFault() throws Exception {
        SOAPConnectionFactory scFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection con = scFactory.createConnection();

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();

        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();

        Name bodyName = envelope.createName("echo");
        SOAPBodyElement bodyElement = body.addBodyElement(bodyName);

        Name name = envelope.createName("arg0");
        SOAPElement symbol = bodyElement.addChildElement(name);
        symbol.addTextNode("Hello");

        URLEndpoint endpoint = new URLEndpoint("http://localhost:8080/jws/FaultTest.jws");
        SOAPMessage response = con.call(message, endpoint);
        SOAPBody respBody = response.getSOAPPart().getEnvelope().getBody();
        assertTrue(respBody.hasFault());
    }

    public static void main(String args[]) throws Exception {
        TestJAXMSamples tester = new TestJAXMSamples("tester");
        tester.testUddiPing();
    } // main
}


