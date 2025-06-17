package org.piramalswasthya.sakhi.ui.home_activity


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.piramalswasthya.sakhi.R


class FBMessaging : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("#####", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var mTitle = ""
        var mBody = ""
        var mUniqueNotificationCode = 0
        var payloadCode = 0
        var mType = ""
        var notificationID = 0
        var senderId = 0
        var referenceId = 0

        try
        {

            remoteMessage.data.let {
                Log.d("#####", "Splash " + remoteMessage.data)
                Log.d("#####", "Message data payload: " + remoteMessage.senderId)
                Log.d("#####", "From: ${remoteMessage.from}")
                Log.d("#####", "Title: ${remoteMessage.notification?.title}")
                Log.d("#####", "Body: ${remoteMessage.notification?.body}")
                Log.d("#####", "NotificationTypeId: ${it["NotificationTypeId"]}")

                Log.d("payloadResponseData","GetData"+it.toString())

                mType = "${it["NotificationTypeId"]}" // notificationTypeId



//                mUniqueNotificationCode = "${it["NotificationTypeId"]}".toInt()   // notificationTypeId
//                notificationID = "${it["NotificationId"]}".toInt() //  userId
//                senderId = "${it["SenderId"]}".toInt() // senderId
//                referenceId = "${it["AdditionalData"]}".toInt()

//                var a = "${it}"
//                Log.d("arty",""+a)

                mBody = "${remoteMessage.notification!!.body}" //
                mTitle = "${remoteMessage.notification!!.title}" // senderTitle

//               var mSenderProfilePic = "${it["SenderProfilePic"]}"


               /* if ("${it["AdditionalData"]}".toString().equals("") || "${it["AdditionalData"]}"==null)
                {
                    payloadCode = 0
                }
                else
                {
                    payloadCode = "${it["AdditionalData"]}".toInt()
                }*/


            }



//            if(mUniqueNotificationCode==107){
//
//                Log.e("AAAAAMessage","HIt")
//                messageUpdate!!.ApiUpdate()
//            }
            mShowNotification(mTitle, mBody, mUniqueNotificationCode, mType,payloadCode,referenceId,notificationID,senderId)

        }
        catch (e:Exception)
        {
            Log.d("msg", "onMessageReceivedDDDD: " + e.message.toString())
            mShowNotification(mTitle, mBody, mUniqueNotificationCode, mType,payloadCode,referenceId,notificationID,senderId)


        }



//        var mTitle = remoteMessage.notification?.title
//        var mBody = remoteMessage.notification?.body
//        var type = ""
//        var bookingId = ""
//        var channelId = ""
//        var coachId = ""
//        var coachName = ""
//        var uniqueCode = ""
//        var statusName= ""
//        var statusId = ""

//        remoteMessage.data.let {
//            Log.d("#####", "Message data payload: " + remoteMessage.data)
//        }
//
//            val params = remoteMessage.data as Map<String, String>
//            val json = JSONObject(params)
//            var dataString = json.getString("Data")
//
//            val dataJson = JSONObject(dataString)

//            type = json.getString("Type")
//            Log.e("JSON OBJECT", json.toString())
//            bookingId = dataJson.getString("bookingId")
//            channelId = dataJson.getString("ChannelId")
//            coachId = dataJson.getString("ConnectToUserId")
//            coachName = dataJson.getString("ConnectToUserName")
//            uniqueCode = dataJson.getString("UniqueCode")
//            statusName = dataJson.getString("StatusName")
//            statusId = dataJson.getString("StatusId")

//            var iDdata = "${it["Data"]}"
//            var dataiId = iDdata.split(",").toTypedArray()
//            bookingId = dataiId[0]
//        }

//        remoteMessage.notification?.let {
//            Log.d("#####", "Message Notification Body: ${it.body}")
//            mBody = "${it.body}"
//            mTitle = "${it.title}"
//        }
//        mShowNotification(mTitle!!, mBody!!)
    }

    companion object{
        var messageUpdate: MessageUpdate ?= null
    }

//    private fun mShowNotification(
//        mTitle: String,
//        mBody: String
//    ) {
//        var intent = Intent()
//
//            intent = Intent(this, HomeActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT
//        )
//        val channelId = "circular_app_021"
//        val channelName = "CIRCULAR_APP"
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
//            setContentTitle(mTitle)
//            setContentText(mBody)
//            setAutoCancel(true)
//            setContentIntent(pendingIntent)
//            setSmallIcon(R.mipmap.ic_circular_launcher)
//            val bigTextStyle = NotificationCompat.BigTextStyle()
//            bigTextStyle.setBigContentTitle(mTitle)
//            bigTextStyle.bigText(mBody)
//            setStyle(bigTextStyle)
//        }
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Since android Oreo notification channel is needed.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                channelName,
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//
//        }
//        notificationManager.notify(0, notificationBuilder.build())
//    }

    private fun mShowNotification(
        mTitle: String,
        mBody: String,
        mUniqueNotificationCode: Int,
        mType: String,
        payloadCode: Int,
        referenceId: Int,
        notificationID: Int,
        senderId:Int
    ) {

        val intent = Intent(applicationContext, HomeActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        intent.putExtra("uniqueNotificationCode", mUniqueNotificationCode)
        intent.putExtra("notificationId",notificationID)
        intent.putExtra("senderId",senderId)
        intent.putExtra("feedId",referenceId)
        intent.putExtra("mTitle",mTitle)
        intent.putExtra("FBM",true)


//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )


        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )


        val channelId = "my_channel_id_180"
        val channelName = "App"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle(mTitle)
            setContentText(mBody)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            setSmallIcon(R.drawable.ic_logo_icon)


//            val bigTextStyle = NotificationCompat.BigTextStyle()
//            bigTextStyle.setBigContentTitle(mTitle)
//            bigTextStyle.bigText(mBody)
//            setStyle(bigTextStyle)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
/*

        if (mTitle == Constants.NOTIFICATION_TYPE_CREDIT|| mTitle == Constants.NOTIFICATION_TYPE_DEBIT) {
            sendBroadcast(Intent().apply { action = "${Constants.NOTIFICATION_ACTION}${mTitle}" })
        }
*/


        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())

    }

}


