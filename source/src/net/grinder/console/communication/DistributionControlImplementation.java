// Copyright (C) 2007 - 2008 Philip Aston
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
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console.communication;

import net.grinder.messages.agent.CacheHighWaterMark;
import net.grinder.messages.agent.ClearCacheMessage;
import net.grinder.messages.agent.DistributeFileMessage;
import net.grinder.messages.agent.DistributionCacheCheckpointMessage;
import net.grinder.util.FileContents;


/**
 * Implementation of {@link DistributionControl}.
 *
 * @author Philip Aston
 * @version $Revision:$
 */
public class DistributionControlImplementation
  implements DistributionControl {

  private final ConsoleCommunication m_consoleCommunication;
  private final AgentFileCacheState m_agentFileCacheState;

  /**
   * Constructor.
   *
   * @param consoleCommunication
   *          The console communication handler.
   * @param agentFileCacheState
   *          Something that knows the state of the agent file caches.
   */
  public DistributionControlImplementation(
    ConsoleCommunication consoleCommunication,
    AgentFileCacheState agentFileCacheState) {
      m_consoleCommunication = consoleCommunication;
      m_agentFileCacheState = agentFileCacheState;
  }

  /**
   * Signal the agent processes that are less up to date than the given water
   * mark to clear their file caches.
   *
   * @param cacheHighWaterMark
   *            The water mark. In practice, its associated time will be 0, so
   *            agents with caches for out of date cache parameters will be
   *            cleared.
   */
  public void clearFileCaches(CacheHighWaterMark cacheHighWaterMark) {
    m_consoleCommunication.sendToAddressedAgents(
      m_agentFileCacheState.agentsWithOutOfDateCaches(cacheHighWaterMark),
      new ClearCacheMessage());
  }

  /**
   * Send a file to the file caches that are less up to date than the given
   * high water mark.
   *
   * @param fileContents The file contents.
   * @param cacheHighWaterMark The high water mark.
   */
  public void sendFile(FileContents fileContents,
                       CacheHighWaterMark cacheHighWaterMark) {
    m_consoleCommunication.sendToAddressedAgents(
      m_agentFileCacheState.agentsWithOutOfDateCaches(cacheHighWaterMark),
      new DistributeFileMessage(fileContents));
  }

  /**
   * Inform agent processes of a checkpoint of the cache state.
   *
   * @param highWaterMark
   *            A checkpoint of the cache state.
   */
  public void setHighWaterMark(CacheHighWaterMark highWaterMark) {
    m_consoleCommunication.sendToAgents(
      new DistributionCacheCheckpointMessage(highWaterMark));
  }
}
