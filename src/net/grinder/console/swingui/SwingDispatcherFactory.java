// Copyright (C) 2005 Philip Aston
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

package net.grinder.console.swingui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import net.grinder.console.common.ErrorHandler;


/**
 * Factory for dynamic proxies that dispatch work in the Swing thread.
 *
 * @author Philip Aston
 * @version $Revision$
 */
final class SwingDispatcherFactory {

  private final ErrorHandler m_errorHandler;

  public SwingDispatcherFactory(ErrorHandler errorHandler) {
    m_errorHandler = errorHandler;
  }

  public Object create(final Object delegate) {

    final InvocationHandler invocationHandler =
      new InvocationHandler() {
        public Object invoke(Object proxy,
                             final Method method,
                             final Object[] args)
          throws Throwable {

          SwingUtilities.invokeLater(
            new Runnable() {
              public void run() {
                try {
                  method.invoke(delegate, args);
                }
                catch (InvocationTargetException e) {
                  m_errorHandler.handleException(e.getCause());
                }
                catch (IllegalArgumentException e) {
                  throw new AssertionError(e);
                }
                catch (IllegalAccessException e) {
                  throw new AssertionError(e);
                }
              }
            });

          return null;
        }
      };

    final Class delegateClass = delegate.getClass();

    return Proxy.newProxyInstance(delegateClass.getClassLoader(),
                                  getAllInterfaces(delegateClass),
                                  invocationHandler);
  }

  private static Class[] getAllInterfaces(Class theClass) {
    final List interfaces = new ArrayList();

    if (theClass.isInterface()) {
      interfaces.add(theClass);
    }

    Class c = theClass;

    do {
      final Class[] moreInterfaces = c.getInterfaces();

      if (moreInterfaces != null) {
        interfaces.addAll(Arrays.asList(moreInterfaces));
      }

      c = c.getSuperclass();
    }
    while (c != null);

    return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
  }
}
