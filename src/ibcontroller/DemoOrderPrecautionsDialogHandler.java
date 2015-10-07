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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;

class DemoOrderPrecautionsDialogHandler implements WindowHandler {
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    public void handleWindow(Window window, int eventID) {
        if( Utils.setCheckBoxSelected(window, "Don't display this message again.", true) &&
            Utils.clickButton(window, "OK") ) {
            Utils.logComponent(window);
            Utils.logToConsole("order precautions auto-clicked");
        } else {
            Utils.logError("could not close the order precautions window because we could not find one of the controls.");
        }
    }

    public boolean recogniseWindow(Window window) {
        boolean ret = false;
        final String Enable = "Enable";
        final String Disable = "Disable";
        if (! (window instanceof JDialog)) return false;
        String apiEnable = Settings.getString("DemoApiEnable", Disable);
        if (!apiEnable.equalsIgnoreCase(Enable)) return false;

        if (Utils.titleContains(window, "IB TWS (Demo System)")) {
            Utils.logToConsole("demo system window detected");
            if (Utils.findCheckBox(window, "Don't display this message again.") != null) {
                Utils.logToConsole("demo: found 'Don't display this message again.'");
                ret = true;
            } else {
                Utils.logToConsole("demo: unknown window");
            }
        }
        return ret;
    }
}

