/*
 * Copyright (C) 2012 Yuriy Kulikov yuriy.kulikov.87@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.premium.alarm;

import java.lang.reflect.Field;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.preference.PreferenceManager;
import android.view.ViewConfiguration;

import com.github.androidutils.logger.FileLogWriter;
import com.github.androidutils.logger.LogcatLogWriterWithLines;
import com.github.androidutils.logger.Logger;
import com.github.androidutils.logger.Logger.LogLevel;
import com.github.androidutils.logger.LoggingExceptionHandler;
import com.github.androidutils.logger.StartupLogWriter;
import com.github.androidutils.statemachine.StateMachine;
import com.github.androidutils.wakelock.WakeLockManager;
import com.premium.alarm.model.AlarmCore;
import com.premium.alarm.model.Alarms;
import com.premium.alarm.model.AlarmsManager;
import com.premium.alarm.model.AlarmsScheduler;
import com.premium.alarm.model.AlarmsService;
import com.premium.alarm.model.persistance.AlarmDatabaseHelper;
import com.premium.alarm.model.persistance.AlarmProvider;
import com.premium.alarm.presenter.AlarmsListFragment;
import com.premium.alarm.presenter.DynamicThemeHandler;
import com.premium.alarm.presenter.alert.AlarmAlertFullScreen;
import com.premium.alarm.presenter.background.KlaxonService;
import com.premium.alarm.presenter.background.VibrationService;

// @formatter:off
@ReportsCrashes(
        formKey = "",
        mailTo = "yuriy.kulikov.87@gmail.com",
        applicationLogFile = "applog.log",
        applicationLogFileLines = 150,
        customReportContent = {
                ReportField.IS_SILENT,
                ReportField.APP_VERSION_CODE,
                ReportField.PHONE_MODEL,
                ReportField.ANDROID_VERSION,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.APPLICATION_LOG,
                ReportField.SHARED_PREFERENCES,
                })
// @formatter:on
public class AlarmApplication extends Application {

    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        DynamicThemeHandler.init(this);
        setTheme(DynamicThemeHandler.getInstance().getIdForName(DynamicThemeHandler.DEFAULT));

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        Logger logger = Logger.getDefaultLogger();
        logger.addLogWriter(LogcatLogWriterWithLines.getInstance());
        logger.addLogWriter(FileLogWriter.getInstance(this, false));
        logger.addLogWriter(StartupLogWriter.getInstance());
        LoggingExceptionHandler.addLoggingExceptionHandlerToAllThreads(logger);

        logger.setLogLevel(WakeLockManager.class, LogLevel.ERR);
        logger.setLogLevel(AlarmDatabaseHelper.class, LogLevel.ERR);
        logger.setLogLevel(AlarmProvider.class, LogLevel.ERR);
        logger.setLogLevel(AlarmsScheduler.class, LogLevel.DBG);
        logger.setLogLevel(AlarmCore.class, LogLevel.DBG);
        logger.setLogLevel(Alarms.class, LogLevel.DBG);
        logger.setLogLevel(StateMachine.class, LogLevel.DBG);
        logger.setLogLevel(AlarmsService.class, LogLevel.DBG);
        logger.setLogLevel(KlaxonService.class, LogLevel.DBG);
        logger.setLogLevel(VibrationService.class, LogLevel.DBG);
        logger.setLogLevel(AlarmsListFragment.class, LogLevel.DBG);
        logger.setLogLevel(AlarmAlertFullScreen.class, LogLevel.DBG);

        WakeLockManager.init(getApplicationContext(), logger, true);
        AlarmsManager.init(getApplicationContext(), logger);

        // ACRA.getErrorReporter().addOnExceptionHandledCommand(new Runnable() {
        // @Override
        // public void run() {
        // ACRA.getErrorReporter().putCustomData("STARTUP_LOG",
        // StartupLogWriter.getInstance().getMessagesAsString());
        // }
        // });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        logger.d("onCreate");
        super.onCreate();
    }
}
