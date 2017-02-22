/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.paradrops.encounter

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import org.greenrobot.eventbus.EventBus
import org.paradrops.encounter.data.EncounterInfoDataStore
import org.paradrops.encounter.eventbus.FoundEvent
import org.paradrops.encounter.view.EncounterInfo
import java.util.*


/**
 * While subscribed in the background, this service shows a persistent notification with the
 * current set of messages from nearby beacons. Nearby launches this service when a message is
 * found or lost, and this service updates the notification, then stops itself.
 */
class BackgroundSubscribeIntentService : IntentService("BackgroundSubscribeIntentService"), GoogleApiClient.ConnectionCallbacks {
    private val TAG = BackgroundSubscribeIntentService::class.java.simpleName

    private lateinit var mGoogleApiClient: GoogleApiClient

    private val TTL_IN_SECONDS = 3 * 60 // Three minutes.
    private val PUB_SUB_STRATEGY = Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build()

    private lateinit var mMessageListener: MessageListener

    private var mPubMessage: Message? = null

    override fun onCreate() {
        super.onCreate()

        mMessageListener = object : MessageListener() {
            override fun onFound(message: Message?) {
            }

            override fun onLost(message: Message?) {
            }
        }

        updateNotification()
        buildGoogleApiClient()
        mGoogleApiClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mGoogleApiClient.disconnect()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            Nearby.Messages.handleIntent(intent, object : MessageListener() {
                override fun onFound(message: Message) {
                    Utils.saveFoundMessage(applicationContext, message)
                    updateNotification()

                    EncounterInfoDataStore().run {
                        val deviceMessage = DeviceMessage.fromNearbyMessage(message)
                        val info = EncounterInfo(deviceMessage.toString(), Date().toString())

                        save(info)
                        EventBus.getDefault().post(FoundEvent())
                    }
                }

                override fun onLost(message: Message) {
                    Utils.removeLostMessage(applicationContext, message)
                    updateNotification()
                }
            })
        }
    }

    override fun onConnected(p0: Bundle?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionSuspended(p0: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun updateNotification() {
        val messages = Utils.getCachedMessages(applicationContext)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val launchIntent = Intent(applicationContext, MainActivity::class.java)
        launchIntent.action = Intent.ACTION_MAIN
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pi = PendingIntent.getActivity(applicationContext, 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val contentTitle = getContentTitle(messages)
        val contentText = getContentText(messages)

        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setOngoing(true)
                .setContentIntent(pi)
        notificationManager.notify(MESSAGES_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getContentTitle(messages: List<String>): String {
        when (messages.size) {
            0 -> return resources.getString(R.string.scanning)
            1 -> return resources.getString(R.string.one_message)
            else -> return resources.getString(R.string.many_messages, messages.size)
        }
    }

    private fun getContentText(messages: List<String>): String {
        val newline = System.getProperty("line.separator")
        if (messages.size < NUM_MESSAGES_IN_NOTIFICATION) {
            return TextUtils.join(newline, messages)
        }
        return TextUtils.join(newline, messages.subList(0, NUM_MESSAGES_IN_NOTIFICATION)) +
                newline + "&#8230;"
    }

    companion object {
        private val MESSAGES_NOTIFICATION_ID = 1
        private val NUM_MESSAGES_IN_NOTIFICATION = 5
    }

    /**
     * Subscribes to messages from nearby devices and updates the UI if the subscription either
     * fails or TTLs.
     */
    private fun subscribe() {
        Log.i(TAG, "Subscribing")
        val options = SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(object : SubscribeCallback() {
                    override fun onExpired() {
                        super.onExpired()
                        Log.i(TAG, "No longer subscribing")
                    }
                }).build()

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(ResultCallback<Status> { status ->
                    if (status.isSuccess) {
                        Log.i(TAG, "Subscribed successfully.")
                    } else {
                        logAndShowSnackbar("Could not subscribe, status = " + status)
                    }
                })
    }

    /**
     * Publishes a message to nearby devices and updates the UI if the publication either fails or
     * TTLs.
     */
    private fun publish() {
        Log.i(TAG, "Publishing")
        val options = PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(object : PublishCallback() {
                    override fun onExpired() {
                        super.onExpired()
                        Log.i(TAG, "No longer publishing")
                    }
                }).build()

        Nearby.Messages.publish(mGoogleApiClient, mPubMessage, options)
                .setResultCallback { status ->
                    if (status.isSuccess) {
                        Log.i(TAG, "Published successfully.")
                    } else {
                        logAndShowSnackbar("Could not publish, status = " + status)
                    }
                }
    }

    /**
     * Stops subscribing to messages from nearby devices.
     */
    private fun unsubscribe() {
        Log.i(TAG, "Unsubscribing.")
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
    }

    /**
     * Stops publishing message to nearby devices.
     */
    private fun unpublish() {
        Log.i(TAG, "Unpublishing.")
        Nearby.Messages.unpublish(mGoogleApiClient, mPubMessage)
    }

    /**
     * Logs a message and shows a [Snackbar] using `text`;

     * @param text The text used in the Log message and the SnackBar.
     */
    private fun logAndShowSnackbar(text: String) {
        Log.w(TAG, text)
    }

    /**
     * Builds [GoogleApiClient], enabling automatic lifecycle management using
     * [GoogleApiClient.Builder.enableAutoManage]. I.e., GoogleApiClient connects in
     * [AppCompatActivity.onStart], or if onStart() has already happened, it connects
     * immediately, and disconnects automatically in [AppCompatActivity.onStop].
     */
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .build()
    }
}