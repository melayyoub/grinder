// The Grinder
// Copyright (C) 2000  Paco Gomez
// Copyright (C) 2000  Philip Aston

// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.grinder.plugin.http;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.grinder.plugininterface.PluginContext;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.TestDefinition;
import net.grinder.util.FilenameFactory;
import net.grinder.util.GrinderException;
import net.grinder.util.GrinderProperties;


/**
 * Simple HTTP client benchmark.
 * 
 * @author Paco Gomez
 * @author Philip Aston
 * @version $Revision$
 */
public class HttpPlugin implements GrinderPlugin
{
    private PluginContext m_pluginContext = null;
    private FilenameFactory m_filenameFactory = null;
    private HashMap m_callData = new HashMap();
    private boolean m_logHTML = true;
    private HttpMsg m_httpMsg = null;
    private int m_currentIteration = 0; // How many times we've done all the URL's
    private final DateFormat m_dateFormat =
	new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz");

    /**
     * Inner class that holds the data for a call.
     */
    protected class CallData implements HttpRequestData
    {
	private String m_urlString;
	private  String m_okString;
	private long m_ifModifiedSince = -1;
	private String m_postString = null;
    
	public CallData(TestDefinition test) throws PluginException
	{
	    final GrinderProperties testParameters = test.getParameters();

	    try {
		m_urlString = testParameters.getMandatoryProperty("url");
	    }
	    catch (GrinderException e) {
		throw new PluginException(
		    "URL for Test " + test.getTestNumber() + " not specified",
		    e);
	    }

	    m_okString = testParameters.getProperty("ok", null);

	    final String ifModifiedSinceString =
		testParameters.getProperty("ifModifiedSince", null);

	    if (ifModifiedSinceString != null) {
		try {
		    final Date date =
			m_dateFormat.parse(ifModifiedSinceString);
	
		    m_ifModifiedSince = date.getTime();
		}
		catch (ParseException e) {
		    m_pluginContext.logError(
			"Couldn't parse ifModifiedSince date '" +
			ifModifiedSinceString + "'");
		}
	    }

	    final String postFilename =
		testParameters.getProperty("post", null);

	    if (postFilename != null) {
		try {
		    final FileReader in = new FileReader(postFilename);
		    final StringWriter writer = new StringWriter(512);
		    
		    char[] buffer = new char[4096];
		    int charsRead = 0;

		    while ((charsRead = in.read(buffer, 0, buffer.length)) > 0)
		    {
			writer.write(buffer, 0, charsRead);
		    }
		
		    in.close();
		    writer.close();
		    m_postString = writer.toString();
		}
		catch (IOException e) {
		    m_pluginContext.logError(
			"Could not read post data from " + postFilename);

		    e.printStackTrace(System.err);
		}
	    }	    
	}

	public String getURLString() { return m_urlString; }
	public String getContextURLString() { return null; }
	public String getPostString() { return m_postString; }
	public long getIfModifiedSince() { return m_ifModifiedSince; }
	public String getOKString() { return m_okString; }

	protected void setURLString(String s) { m_urlString = s; }
	protected void setPostString(String s) { m_postString = s; }
	protected void setIfModifiedSince(long l) { m_ifModifiedSince = l; }
	protected void setOKString(String s) { m_okString = s; }
    }

    /**
     * This method initializes the plug-in.
     */    
    public void initialize(PluginContext pluginContext)
	throws PluginException
    {
	m_pluginContext = pluginContext;

	final GrinderProperties parameters =
	    pluginContext.getPluginParameters();

	m_filenameFactory = pluginContext.getFilenameFactory();
    
	m_httpMsg = new HttpMsg(pluginContext,
				parameters.getBoolean("keepSession", false),
				parameters.getBoolean("followRedirects",
						      false));

	m_logHTML = parameters.getBoolean("logHTML", false);
    }

    public void beginCycle() throws PluginException
    {
	// Reset cookie if necessary.
	m_httpMsg.reset();      
    }

    /**
     * This method processes the URLs.
     */    
    public boolean doTest(TestDefinition test) throws PluginException
    {
	final Integer testNumber = test.getTestNumber();
	
	CallData callData = (CallData)m_callData.get(testNumber);
	
	if (callData == null) {
	    callData = createCallData(m_pluginContext, test);
	    m_callData.put(testNumber, callData);
	}
	
	// Do the call.
	final String page;

	m_pluginContext.startTimer();

	try {
	    page = m_httpMsg.sendRequest(callData);
	}
	catch (IOException e) {
	    throw new PluginException("HTTP IOException: " + e, e);
	}

	final String okString = callData.getOKString();

	final boolean error =
	    okString != null && page != null && page.indexOf(okString) == -1;
	
	if (m_logHTML || error) {
	    final String filename =
		m_filenameFactory.createFilename("page",
						 "_" + m_currentIteration +
						 "_" + testNumber + ".html");
	    try {
		final BufferedWriter htmlFile =
		    new BufferedWriter(new FileWriter(filename, false));

		htmlFile.write(page);
		htmlFile.close();
	    }
	    catch (IOException e) {
		throw new PluginException("Error writing to " + filename +
					  ": " + e, e);
	    }

	    if (error) {
		m_pluginContext.logError(
		    "The 'ok' string ('" + okString +
		    "') was not found in the page received. " +
		    "The output has been written to '" + filename + "'");
	    }
	}

	return !error;
    }

    /**
      * Give derived classes a chance to be interesting.
      */
    protected CallData createCallData(PluginContext pluginContext,
 				      TestDefinition test)
	throws PluginException
    {
 	return new CallData(test);
    }

    public void endCycle() throws PluginException
    {
	m_currentIteration++;
    }
}
