// Copyright (C) 2004 Philip Aston
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

package net.grinder.testutility;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import net.grinder.common.GrinderException;


/**
 * File utilties missing from Java.
 *
 * @author    Philip Aston
 */
public class FileUtilities extends Assert {

  public static void setCanRead(File file, boolean canRead) throws Exception {

    final String[] commandArray = new String[] {
      "chmod",
      canRead ? "ugo+r" : "ugo-r",
      file.getAbsolutePath(),
    };

    final Process process;

    try {
      process = Runtime.getRuntime().exec(commandArray);
    }
    catch (IOException e) {
      throw new GrinderException(
        "Couldn't chmod: perhaps you should install cygwin or patch this " +
        "test for your platform?",
        e);
    }
    
    process.waitFor();

    assertEquals("chmod suceeded", 0, process.exitValue());
  }
}