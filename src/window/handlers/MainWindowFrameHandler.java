// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController. If not, see <http://www.gnu.org/licenses/>.

package window.handlers;

import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import window.WindowHandler;
import window.manager.MainWindowManager;

public class MainWindowFrameHandler implements WindowHandler {
  @Override
  public boolean filterEvent(Window window, int eventId) {
    switch (eventId) {
      case WindowEvent.WINDOW_OPENED:
        return true;
      default:
        return false;
    }
  }

  @Override
  public void handleWindow(Window window, int eventID) {
    if (eventID != WindowEvent.WINDOW_OPENED) return;

    MainWindowManager.mainWindowManager().setMainWindow((JFrame) window);
  }

  @Override
  public boolean recogniseWindow(Window window) {
    if (!(window instanceof JFrame)) return false;

    return SwingUtils.findMenuItemInAnyMenuBar(window, new String[] { "Help", "About Trader Workstation..." }) != null;
  }
}
