package com.antorparvez.utilitybox

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt

object UtilityBox {

    const val TAG = "AntorParvez Utils"

    @SuppressLint("LongLogTag")
    const val DATE_TIME_FORMAT_DD_MMM_YYYY_HH_MM_A = "d MMM yyyy h:mm a"
    const val DATE_TIME_FORMAT_FULL = "d/MM/yyyy\nh:mm a"
    const val DEBOUNCE_DURATION = 500L
    const val DATE_TIME_FORMAT_DD_MMMM_YYYY = "d MMM yyyy"
    const val DATE_TIME_FORMAT_DD_MM_YYYY = DATE_TIME_FORMAT_DD_MMMM_YYYY
    const val TIME_FORMAT_H_MM_A = "h:mm a"

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun Context.readRaw(@RawRes resourceId: Int): String {
        return resources.openRawResource(resourceId).bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
    }

    fun isPortrait(isPortraitEnable: Int?): Boolean {
        return !(isPortraitEnable == null || isPortraitEnable == 0)
    }

    fun calculateNoOfColumns(context: Context, columnWidthDp: Float = 100f): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return screenWidthDp.div(columnWidthDp).toInt()
    }


    fun getSearchKey(searchKey: String?): String {
        searchKey?.let {
            val trimmedSearchKey = searchKey.trim()
            if (trimmedSearchKey.isNotEmpty()) return trimmedSearchKey
        }
        return String()
    }


    fun timeBasedContent(context: Context, time: Long): String {
        val difference = (System.currentTimeMillis() - time) / 1000
        return when {
            difference < 60 -> {
                context.getString(R.string.time_based_content_now)
            }
            difference < 60 * 2 -> {
                context.getString(R.string.time_based_content_min)
            }
            difference < 60 * 60 -> {
                context.getString(
                    R.string.time_based_content_mins,
                    (difference / 60.0).roundToInt().formatCurrency()
                )
            }
            difference < 60 * 60 * 2 -> {
                context.getString(R.string.time_based_content_hour)
            }
            difference < 60 * 60 * 24 -> {
                context.getString(
                    R.string.time_based_content_hours,
                    (difference / (60.0 * 60.0)).roundToInt().formatCurrency()
                )
            }
            difference < 60 * 60 * 48 -> {
                context.getString(R.string.time_based_content_day)
            }
            else -> {
                context.getString(
                    R.string.time_based_content_days,
                    (difference / (60.0 * 60.0 * 24.0)).roundToInt().formatCurrency()
                )
            }
        }
    }


    fun isEmailIdValid(email: String?): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat: Pattern = Pattern.compile(emailRegex)
        return if (email == null) false else pat.matcher(email).matches()
    }

    fun getButtonColor(context: Context, isDisable: Boolean, color1: Int, color2: Int): Int {
        return when (isDisable) {
            true -> ContextCompat.getColor(context, color1)
            false -> ContextCompat.getColor(context, color2)
        }
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun setBundleTextDisable(
        context: Context,
        viewList: List<MaterialTextView>,
        isDisable: Boolean
    ) {
        viewList.forEach { singleView ->
            singleView.setTextColor(
                getButtonColor(
                    context, isDisable, R.color.cardview_shadow_end_color,
                    R.color.cardview_shadow_start_color
                )
            )
        }
    }

    fun navigateToGooglePlay(context: Context, appPackageName: String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
            .setPackage("com.android.vending")
        try {
            context.startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            Log.e(TAG, "navigateToGooglePlay: $exception")
        }
    }

    fun share(context: Context?, url: String, buildType: String) {
        context?.let {
            val url = when (buildType) {
                "release", "production" -> {
                    url
                }
                else -> url
            }
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        }
    }

    fun ImageView.loadImage(
        context: Activity?,
        url: String?,
        placeHolderUrl: Int,
        errorImageUrl: Int
    ) {
        if (context == null || context.isDestroyed) return

        //placeHolderUrl=R.drawable.ic_user;
        //errorImageUrl=R.drawable.ic_error;
        Glide.with(context) //passing context
            .load(url) //passing your url to load image.
            .transition(GenericTransitionOptions.with(R.anim.fade_in))
            .placeholder(placeHolderUrl) //this would be your default image (like default profile or logo etc). it would be loaded at initial time and it will replace with your loaded image once glide successfully load image using url.
            .error(errorImageUrl) //in case of any glide exception or not able to download then this image will be appear . if you won't mention this error() then nothing to worry placeHolder image would be remain as it is.
            .diskCacheStrategy(DiskCacheStrategy.ALL) //using to load into cache then second time it will load fast.
            .fitCenter() //this method help to fit image into center of your ImageView
            .into(this) //pass imageView reference to appear the image.
    }


    private fun DateTime.formatToPattern(pattern: String): String? {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time = this.toDate()
            val dateTimeFormatter = DateTimeFormat.forPattern(pattern)
            this.toString(dateTimeFormatter).replace("am", "AM").replace("pm", "PM")
        } catch (e: IllegalArgumentException) {
            return null
        }
    }

    fun Int.dpToPx(context: Context?): Int {
        val resources = context?.resources
        val metrics = resources?.displayMetrics
        val px = this * ((metrics?.densityDpi ?: 0) / 160f)
        return px.toInt()
    }

    fun View.showIf(should: Boolean) {
        this.visibility = if (should) View.VISIBLE else View.GONE
    }

    fun View.visibleIf(should: Boolean) {
        this.visibility = if (should) View.VISIBLE else View.INVISIBLE
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    fun View.gone() {
        this.visibility = View.GONE
    }

    fun View.hide() {
        this.visibility = View.INVISIBLE
    }

    fun TextView.clear() {
        this.text = ""
    }

    fun View.enable() {
        this.isEnabled = true
    }

    fun View.disable() {
        this.isEnabled = false
    }

    fun View.requestFocusIfVisible() {
        if (this.isVisible) this.requestFocus()
    }


    fun TextInputEditText.disable(boolean: Boolean) {
        this.isFocusable = boolean
        this.isClickable = boolean
        this.isFocusableInTouchMode = boolean
        this.isCursorVisible = boolean
        if (this.error != null) this.error = null
    }

    fun MaterialButton.disable(context: Context?, boolean: Boolean, color1: Int, color2: Int) {
        this.isEnabled = !boolean
        this.setTextColor(
            if (boolean) {
                ContextCompat.getColor(context!!, color1)
            } else {
                ContextCompat.getColor(context!!, color2)
            }
        )
        this.setStrokeColorResource(
            if (boolean) {
                color1
            } else {
                color2
            }
        )
    }


    inline fun <T> MutableList<T>.mapInPlace(mutator: (T) -> T) {
        val iterate = this.listIterator()
        while (iterate.hasNext()) {
            val oldValue = iterate.next()
            val newValue = mutator(oldValue)
            if (newValue !== oldValue) {
                iterate.set(newValue)
            }
        }
    }

    fun Fragment.hideKeyboard() {
        activity?.hideKeyboard(this.requireView())
    }

    fun Fragment.showKeyboard() {
        activity?.showKeyboard(this.requireView())
    }

    fun Fragment.showKeyboard(view: View) {
        activity?.showKeyboard(view)
    }

    fun Activity.hideKeyboard() {
        if (currentFocus == null) View(this) else currentFocus?.let { hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun Context.showKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun Double.formatCurrency(): String {
        val decimalFormat = DecimalFormat("##,##,###.00")
        return decimalFormat.format(this)
    }

    fun Double.format(): Double {
        return this.times(1.00)
    }

    fun Double.formatDecimal(): String {
        val decimalFormat = DecimalFormat("##,##,###.00")
        return decimalFormat.format(this)
    }

    fun Int.formatCurrency(): String {
        val decimalFormat = DecimalFormat("##,##,###")
        return decimalFormat.format(this)
    }

    fun View.setOnClickListenerWithDebounce(onClick: (view: View) -> Unit) {
        this.setOnClickListener(OnClickListenerDebounceDecorator { view ->
            view?.apply {
                onClick.invoke(this)
            }
        })
    }

    inline fun <reified T> Any?.cast(): T? {
        return if (this is T) this else null
    }

    class OnClickListenerDebounceDecorator(private val wrapper: View.OnClickListener) :
        View.OnClickListener {

        private var lastClickTime: Long = 0

        override fun onClick(view: View?) {
            if (SystemClock.elapsedRealtime() - lastClickTime > 500L) {
                view?.apply { wrapper.onClick(this) }
            }
            lastClickTime = SystemClock.elapsedRealtime()
        }
    }

    fun showCommonDialog(msg: String?, context: Context?) {
        if (context != null) {
            AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show()
        }
    }

    fun Fragment.showCommonDialog(msg: String?) {
        AlertDialog.Builder(this.requireContext())
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), null)
            .setCancelable(false)
            .show()
    }

    @JvmName("navigateToGooglePlay1")
    fun Context.navigateToGooglePlay(appPackageName: String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
            .setPackage("com.android.vending")
        try {
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            Log.e(TAG, "navigateToGooglePlay: $exception")
        }
    }

    fun Context.getVersionName(): String {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionName
    }

    fun CharSequence?.isNotNullOrEmpty(): Boolean {
        return !this.isNullOrEmpty()
    }

    fun <T> List<T>.isNotNullOrEmpty(): Boolean {
        return this.isNullOrEmpty().not()
    }

    fun emptyString(): String {
        return ""
    }

    fun String.removeAllSpecialChar(): String {
        this.let {
            return Regex("[^a-zA-Z0-9 ]")  // this will accept only space, characters and digits
                .replace(this, String())
        }
    }

    fun roundOffDecimal(number: Double?): Double? {
        if (number == null) return 0.0
        val df = DecimalFormat("#.##")
        val n = Math.ceil(number * 20) / 20
        val result = number.round(2)
        return result
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return Math.round(this * multiplier) / multiplier
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun Context.makeVibrate(milliSeconds: Long = 25L) {
        val v: Vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val singleEffect =
                VibrationEffect.createOneShot(milliSeconds, VibrationEffect.EFFECT_DOUBLE_CLICK)

            v.vibrate(singleEffect)
        } else {
            v.vibrate(milliSeconds)
        }
    }

    fun View.shakeAnimate() {
        val objetctAnimator = ObjectAnimator.ofFloat(
            this,
            "rotation",
            0f,
            10f,
            0f,
            -10f,
            0f
        ) // rotate o degree then 20 degree and so on for one loop of rotation.

        objetctAnimator.setAutoCancel(true)
        objetctAnimator.duration = 100L
        objetctAnimator.repeatCount = 1

        objetctAnimator.start()
    }

    fun Int.digitIntoBn(context: Context?) = context?.getString(R.string.item_quantity, this)

    fun String.stringIntoBn(context: Context?) =
        context?.getString(R.string.item_quantity_string, this)

    fun String.convertBanglaForDate(context: Context?): String {
        val bnStringList = this.split("/").map { it.toInt().digitIntoBn(context) }
        return bnStringList.joinToString(separator = "/")
    }


    fun Context.showToast(msg: String) {
        val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 72)
        toast.show()
    }


    fun String.timeFormat(): String {
        val DATE_TIME_FORMAT = "HH:mm:ss"

        val formatter = DateTimeFormat.forPattern(DATE_TIME_FORMAT)
        return formatter.parseDateTime(this).formatToPattern(TIME_FORMAT_H_MM_A)?.toUpperCase()
            ?: this
    }

    fun String.trackingTimeFormat(): String {
        val formatter = DateTimeFormat.forPattern("HH:mm:ss")
        return formatter.parseDateTime(this).formatToPattern(TIME_FORMAT_H_MM_A)?.toUpperCase()
            ?: this
    }

    fun String.capitalize(): String {
        this.trim()
        return if (isNotEmpty() && this[0].isLowerCase()) substring(
            0,
            1
        ).toUpperCase() + substring(1) else this
    }

    fun Context.sendSMSPoneNumber(phoneNo: String?, msg: String?) {
        if (phoneNo.isNullOrEmpty()) {
            showToast(getString(R.string.error_invalid_phone_number))
            return
        }

        val SENT = "SMS_SENT"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNo")
//        type = "text/plain"
            putExtra("sms_body", msg)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    fun Context.callToPhoneNumber(phoneNumber: String) {
        startActivity(
            Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:${phoneNumber}")
            )
        )
    }

    fun getSmsIntent(context: Context?, urlWithSmsContent: String): Boolean {
        val content = urlWithSmsContent.split("&")
        val uri: Uri = Uri.parse(content[0])
        val decodedText = URLDecoder.decode(content[1], "UTF-8")
        val smsIntent = Intent(Intent.ACTION_SENDTO, uri)
        smsIntent.putExtra("sms_body", decodedText)
        context?.let {
            it.startActivity(smsIntent)
            return true
        } ?: kotlin.run { return false }
    }

    fun getCallingIntent(context: Context?, numberUrl: String?): Boolean {
        numberUrl?.let { number ->
            val phoneNumber = if (number.contains("tel:")) number else "tel:$number"
            context?.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber)))
            return true
        } ?: kotlin.run { return false }
    }

    fun setClipboard(
        context: Context,
        text: String
    ) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    fun Fragment.sendToWhatsApp(msg: String, contactNumber: String?) {
        try {
            val packageManager = requireContext().packageManager
            val i = Intent(Intent.ACTION_VIEW)
            val url =
                "https://api.whatsapp.com/send?phone=$contactNumber&text=" + URLEncoder.encode(
                    msg.toString(),
                    "UTF-8"
                )
            i.data = Uri.parse(url)

            if (i.resolveActivity(packageManager) != null) {
                i.setPackage("com.whatsapp")
                startActivityForResult(Intent.createChooser(i, "Share with"), 888)
            } else {
                requireContext().showToast("WhatsApp not Installed")
                val b = Bundle()
                b.putBoolean("new_window", true) //sets new window
//                i.putExtras(b)
                startActivity(i)
            }

        } catch (e: PackageManager.NameNotFoundException) {
            requireContext().showToast("WhatsApp not Installed")
        }

    }
}


