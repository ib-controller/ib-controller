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

package ibcontroller;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import utils.Encryptor;
import utils.Settings;
import utils.Utils;
import window.TwsListener;
import window.TwsSettingsSaver;
import window.WindowHandler;
import window.handlers.AcceptIncomingConnectionDialogHandler;
import window.handlers.ApiChangeConfirmationDialogHandler;
import window.handlers.BlindTradingWarningDialogHandler;
import window.handlers.ExistingSessionDetectedDialogHandler;
import window.handlers.ExitConfirmationDialogHandler;
import window.handlers.ExitSessionFrameHandler;
import window.handlers.GatewayLoginFrameHandler;
import window.handlers.GatewayMainWindowFrameHandler;
import window.handlers.GlobalConfigurationDialogHandler;
import window.handlers.LoginFrameHandler;
import window.handlers.MainWindowFrameHandler;
import window.handlers.NSEComplianceFrameHandler;
import window.handlers.NewerVersionDialogHandler;
import window.handlers.NewerVersionFrameHandler;
import window.handlers.NonBrokerageAccountDialogHandler;
import window.handlers.NotCurrentlyAvailableDialogHandler;
import window.handlers.PasswordExpiryWarningFrameHandler;
import window.handlers.ReloginDialogHandler;
import window.handlers.SecurityCodeDialogHandler;
import window.handlers.SplashFrameHandler;
import window.handlers.TipOfTheDayDialogHandler;
import window.handlers.TradesFrameHandler;
import window.interactions.ConfigurationTask;
import window.interactions.ConfigureTwsApiPortTask;
import window.interactions.DefaultSettings;
import window.interactions.StopTask;
import window.manager.ConfigDialogManager;
import window.manager.DefaultLoginManager;
import window.manager.DefaultMainWindowManager;
import window.manager.DefaultTradingModeManager;
import window.manager.JtsIniManager;
import window.manager.LoginManager;
import window.manager.MainWindowManager;
import window.manager.TradingModeManager;

/**
 * @author stevek
 *
 *         This is our way of automating the TWS app so it does not require
 *         human interaction. IBController is a class whose main starts up the
 *         TWS api, and which monitors the application for certain events, such
 *         as the login dialog, after which it can automatically respond to
 *         these events. Upon seeing the login dialog, it fills out the username
 *         and pwd and presses the button. Upon seeing the "allow incoming
 *         connection dialog it presses the yes button.
 *
 *         This code is based original code by Ken Geis (ken_geis@telocity.com).
 */

public class IBController {

  private final ExecutorService cachedThreadPool;
  private final ScheduledExecutorService scheduledExecutorService;

  /**
   * starts up the TWS app.
   * 
   * @param args
   *          - If length == 1, then args[0] is the path to the ini file. If
   *          length == 0, we assume that the ini file is located in the current
   *          user directory in a file called "IBController.ini". If length == 2
   *          and args[0] is "encrypt", we print out the encryption of args[1].
   * @throws java.lang.Exception
   */

  private IBController(String[] args, Encryptor encryptor) throws Exception {
    cachedThreadPool = Executors.newCachedThreadPool();
    scheduledExecutorService = Executors.newScheduledThreadPool(4);
    setupDefaultEnvironment(args, false);
  }

  public static void main(final String[] args) throws Exception {
    Encryptor encryptor = new Encryptor();
    checkArguments(args, encryptor);
    IBController ibController = new IBController(args, encryptor);
    ibController.load();
  }

  void setupDefaultEnvironment(final String[] args, final boolean isGateway) throws Exception {
    Settings.initialise(new DefaultSettings(args));
    LoginManager.initialise(new DefaultLoginManager(args));
    MainWindowManager.initialise(new DefaultMainWindowManager(isGateway));
    TradingModeManager.initialise(new DefaultTradingModeManager(args));
  }

