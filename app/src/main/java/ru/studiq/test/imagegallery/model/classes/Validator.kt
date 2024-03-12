package ru.studiq.test.imagegallery.model.classes

import android.text.TextUtils
import android.util.Patterns
import java.util.regex.Pattern

class Validator {
    companion object {
        fun isValidEmail(email: String?): Boolean {
            return if (TextUtils.isEmpty(email)) {
                false
            } else {
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        }
        fun isValidLogin(login: String): Boolean {
            val regExpn = "^([a-zA-Z]{4,24})?([a-zA-Z][a-zA-Z0-9_]{4,24})$"
            val inputStr: CharSequence = login
            val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(inputStr)
            return matcher.matches()
        }
        fun isValidSearchQuery(query: String): Boolean {
            val regExpn = "^([a-zA-Z]{1,24})?([a-zA-Z][a-zA-Z0-9_]{1,24})$"
            val inputStr: CharSequence = query
            val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(inputStr)
            return matcher.matches()
        }

        fun isValidPassword(password: String): Boolean {
            val regExpn = "^[a-z0-9_]{6,24}$"
            val inputStr: CharSequence = password
            val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(inputStr)
            return matcher.matches()
        }

    }
}