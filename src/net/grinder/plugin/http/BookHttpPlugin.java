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

import java.text.DecimalFormat;

import net.grinder.plugininterface.PluginContext;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.TestDefinition;


/**
 * @author Philip Aston
 * @version $Revision$
 */
public class BookHttpPlugin extends HttpPlugin {

    private final static String TEMPLATE_STRING = "$GRINDER_VARIABLE";

    protected class CallData extends HttpPlugin.CallData
    {
	private String m_phone = "2000";
	private DecimalFormat m_twoDigitsFormat = new DecimalFormat("00");

	public CallData(PluginContext pluginContext, TestDefinition test)
	    throws PluginException
	{
	    super(test);

	    final String original = getURLString();

	    if (original != null) {
		final int index = original.indexOf(TEMPLATE_STRING);

		if (index >= 0) {
		    final StringBuffer buffer = new StringBuffer();
		    
		    buffer.append(original.substring(0, index));
		    buffer.append("555");
		    buffer.append(pluginContext.getHostIDString());
		    buffer.append(pluginContext.getProcessIDString());
		    buffer.append(m_twoDigitsFormat.format(
				      pluginContext.getThreadID()));
		    buffer.append(original.substring(
				      index + TEMPLATE_STRING.length()));

		    setURLString(buffer.toString());
		}
	    }
	}
    }

    protected HttpPlugin.CallData createCallData(PluginContext gc,
						 TestDefinition test)
	throws PluginException
    {
	return new CallData(gc, test);
    }
}
