// The Grinder
// Copyright (C) 2000, 2001 Paco Gomez
// Copyright (C) 2000, 2001 Philip Aston

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

package net.grinder.console;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.plugininterface.Test;
import net.grinder.util.GrinderException;
import net.grinder.util.GrinderProperties;
import net.grinder.util.PropertiesHelper;

import net.grinder.console.swing.ConsoleUI;


/**
 * This is the entry point of The Grinder Console.
 * 
 * @author Paco Gomez
 * @author Philip Aston
 * @version $Revision$
 */
public class Console
{
    private final ConsoleCommunication m_communication;
    private final Map m_tests = new TreeMap();
    private final ConsoleUI m_userInterface;

    //ms between console refreshes
    private int REFRESH_INTERVAL = 500;     
                 
    public Console() throws GrinderException
    {
	final GrinderProperties properties = GrinderProperties.getProperties();
	final PropertiesHelper propertiesHelper = new PropertiesHelper();

	m_communication = new ConsoleCommunication(properties);

	final GrinderPlugin grinderPlugin =
	    propertiesHelper.instantiatePlugin(null);
	// Shove the tests into a TreeMap so that they're ordered.
	final Iterator testSetIterator =
	    propertiesHelper.getTestSet(grinderPlugin).iterator();

	while (testSetIterator.hasNext())
	{
	    final Test test = (Test)testSetIterator.next();
	    final Integer testNumber = test.getTestNumber();
	    m_tests.put(test.getTestNumber(), test);
	}

	final ActionListener startHandler =
	    new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
			System.out.println("Starting Grinder...");
			    
			try {
			    m_communication.sendStartMessage();
			}
			catch (GrinderException e) {
			    System.err.println(
				"Could not send start message: " + e);
			    e.printStackTrace();
			}
		    }
		};

	m_userInterface = new ConsoleUI(m_tests.values(), startHandler);
    }
    
    public void run() throws GrinderException
    {
        System.out.println("Grinder Console started.");        

	while (true) {
	    try {
		Thread.sleep(100);
	    }
	    catch (Exception e){
	    }
	}
	
	
	// Create the UI
	/*        b.addActionListener(this);
        
	int n = m_tests.size();
	    
        final GraphStatInfo[] graphStatInfoArray = new GraphStatInfo[n];
        final StatInfo[] statInfoArray = new StatInfo[n];

	final Iterator testIterator = m_tests.values().iterator();
	int i = 0;
	
	while (testIterator.hasNext()) {
	    final Test test = (Test)testIterator.next();

            graphStatInfoArray[i] = new GraphStatInfo(test.toString(), 0, 0);
            p.add(graphStatInfoArray[i]);
            statInfoArray[i] = new StatInfo(0,0);
	    i++;
        }
        
	*/
       
	// Event loop.
	/*        MsgReader mr =
	    new MsgReader(statInfoArray,
			  m_properties.getMandatoryProperty(
			      "grinder.console.multicastAddress"),
			  m_properties.getMandatoryInt(
			      "grinder.console.multicastPort"));
	
        while (true) {
            try {
                Thread.sleep(REFRESH_INTERVAL);
                
                for (int i=0; i<statInfoArray.length; i++){
                    graphStatInfoArray[i].add(statInfoArray[i]._art);
                    graphStatInfoArray[i].update(statInfoArray[i]);
                }
            }
            catch(Exception e){
                System.err.println(e);
            }           
        }
	*/
    }

}
