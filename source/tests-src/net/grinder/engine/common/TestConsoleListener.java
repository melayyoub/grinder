// Copyright (C) 2001, 2002, 2003, 2004, 2005, 2006 Philip Aston
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

package net.grinder.engine.common;

import java.io.File;
import java.io.Serializable;

import junit.framework.TestCase;

import net.grinder.common.Logger;
import net.grinder.common.LoggerStubFactory;
import net.grinder.communication.CommunicationException;
import net.grinder.communication.Message;
import net.grinder.communication.MessageHandlerChain.MessageHandler;
import net.grinder.engine.messages.ResetGrinderMessage;
import net.grinder.engine.messages.StartGrinderMessage;
import net.grinder.engine.messages.StopGrinderMessage;
import net.grinder.util.thread.Monitor;


/**
 * Unit test case for <code>ConsoleListener</code>.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class TestConsoleListener extends TestCase {

  private final LoggerStubFactory m_loggerFactory = new LoggerStubFactory();
  private final Logger m_logger = m_loggerFactory.getLogger();

  protected void setUp() throws Exception {
    m_loggerFactory.resetCallHistory();
  }

  public void testConstruction() throws Exception {
    final Monitor myMonitor = new Monitor();

    final ConsoleListener listener0 =
      new ConsoleListener(myMonitor, m_logger);

    final MessageHandler messageHandler = listener0.getMessageHandler();
    assertNotNull(messageHandler);

    m_loggerFactory.assertNoMoreCalls();
  }

  public void testSendNotification() throws Exception {
    final Monitor myMonitor = new Monitor();
    final ConsoleListener listener = new ConsoleListener(myMonitor, m_logger);

    final MessageHandler messageHandler = listener.getMessageHandler();

    final WaitForNotification notified = new WaitForNotification(myMonitor);

    assertTrue(messageHandler.process(new StopGrinderMessage()));

    assertTrue(notified.wasNotified());
  }

  public void testCheckForMessageAndReceive() throws Exception {

    final Monitor myMonitor = new Monitor();
    final ConsoleListener listener = new ConsoleListener(myMonitor, m_logger);

    assertFalse(listener.checkForMessage(ConsoleListener.ANY));
    assertFalse(listener.checkForMessage(ConsoleListener.RESET |
                                         ConsoleListener.SHUTDOWN));
    assertFalse(listener.checkForMessage(ConsoleListener.SHUTDOWN));

    final MessageHandler messageHandler = listener.getMessageHandler();

    assertTrue(
      messageHandler.process(new StartGrinderMessage(new File("foo"))));
    assertFalse(messageHandler.process(new MyMessage()));
    assertTrue(messageHandler.process(new ResetGrinderMessage()));

    m_loggerFactory.assertSuccess("output", new Class[] { String.class });
    m_loggerFactory.assertSuccess("output", new Class[] { String.class });
    m_loggerFactory.assertNoMoreCalls();

    assertFalse(listener.checkForMessage(ConsoleListener.ANY ^
                                         (ConsoleListener.START |
                                          ConsoleListener.RESET)));
    assertTrue(listener.checkForMessage(ConsoleListener.START |
                                        ConsoleListener.STOP));
    assertTrue(listener.received(ConsoleListener.START));
    assertFalse(listener.received(ConsoleListener.STOP));
    assertTrue(listener.received(ConsoleListener.ANY));
    assertFalse(listener.received(ConsoleListener.STOP |
                                 ConsoleListener.RESET));
    assertFalse(listener.received(ConsoleListener.STOP));
    assertFalse(listener.received(ConsoleListener.SHUTDOWN));
    assertFalse(listener.received(ConsoleListener.RESET));

    assertFalse(listener.checkForMessage(ConsoleListener.START));
    assertFalse(listener.received(ConsoleListener.ANY));
    assertFalse(listener.received(ConsoleListener.START));

    assertTrue(listener.checkForMessage(ConsoleListener.RESET));
    assertTrue(listener.received(ConsoleListener.RESET));
    assertTrue(listener.received(ConsoleListener.RESET));

    assertFalse(listener.checkForMessage(ConsoleListener.RESET));
    assertFalse(listener.received(ConsoleListener.RESET));

    assertTrue(
      messageHandler.process(new StartGrinderMessage(new File("bah"))));
    assertTrue(messageHandler.process(new ResetGrinderMessage()));

    m_loggerFactory.assertSuccess("output", new Class[] { String.class });
    m_loggerFactory.assertSuccess("output", new Class[] { String.class });
    m_loggerFactory.assertNoMoreCalls();

    assertTrue(listener.checkForMessage(ConsoleListener.RESET |
                                        ConsoleListener.START));
    assertTrue(messageHandler.process(new ResetGrinderMessage()));

    m_loggerFactory.assertSuccess("output", new Class[] { String.class });
    m_loggerFactory.assertNoMoreCalls();

    assertTrue(listener.checkForMessage(ConsoleListener.RESET |
                                        ConsoleListener.START));
    assertTrue(listener.received(ConsoleListener.RESET));
    assertFalse(listener.checkForMessage(ConsoleListener.RESET |
                                         ConsoleListener.START));
    assertFalse(listener.received(ConsoleListener.START));

    messageHandler.shutdown();

    assertTrue(listener.checkForMessage(ConsoleListener.SHUTDOWN));
    assertTrue(listener.received(ConsoleListener.SHUTDOWN));
    assertTrue(listener.received(ConsoleListener.SHUTDOWN));
  }

  public void testDiscardMessages() throws Exception {
    final Monitor myMonitor = new Monitor();
    final ConsoleListener listener = new ConsoleListener(myMonitor, m_logger);

    assertFalse(listener.checkForMessage(ConsoleListener.ANY));
    assertFalse(listener.checkForMessage(ConsoleListener.RESET |
                                         ConsoleListener.SHUTDOWN));
    assertFalse(listener.checkForMessage(ConsoleListener.SHUTDOWN));

    final MessageHandler messageHandler = listener.getMessageHandler();

    listener.discardMessages(ConsoleListener.ANY);

    assertTrue(messageHandler.process(new StartGrinderMessage(new File("x"))));
    assertFalse(messageHandler.process(new MyMessage()));
    assertTrue(messageHandler.process(new ResetGrinderMessage()));

    assertTrue(listener.checkForMessage(ConsoleListener.START |
                                        ConsoleListener.STOP));
    assertTrue(listener.received(ConsoleListener.START));
    assertFalse(listener.received(ConsoleListener.RESET));

    listener.discardMessages(ConsoleListener.RESET);

    assertFalse(listener.checkForMessage(ConsoleListener.RESET));
    assertFalse(listener.received(ConsoleListener.RESET));

    assertTrue(messageHandler.process(new ResetGrinderMessage()));

    assertTrue(listener.checkForMessage(ConsoleListener.RESET));

    listener.discardMessages(ConsoleListener.RESET);

    assertFalse(listener.received(ConsoleListener.RESET));

    listener.discardMessages(ConsoleListener.RESET);

    assertFalse(listener.received(ConsoleListener.RESET));

    messageHandler.shutdown();

    assertTrue(listener.checkForMessage(ConsoleListener.SHUTDOWN));
    assertTrue(listener.received(ConsoleListener.SHUTDOWN));
    listener.discardMessages(ConsoleListener.SHUTDOWN);
    assertFalse(listener.received(ConsoleListener.SHUTDOWN));
  }

  public void testWaitForMessage() throws Exception {
    final Monitor myMonitor = new Monitor();
    final ConsoleListener listener = new ConsoleListener(myMonitor, m_logger);
    final MessageHandler messageHandler = listener.getMessageHandler();

    final Thread t = new Thread() {
        public void run() {
          synchronized (myMonitor) {} // Wait until we're listening.
          try {
            messageHandler.process(new StartGrinderMessage(new File("lah")));
          }
          catch (CommunicationException e) {
            e.printStackTrace();
          }
        }
    };

    synchronized (myMonitor) {
      t.start();
      listener.waitForMessage();
    }

    assertTrue(listener.received(ConsoleListener.START));
  }

  private static final class MyMessage implements Message, Serializable {
  }

  public void testShutdown() throws Exception {

    final Monitor myMonitor = new Monitor();
    final ConsoleListener listener = new ConsoleListener(myMonitor, m_logger);
    final MessageHandler messageHandler = listener.getMessageHandler();

    final WaitForNotification notified = new WaitForNotification(myMonitor);

    messageHandler.shutdown();

    assertTrue(notified.wasNotified());

    m_loggerFactory.assertSuccess("output",
                                  new Class[] { String.class, Integer.class });

    m_loggerFactory.assertNoMoreCalls();

    assertFalse(listener.checkForMessage(ConsoleListener.ANY ^
                                          ConsoleListener.SHUTDOWN));
    assertTrue(listener.checkForMessage(ConsoleListener.SHUTDOWN));
  }

  private static class WaitForNotification implements Runnable {
    private final Thread m_thread;
    private final Object m_monitor;
    private boolean m_started = false;
    private boolean m_notified = false;

    public WaitForNotification(Object monitor) throws InterruptedException {
      m_monitor = monitor;

      m_thread = new Thread(this);
      m_thread.start();

      synchronized (m_monitor) {
        while (!m_started) {
          m_monitor.wait();
        }
      }
    }

    public boolean wasNotified() throws InterruptedException {
      m_thread.join();

      return m_notified;
    }

    public final void run() {
      synchronized(m_monitor) {
        final long startTime = System.currentTimeMillis();
        final long maximumTime = 10000;
        m_started = true;
        m_monitor.notifyAll();

        try {
          m_monitor.wait(maximumTime);

          if (System.currentTimeMillis() - startTime < maximumTime) {
            m_notified = true;
          }
        }
        catch (InterruptedException e) {
        }
      }
    }
  }

  public void testGetLastStartGrinderMessage() throws Exception {

    final ConsoleListener listener =
      new ConsoleListener(new Monitor(), m_logger);

    final Message m1 = new StartGrinderMessage(new File("a"));
    final Message m2 = new StartGrinderMessage(new File("a"));
    final Message m3 = new MyMessage();

    final MessageHandler messageHandler = listener.getMessageHandler();

    assertNull(listener.getLastStartGrinderMessage());

    assertTrue(messageHandler.process(m1));
    assertEquals(m1, listener.getLastStartGrinderMessage());

    assertFalse(messageHandler.process(m3));
    assertEquals(m1, listener.getLastStartGrinderMessage());

    assertTrue(messageHandler.process(m2));
    assertEquals(m2, listener.getLastStartGrinderMessage());
  }
}
