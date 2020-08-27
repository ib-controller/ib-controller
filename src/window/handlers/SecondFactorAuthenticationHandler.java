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
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import io.github.ma1uta.matrix.client.StandaloneClient;
import utils.Settings;
import utils.SwingUtils;
import window.WindowHandler;

public class SecondFactorAuthenticationHandler implements WindowHandler {
  Instant lastWindowOpened = Instant.MIN;
  Pattern codePattern = Pattern.compile(
      "<html>If you did not receive the notification, enter this challenge in the <br>IB Key app: &nbsp;&nbsp;&nbsp;<strong style='font-size:110%;'>([0-9 ]+)</strong>&nbsp;&nbsp;&nbsp; Then enter the response below and click OK.</html>");
  StandaloneClient mxClient;

  public SecondFactorAuthenticationHandler() {
    mxClient =
        new StandaloneClient.Builder().domain(Settings.settings().getString("MatrixServerName", ""))
            .userId(Settings.settings().getString("MatrixBotName", "")).build();
    mxClient.auth().login(
        Settings.settings().getString("MatrixBotName", ""),
        Settings.settings().getString("MatrixBotAuth", "").toCharArray());
  }

  @Override
  public boolean filterEvent(Window window, int eventId) {
    switch (eventId) {
      case WindowEvent.WINDOW_OPENED:
        lastWindowOpened = Instant.now();
        return true;
      case WindowEvent.WINDOW_CLOSED:
        if (Duration.between(lastWindowOpened, Instant.now())
            .compareTo(Duration.ofSeconds(3)) < 0) {
          System.exit(999);
        }
        break;
    }
    return false;
  }

  @Override
  public void handleWindow(Window window, int eventID) {
    Matcher matcher =
        codePattern.matcher(
            SwingUtils
                .findLabel(
                    window,
                    "If you did not receive the notification, enter this challenge in the")
                .getText());
    if (!matcher.matches()) {
      System.exit(998);
    }
    String authCode = matcher.group(1);
    mxClient.room().joinedRooms().getJoinedRooms().stream().forEach(
        room -> mxClient.eventAsync()
            .sendMessage(room, "@here IBApi need 2factor auth code is " + authCode));

    SwingUtils.setTextField(window, 0, "1234");
    SwingUtils.setTextField(window, 1, "4567");
  }

  @Override
  public boolean recogniseWindow(Window window) {
    if (!(window instanceof JDialog))
      return false;

    return (SwingUtils.titleContains(window, "Second Factor Authentication"));
  }
}
