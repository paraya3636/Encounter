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
import android.support.v4.app.NotificationCompat
import android.text.TextUtils

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener


/**
 * While subscribed in the background, this service shows a persistent notification with the
 * current set of messages from nearby beacons. Nearby launches this service when a message is
 * found or lost, and this service updates the notification, then stops itself.
 */
class BackgroundSubscribeIntentService : IntentService("BackgroundSubscribeIntentService") {

    override fun onCreate() {
        super.onCreate()
        updateNotification()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            Nearby.Messages.handleIntent(intent, object : MessageListener() {
                override fun onFound(message: Message) {
                    Utils.saveFoundMessage(applicationContext, message)
                    updateNotification()
                }

                override fun onLost(message: Message) {
                    Utils.removeLostMessage(applicationContext, message)
                    updateNotification()
                }
            })
        }
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
}