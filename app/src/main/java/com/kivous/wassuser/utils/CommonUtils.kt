package com.kivous.wassuser.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kivous.wassuser.R
import kotlin.math.pow


object CommonUtils {

    @SuppressLint("StaticFieldLeak")
    val db = Firebase.firestore
    val auth = Firebase.auth

    /**
    Show password on visibility icon click
     */
    fun showPassword(isShow: Boolean, et: EditText, iv: ImageView) {
        if (isShow) {
            et.transformationMethod = HideReturnsTransformationMethod.getInstance()
            iv.setImageResource(R.drawable.visibility)
        } else {
            et.transformationMethod = PasswordTransformationMethod.getInstance()
            iv.setImageResource(R.drawable.visibility_off)
        }
        //to change cursor position
        et.setSelection(et.text.toString().length)
    }

    /**
    show cancel icon when edittext is not empty and clear edittext on cancel icon click
     */
    fun clearEdittext(et: EditText, iv: ImageView) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (et.text.toString().isEmpty()) {
                    iv.visibility = View.GONE
                } else {
                    iv.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                iv.setOnClickListener {
                    et.text.clear()
                }
            }
        })
    }

    /**
    Change icon color when edittext is not in focus
     */
    fun changeIconColorWhenEdittextNotInFocus(context: Context, et: EditText, iv: ImageView) {
        et.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                iv.setColorFilter(Color.GRAY)
            } else {
                iv.setColorFilter(
                    ContextCompat.getColor(
                        context, R.color.blue
                    )
                )
            }
        }
    }

    fun Fragment.hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


    /**
    Calculate distance between user latitude-longitude and set latitude-longitude
     */
    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth
        val latDistance = Math.toRadians(kotlin.math.abs(lat2 - lat1))
        val lonDistance = Math.toRadians(kotlin.math.abs(lon2 - lon1))
        val a = (kotlin.math.sin(latDistance / 2) * kotlin.math.sin(latDistance / 2)
                + (kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2))
                * kotlin.math.sin(lonDistance / 2) * kotlin.math.sin(lonDistance / 2)))
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        var distance = r * c * 1000 // distance in meter
        distance = distance.pow(2.0)
        return kotlin.math.sqrt(distance)
    }

    /**
    Check is user in the geofence or not
     */
    fun isInFence(
        setLat: Double,
        setLong: Double,
        yourLat: Double,
        yourLong: Double,
        radius: Double
    ): Boolean {
        return getDistance(setLat, setLong, yourLat, yourLong) <= radius
    }
}