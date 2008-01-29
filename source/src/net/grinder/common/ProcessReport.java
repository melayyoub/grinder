// Copyright (C) 2005 - 2008 Philip Aston
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

package net.grinder.common;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Common interface for enquiring about a process.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public interface ProcessReport {

  /**
   * Constant representing the "started" state.
   */
  short STATE_STARTED = 1;

  /**
   * Constant representing the "running" state.
   */
  short STATE_RUNNING = 2;

  /**
   * Constant representing the "finished" state.
   */
  short STATE_FINISHED = 3;

  /**
   * Constant representing the "unknown" state.
   */
  short STATE_UNKNOWN = 4;

  /**
   * Return the unique process identity.
   *
   * @return The process identity.
   */
  ProcessIdentity getIdentity();

  /**
   * Return the process status.
   *
   * @return One of {@link #STATE_STARTED}, {@link #STATE_RUNNING},
   * {@link #STATE_FINISHED}.
   */
  short getState();

  /**
   * The identity of a process.
   *
   * <p>Implementations should define equality so that instances are equal if
   * and only they represent the same process.</p>
   *
   * @author Philip Aston
   * @version $Revision$
   */
  interface ProcessIdentity extends Serializable {

    /**
     * Return the process name.
     *
     * @return The process name.
     */
    String getName();
  }

  /**
   * Comparator that compares ProcessReports by state, then by name.
   */
  final class StateThenNameComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      final ProcessReport processReport1 = (ProcessReport)o1;
      final ProcessReport processReport2 = (ProcessReport)o2;

      final int compareState =
        processReport1.getState() - processReport2.getState();

      if (compareState == 0) {
        return processReport1.getIdentity().getName().compareTo(
               processReport2.getIdentity().getName());
      }
      else {
        return compareState;
      }
    }
  }
}
