package com.gf.mail.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gf.mail.R
import com.gf.mail.domain.model.Email
import com.gf.mail.presentation.ui.email.EmailListScreen
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing email notifications
 */
@Singleton
class EmailNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val CHANNEL_ID = "email_notifications"
        private const val CHANNEL_NAME = "Email Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for new emails and email updates"
        private const val NOTIFICATION_ID_BASE = 1000
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show notification for new email
     */
    fun showNewEmailNotification(email: Email, accountEmail: String) {
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("account_id", email.accountId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            email.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New email from ${email.fromName}")
            .setContentText(email.subject)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(email.bodyText ?: email.bodyHtml ?: "")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EMAIL)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        
        notificationManager.notify(
            NOTIFICATION_ID_BASE + email.hashCode(),
            notification
        )
    }
    
    /**
     * Show notification for multiple new emails
     */
    fun showMultipleEmailsNotification(emails: List<Email>, accountEmail: String) {
        if (emails.isEmpty()) return
        
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("account_id", emails.first().accountId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            emails.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("${emails.size} new emails")
            .setSummaryText("in $accountEmail")
        
        // Add up to 5 email previews
        emails.take(5).forEach { email ->
            inboxStyle.addLine("${email.fromName}: ${email.subject}")
        }
        
        if (emails.size > 5) {
            inboxStyle.addLine("... and ${emails.size - 5} more")
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${emails.size} new emails")
            .setContentText("in $accountEmail")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EMAIL)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setNumber(emails.size)
            .build()
        
        notificationManager.notify(
            NOTIFICATION_ID_BASE + emails.hashCode(),
            notification
        )
    }
    
    /**
     * Show notification for sync status
     */
    fun showSyncStatusNotification(accountEmail: String, isSuccess: Boolean, message: String? = null) {
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            accountEmail.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (isSuccess) "Sync completed" else "Sync failed"
        val text = message ?: if (isSuccess) "Emails synced for $accountEmail" else "Failed to sync emails for $accountEmail"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (isSuccess) R.drawable.ic_sync else R.drawable.ic_warning)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(if (isSuccess) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        
        notificationManager.notify(
            NOTIFICATION_ID_BASE + accountEmail.hashCode() + 10000,
            notification
        )
    }
    
    /**
     * Cancel notification by ID
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
}