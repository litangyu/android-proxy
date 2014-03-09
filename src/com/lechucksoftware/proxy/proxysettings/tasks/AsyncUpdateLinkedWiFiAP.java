package com.lechucksoftware.proxy.proxysettings.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.lechucksoftware.proxy.proxysettings.ApplicationGlobals;
import com.lechucksoftware.proxy.proxysettings.constants.Intents;
import com.lechucksoftware.proxy.proxysettings.db.ProxyEntity;
import com.lechucksoftware.proxy.proxysettings.ui.activities.WiFiApListActivity;
import com.lechucksoftware.proxy.proxysettings.utils.EventReportingUtils;
import com.lechucksoftware.proxy.proxysettings.utils.WhatsNewDialog;
import com.shouldit.proxy.lib.APL;
import com.shouldit.proxy.lib.APLIntents;
import com.shouldit.proxy.lib.ProxyConfiguration;
import com.shouldit.proxy.lib.log.LogWrapper;
import com.shouldit.proxy.lib.reflection.android.ProxySetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.Duration;

/**
 * Created by Marco on 29/11/13.
 */


public class AsyncUpdateLinkedWiFiAP extends AsyncTask<Void, UUID, Integer>
{
    private final Activity callerActivity;
    private final ProxyEntity currentProxy;
    private final ProxyEntity updatedProxy;
    private static final String TAG = AsyncUpdateLinkedWiFiAP.class.getSimpleName();

    public AsyncUpdateLinkedWiFiAP(Activity caller, ProxyEntity current, ProxyEntity updated)
    {
        currentProxy = current;
        updatedProxy = updated;
        callerActivity = caller;
    }

    @Override
    protected void onPostExecute(Integer updatedWiFiAP)
    {
        super.onPostExecute(updatedWiFiAP);

        final int updatedWifi = updatedWiFiAP;

        if (updatedWifi > 0)
        {
            callerActivity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(callerActivity, String.format("Updated %d Wi-Fi access point configuration", updatedWifi), Toast.LENGTH_SHORT);
                }
            });
        }
    }

    @Override
    protected Integer doInBackground(Void... voids)
    {
        int updatedWiFiAP = 0;

        List<ProxyConfiguration> configurations = ApplicationGlobals.getProxyManager().getSortedConfigurationsList();

        LogWrapper.d(TAG, "Current proxy: " + currentProxy.toString());
        LogWrapper.d(TAG, "Updated proxy: " + updatedProxy.toString());

        ApplicationGlobals.getInstance().wifiActionEnabled = false;

        for (ProxyConfiguration conf : configurations)
        {
            if (conf.getProxySettings() == ProxySetting.STATIC)
            {
                LogWrapper.d(TAG, "Checking AP: " + conf.toShortString());

                if (conf.getProxyHost().equalsIgnoreCase(currentProxy.host)
                    && conf.getProxyPort().equals(currentProxy.port)
                    && conf.getProxyExclusionList().equalsIgnoreCase(currentProxy.exclusion))
                {
                    conf.setProxyHost(updatedProxy.host);
                    conf.setProxyPort(updatedProxy.port);
                    conf.setProxyExclusionList(updatedProxy.exclusion);

                    LogWrapper.d(TAG, "Writing updated AP configuration on device: " + conf.toShortString());

                    try
                    {
                        conf.writeConfigurationToDevice();
                    }
                    catch (Exception e)
                    {
                        EventReportingUtils.sendException(e);
                    }

                    updatedWiFiAP++;

                    try
                    {
                        Thread.sleep(1);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    // Calling refresh intent only after save of all AP configurations
                    LogWrapper.i(TAG, "Sending broadcast intent: " + Intents.WIFI_AP_UPDATED);
                    Intent intent = new Intent(Intents.WIFI_AP_UPDATED);
                    APL.getContext().sendBroadcast(intent);
                }
            }
        }

        ApplicationGlobals.getInstance().wifiActionEnabled = true;

        LogWrapper.d(TAG, "Current proxy: " + currentProxy.toString());
        LogWrapper.d(TAG, "Updated proxy: " + updatedProxy.toString());

//        ApplicationGlobals.getDBManager().upsertProxy(updatedProxy);

        return updatedWiFiAP;
    }
}
