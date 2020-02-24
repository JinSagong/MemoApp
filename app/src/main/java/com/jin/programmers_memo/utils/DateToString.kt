package com.jin.programmers_memo.utils

import java.text.SimpleDateFormat
import java.util.*

class DateToString {
    companion object {
        fun getDateString(date: Long): String = if (Locale.getDefault() == Locale.KOREA)
            SimpleDateFormat("yyyy년 M월 d일 a h:mm", Locale.getDefault()).format(Date(date))
        else SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault()).format(Date(date))
    }
}