  static void checkArguments(String[] args, Encryptor encryptor) {
    /**
     * Allowable parameter combinations:
     * 
     * 1. No parameters
     * 
     * 2. ENCRYPT <password>
     * 
     * 3. <iniFile> [<tradingMode>]
     * 
     * 4. <iniFile> <apiUserName> <apiPassword> [<tradingMode>]
     * 
     * 5. <iniFile> <fixUserName> <fixPassword> <apiUserName> <apiPassword>
     * [<tradingMode>]
     * 
     * where:
     * 
     * <iniFile> ::= NULL | path-and-filename-of-.ini-file
     * 
     * <tradingMode> ::= blank | LIVETRADING | PAPERTRADING
     * 
     * <apiUserName> ::= blank | username-for-TWS
     * 
     * <apiPassword> ::= blank | password-for-TWS
     * 
     * <fixUserName> ::= blank | username-for-FIX-CTCI-Gateway
     * 
     * <fixPassword> ::= blank | password-for-FIX-CTCI-Gateway
     * 
     */
    if (args.length == 2) {
      if (args[0].equalsIgnoreCase("encrypt")) {
        Utils.logRawToConsole("========================================================================");
        Utils.logRawToConsole("");
        Utils.logToConsole("encryption of \"" + args[1] + "\" is \"" +
            encryptor.encrypt(args[1]) + "\"");
        Utils.logRawToConsole("");
        Utils.logRawToConsole("========================================================================");
        System.exit(0);
      }
    } else if (args.length > 6) {
      Utils.logError("Incorrect number of arguments passed. quitting...");
      Utils.logRawToConsole("Number of arguments = " + args.length);
      for (String arg : args) {
        Utils.logRawToConsole(arg);
      }
      System.exit(1);
    }
  }

  public void load() {
    printProperties();

    Settings.settings().logDiagnosticMessage();
    LoginManager.loginManager().logDiagnosticMessage();
    MainWindowManager.mainWindowManager().logDiagnosticMessage();
    TradingModeManager.tradingModeManager().logDiagnosticMessage();
    ConfigDialogManager.configDialogManager().logDiagnosticMessage();

    boolean isGateway = MainWindowManager.mainWindowManager().isGateway();

    startIBControllerServer(isGateway);

    startShutdownTimerIfRequired(isGateway);

    createToolkitListener();

    startSavingTwsSettingsAutomatically();

    startTwsOrGateway(isGateway);
  }

  private void createToolkitListener() {
    Toolkit.getDefaultToolkit().addAWTEventListener(new TwsListener(createWindowHandlers()), AWTEvent.WINDOW_EVENT_MASK);
  }

  private List<WindowHandler> createWindowHandlers() {
    List<WindowHandler> windowHandlers = new ArrayList<WindowHandler>();

    windowHandlers.add(new AcceptIncomingConnectionDialogHandler());
    windowHandlers.add(new BlindTradingWarningDialogHandler());
    windowHandlers.add(new ExitSessionFrameHandler());
    windowHandlers.add(new LoginFrameHandler());
    windowHandlers.add(new GatewayLoginFrameHandler());
    windowHandlers.add(new MainWindowFrameHandler());
    windowHandlers.add(new GatewayMainWindowFrameHandler());
    windowHandlers.add(new NewerVersionDialogHandler());
    windowHandlers.add(new NewerVersionFrameHandler());
    windowHandlers.add(new NotCurrentlyAvailableDialogHandler());
    windowHandlers.add(new TipOfTheDayDialogHandler());
    windowHandlers.add(new NSEComplianceFrameHandler());
    windowHandlers.add(new PasswordExpiryWarningFrameHandler());
    windowHandlers.add(new GlobalConfigurationDialogHandler());
    windowHandlers.add(new TradesFrameHandler());
    windowHandlers.add(new ExistingSessionDetectedDialogHandler());
    windowHandlers.add(new ApiChangeConfirmationDialogHandler());
    windowHandlers.add(new SplashFrameHandler());
    windowHandlers.add(new SecurityCodeDialogHandler());
    windowHandlers.add(new ReloginDialogHandler());
    windowHandlers.add(new NonBrokerageAccountDialogHandler());
    windowHandlers.add(new ExitConfirmationDialogHandler());

    return windowHandlers;
  }

