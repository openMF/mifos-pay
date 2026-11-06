/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/**
 * Android-specific implementation for sharing invite messages via SMS and WhatsApp
 */
class AndroidSharingHelper(private val context: android.content.Context) : SharingHelper {

    /**
     * Shares the invite message via the selected sharing option
     *
     * @param sharingOption The selected sharing method (SMS or WhatsApp)
     * @param phoneNumber The target phone number
     * @param message The invite message to share
     */
    override fun shareInviteMessage(
        sharingOption: SharingOption,
        phoneNumber: String,
        message: String,
    ) {
        when (sharingOption) {
            SharingOption.SMS -> shareViaSms(phoneNumber, message)
            SharingOption.WHATSAPP -> shareViaWhatsApp(phoneNumber, message)
        }
    }

    /**
     * Shares the invite message via SMS using Android's SMS intent
     */
    private fun shareViaSms(phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "smsto:$phoneNumber".toUri()
                putExtra("sms_body", message)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra("address", phoneNumber)
                }
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(Intent.createChooser(fallbackIntent, "Send SMS"))
            }
        } catch (e: Exception) {
            println("Error sharing via SMS: ${e.message}")
        }
    }

    /**
     * Shares the invite message via WhatsApp using WhatsApp's intent
     */
    private fun shareViaWhatsApp(phoneNumber: String, message: String) {
        try {
            val cleanPhoneNumber = phoneNumber.replace("+", "").replace(" ", "")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data =
                    "https://api.whatsapp.com/send?phone=$cleanPhoneNumber&text=${Uri.encode(message)}".toUri()
            }

            // Check if WhatsApp is installed
            if (intent.resolveActivity(context.packageManager) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                // Fallback to generic sharing if WhatsApp is not installed
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra("address", phoneNumber)
                }
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(Intent.createChooser(fallbackIntent, "Share via"))
            }
        } catch (e: Exception) {
            // Handle any exceptions (e.g., WhatsApp not installed)
            println("Error sharing via WhatsApp: ${e.message}")
        }
    }
}

/**
 * Android-specific implementation of the factory function
 */
actual fun getSharingHelper(): SharingHelper {
    val context = ContextProvider.getContext()
    return if (context != null) {
        AndroidSharingHelper(context)
    } else {
        DefaultSharingHelper
    }
}
