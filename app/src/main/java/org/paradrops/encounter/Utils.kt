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

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.android.gms.nearby.messages.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object Utils {
    internal val KEY_CACHED_MESSAGES = "cached-messages"

    /**
     * Fetches message strings stored in [SharedPreferences].

     * @param context The context.
     * *
     * @return  A list (possibly empty) containing message strings.
     */
    internal fun getCachedMessages(context: Context): List<String> {
        val sharedPrefs = getSharedPreferences(context)
        val cachedMessagesJson = sharedPrefs.getString(KEY_CACHED_MESSAGES, "")
        if (TextUtils.isEmpty(cachedMessagesJson)) {
            return emptyList()
        } else {
            val type = object : TypeToken<List<String>>() {

            }.type
            return Gson().fromJson<List<String>>(cachedMessagesJson, type)
        }
    }

    /**
     * Saves a message string to [SharedPreferences].

     * @param context The context.
     * *
     * @param message The Message whose payload (as string) is saved to SharedPreferences.
     */
    internal fun saveFoundMessage(context: Context, message: Message) {
        val cachedMessages = ArrayList(getCachedMessages(context))
        val cachedMessagesSet = (cachedMessages)
        val messageString = String(message.content)
        if (!cachedMessagesSet.contains(messageString)) {
            cachedMessages.add(0, String(message.content))
            getSharedPreferences(context)
                    .edit()
                    .putString(KEY_CACHED_MESSAGES, Gson().toJson(cachedMessages))
                    .apply()
        }
    }

    /**
     * Removes a message string from [SharedPreferences].
     * @param context The context.
     * *
     * @param message The Message whose payload (as string) is removed from SharedPreferences.
     */
    internal fun removeLostMessage(context: Context, message: Message) {
        val cachedMessages = ArrayList(getCachedMessages(context))
        cachedMessages.remove(String(message.content))
        getSharedPreferences(context)
                .edit()
                .putString(KEY_CACHED_MESSAGES, Gson().toJson(cachedMessages))
                .apply()
    }

    /**
     * Gets the SharedPReferences object that is used for persisting data in this application.

     * @param context The context.
     * *
     * @return The single [SharedPreferences] instance that can be used to retrieve and modify
     * *         values.
     */
    internal fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
                context.applicationContext.packageName,
                Context.MODE_PRIVATE)
    }
}