  private Date getShutdownTime() {
    String shutdownTimeSetting = Settings.settings().getString("ClosedownAt", "");
    if (shutdownTimeSetting.length() == 0) {
      return null;
    } else {
      int shutdownDayOfWeek;
      int shutdownHour;
      int shutdownMinute;
      Calendar cal = Calendar.getInstance();
      try {
        cal.setTime((new SimpleDateFormat("E HH:mm")).parse(shutdownTimeSetting));
        shutdownDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        shutdownHour = cal.get(Calendar.HOUR_OF_DAY);
        shutdownMinute = cal.get(Calendar.MINUTE);
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, shutdownHour);
        cal.set(Calendar.MINUTE, shutdownMinute);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH,
            (shutdownDayOfWeek + 7 -
                cal.get(Calendar.DAY_OF_WEEK)) % 7);
        if (!cal.getTime().after(new Date())) {
          cal.add(Calendar.DAY_OF_MONTH, 7);
        }
      } catch (ParseException e) {
        Utils.logError("Invalid ClosedownAt setting: should be: <day hh:mm>   eg Friday 22:00");
        System.exit(1);
      }
      return cal.getTime();
    }
  }

  private String getJtsIniFilePath() {
    return getTWSSettingsDirectory() + File.separatorChar + "jts.ini";
  }

  private String getTWSSettingsDirectory() {
    String path = Settings.settings().getString("IbDir", System.getProperty("user.dir"));
    try {
      Files.createDirectories(Paths.get(path));
    } catch (FileAlreadyExistsException ex) {
      Utils.logError("Failed to create TWS settings directory at: " + path + "; a file of that name already exists");
      System.exit(1);
    } catch (IOException ex) {
      Utils.logException(ex);
      System.exit(1);
    }
    return path;
  }

  private void printProperties() {
    Properties p = System.getProperties();
    Enumeration<Object> i = p.keys();
    Utils.logRawToConsole("System Properties");
    Utils.logRawToConsole("------------------------------------------------------------");
    while (i.hasMoreElements()) {
      String props = (String) i.nextElement();
      Utils.logRawToConsole(props + " = " + (String) p.get(props));
    }
    Utils.logRawToConsole("------------------------------------------------------------");
  }

  private void startGateway() {
    String[] twsArgs = new String[1];
    twsArgs[0] = getTWSSettingsDirectory();
    try {
      ibgateway.GWClient.main(twsArgs);
    } catch (Throwable t) {
      Utils.logError("Can't find the Gateway entry point: ibgateway.GWClient.main. Gateway is not correctly installed.");
      t.printStackTrace(Utils.getErrStream());
      System.exit(1);
    }
  }

  private void startIBControllerServer(boolean isGateway) {
    cachedThreadPool.execute(new IBControllerServer(isGateway));
  }

  private void startShutdownTimerIfRequired(boolean isGateway) {
    Date shutdownTime = getShutdownTime();
    if (!(shutdownTime == null)) {
      long delay = shutdownTime.getTime() - System.currentTimeMillis();
      Utils.logToConsole((isGateway ? "Gateway" : "TWS") + " will be shut down at " + (new SimpleDateFormat("yyyy/MM/dd HH:mm")).format(shutdownTime));
      scheduledExecutorService.schedule(new Runnable() {
        @Override
        public void run() {
          cachedThreadPool.execute(new StopTask(null));
        }
      }, delay, TimeUnit.MILLISECONDS);
    }
  }

  private void startTws() {
    if (Settings.settings().getBoolean("ShowAllTrades", false)) {
      Utils.showTradesLogWindow();
    }
    String[] twsArgs = new String[1];
    twsArgs[0] = getTWSSettingsDirectory();
    try {
      jclient.LoginFrame.main(twsArgs);
    } catch (Throwable t) {
      Utils.logError("Can't find the TWS entry point: jclient.LoginFrame.main; TWS is not correctly installed.");
      t.printStackTrace(Utils.getErrStream());
      System.exit(1);
    }
  }

  private void startTwsOrGateway(boolean isGateway) {
    Utils.logToConsole("TWS Settings directory is: " + getTWSSettingsDirectory());
    JtsIniManager.initialise(getJtsIniFilePath());
    JtsIniManager.ensureValidJtsIniFile();
    if (isGateway) {
      startGateway();
    } else {
      startTws();
    }

    int portNumber = Settings.settings().getInt("ForceTwsApiPort", 0);
    if (portNumber != 0) (new ConfigurationTask(new ConfigureTwsApiPortTask(portNumber), cachedThreadPool)).executeAsync();
    Utils.sendConsoleOutputToTwsLog(!Settings.settings().getBoolean("LogToConsole", false));
  }

  private void startSavingTwsSettingsAutomatically() {
    TwsSettingsSaver.getInstance().initialise();
  }

}
