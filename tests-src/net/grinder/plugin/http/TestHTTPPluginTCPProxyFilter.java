// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000, 2001, 2002 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.plugin.http;

import junit.framework.TestCase;
import junit.swingui.TestRunner;
//import junit.textui.TestRunner;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import net.grinder.tools.tcpproxy.ConnectionDetails;


/**
 * Unit test case for <code>HttpPluginTCPProxyFilter</code>.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class TestHTTPPluginTCPProxyFilter extends TestCase
{
    public static void main(String[] args)
    {
	TestRunner.run(TestHTTPPluginTCPProxyFilter.class);
    }

    public TestHTTPPluginTCPProxyFilter(String name)
    {
	super(name);
    }

    private HTTPPluginTCPProxyFilter m_filter;

    private final StringWriter m_filterStringWriter = new StringWriter();
    private final PrintWriter m_filterPrintWriter =
	new PrintWriter(m_filterStringWriter);

    private final ConnectionDetails m_connection1 =
	new ConnectionDetails("alpha", 99, "beta", 1234, false);

    private final ConnectionDetails m_connection2 =
	new ConnectionDetails("gamma", 99, "delta", 777, true);

    protected void setUp() throws Exception
    {
	m_filter = new HTTPPluginTCPProxyFilter(m_filterPrintWriter);
    }

    public void testSimpleGet() throws Exception
    {
	m_filter.connectionOpened(m_connection1);
	doHandle(m_connection1, "data/goodGET1");
	m_filter.connectionClosed(m_connection1);

	m_filterPrintWriter.flush();
	final String result =  m_filterStringWriter.toString();

	final String[] unorderedExpectation = {
	    // Its a reasonable test script.
	    "grinder.processes=1",
	    "grinder.threads=1",
	    "grinder.cycles=0",
	    "grinder.plugin=net.grinder.plugin.http.HttpPlugin",

	    // The GET
	    "grinder.test0.parameter.url=http://beta:1234/over/it",
	    "grinder.test0.sleepTime=",
	    "grinder.test0.description=it",
	};

	assertStringContains(result, unorderedExpectation);
	
	final String[] orderedExpectation = {
	    "# The Grinder version",
	    "# Script generated by the TCPProxy at",
	    "# New connection: " + m_connection1.getDescription(),
	    "grinder.test0.parameter.url=http://beta:1234/over/it",
	};

	assertStringContainsInOrder(result, orderedExpectation);
    }

    public void testMultipleGets() throws Exception
    {
	m_filter.connectionOpened(m_connection1);
	m_filter.connectionOpened(m_connection2);
	doHandle(m_connection1, "data/goodGET1");
	m_filter.connectionClosed(m_connection1);
	m_filter.connectionOpened(m_connection1);
	doHandle(m_connection1, "data/goodGET1");
	doHandle(m_connection1, "data/goodGET2");
	m_filter.connectionClosed(m_connection1);
	doHandle(m_connection2, "data/goodGET2");
	m_filter.connectionClosed(m_connection2);

	m_filterPrintWriter.flush();
	final String result =  m_filterStringWriter.toString();

	System.out.println(result);

	final String[] unorderedExpectation = {
	    "grinder.test0.parameter.url=http://beta:1234/over/it",
	    "grinder.test0.sleepTime=",
	    "grinder.test0.description=it",
	    "grinder.test1.parameter.url=http://beta:1234/over/it",
	    "grinder.test1.sleepTime=",
	    "grinder.test1.description=it",
	    "grinder.test2.parameter.url=http://beta:1234/out.of/here?query",
	    "grinder.test2.sleepTime=",
	    "grinder.test2.description=here",
	    "grinder.test3.parameter.url=https://delta:777/out.of/here?query",
	    "grinder.test3.sleepTime=",
	    "grinder.test3.description=here",
	};

	assertStringContains(result, unorderedExpectation);
	
	final String[] orderedExpectation = {
	    "# New connection: " + m_connection1.getDescription(),
	    "# New connection: " + m_connection2.getDescription(),
	    "grinder.test0.parameter.url=http://beta:1234/over/it",
	    "# New connection: " + m_connection1.getDescription(),
	    "grinder.test1.parameter.url=http://beta:1234/over/it",
	};

	assertStringContainsInOrder(result, orderedExpectation);
    }

    public void testConnectionOpenAndClose() throws Exception
    {
	m_filter.connectionOpened(m_connection1);
	m_filter.connectionClosed(m_connection1);
	m_filter.connectionOpened(m_connection2);
	m_filter.connectionClosed(m_connection2);

	m_filterPrintWriter.flush();
	final String result =  m_filterStringWriter.toString();

	final String[] orderedExpectation = {
	    "# New connection: " + m_connection1.getDescription(),
	    "# New connection: " + m_connection2.getDescription(),
	};

	assertStringContainsInOrder(result, orderedExpectation);

	try {
	    m_filter.connectionClosed(m_connection2);
	    fail("Expected IllegalArgumentException");
	}
	catch (IllegalArgumentException e) {
	}
    }

    public void testPOSTParsing() throws Exception
    {
	m_filter.connectionOpened(m_connection1);
	doHandle(m_connection1, "data/goodPOST1");
	m_filter.connectionClosed(m_connection1);

	m_filterPrintWriter.flush();
	final String result =  m_filterStringWriter.toString();

	final String[] expectation = {
	    "grinder.test0.parameter.url=http://beta:1234/somewhere/over/the/rainbow",
	    "grinder.test0.parameter.post=http-plugin-tcpproxy-post-0",
	    "grinder.test0.description=rainbow",
	    "grinder.test0.sleepTime=",
	};
	
	assertStringContains(result, expectation);

	final File postFile = new File("http-plugin-tcpproxy-post-0");
	assertEquals(32, postFile.length());
	postFile.delete();
    }

    private void doHandle(ConnectionDetails connectionDetails,
			  String resource) throws Exception
    {
	final byte[] testData = readData(resource);
	m_filter.handle(connectionDetails, testData, testData.length);
    }

    private byte[] readData(String resource) throws IOException
    {
	final InputStream resourceStream =
	    getClass().getResourceAsStream(resource);

	if (resourceStream == null) {
	    throw new IOException("Resource: " + resource + " not found");
	}
	
	final InputStream in = new BufferedInputStream(resourceStream);
	final ByteArrayOutputStream out = new ByteArrayOutputStream();

	int b;

	while ((b = in.read()) > -1) {
	    out.write(b);
	}

	in.close();
	out.close();

	return out.toByteArray();
    }

    private static void assertStringContains(String s, String[] matches) 
    {
	for (int i=0; i<matches.length; ++i) {
	    assertStringContains(s, matches[i]);
	}
    }

    private static void assertStringContainsInOrder(String s,
						    String[] matches) 
    {
	int lastMatch = 0;

	for (int i=0; i<matches.length; ++i) {
	    lastMatch = assertStringContains(s, lastMatch, matches[i]);
	}
    }

    private static void assertStringContains(String s, String match) 
    {
	assertStringContains(s, 0, match);
    }

    private static int assertStringContains(String s, int from, String match) 
    {
	final int matchIndex = s.indexOf(match, from);
	
	assertTrue("String contains '" + match + "'", matchIndex > -1);

	return matchIndex + match.length();
    }

    private static void assertByteArraysEqual(byte[] a, byte[] b) 
    {
	assertTrue("Arrays of equal length", a.length == b.length);

	for (int i=0; i<a.length; ++i) {
	    assertTrue("Byte " + i + " matches", a[i] == b[i]);
	}
    }
}
