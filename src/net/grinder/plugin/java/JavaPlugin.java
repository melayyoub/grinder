// Copyright (C) 2002 Philip Aston
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

package net.grinder.plugin.java;

import net.grinder.common.GrinderException;
import net.grinder.common.Test;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.PluginProcessContext;
import net.grinder.plugininterface.PluginThreadCallbacks;
import net.grinder.plugininterface.PluginThreadContext;
import net.grinder.plugininterface.PluginTest;
import net.grinder.script.ScriptPluginContext;


/**
 * Java plugin.
 * 
 * @author Philip Aston
 * @version $Revision$
 **/
public class JavaPlugin implements GrinderPlugin
{
    public void initialize(PluginProcessContext processContext)
    {
    }

    public PluginThreadCallbacks createThreadCallbackHandler(
	PluginThreadContext threadContext)
    {
	return new JavaPluginThreadCallbacks();
    }

    private static class JavaPluginThreadCallbacks
	implements PluginThreadCallbacks
    {
	public void beginRun() throws PluginException
	{
	}

	public Object invokeTest(Test test, Object parameters)
	    throws PluginException
	{
	    return ((Invokeable)parameters).invoke();
	}

	public void endRun() throws PluginException
	{
	}
    }

    public final ScriptPluginContext getScriptPluginContext()
    {
	return null;
    }

    interface Invokeable 
    {
	public Object invoke();
    }
}
