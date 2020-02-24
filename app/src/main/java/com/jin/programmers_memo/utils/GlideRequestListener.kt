package com.jin.programmers_memo.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jin.programmers_memo.R
import org.jetbrains.anko.longToast

class GlideRequestListener {
    companion object {
        // 이미지 로드의 성공여부를 나타냅니다.
        var success: Boolean = false

        // 이미지 로드 리스너
        fun getListener(context: Context, isImport: Boolean) = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
            ): Boolean {
                success = false

                val exceptionClass =
                    e?.rootCauses?.firstOrNull()?.toString()?.split(":")?.firstOrNull()

                InfoToast.infoToast?.cancel()
                InfoToast.infoToast = context.longToast(
                    when {
                        /* 외부 이미지 로딩 예외처리
                        *  1. 입력 없을 떄
                        *  2. URL 입력 오류, 서버 자체 오류
                        *  3. 네트워크 연결 X (&& 캐시 X)
                        *
                        *  내부 이미지 로딩 예외처리
                        *  1. 내부 저장소에 저장된 이미지가 없을 때
                        *     (메모 작성 이후 이미지 삭제 && 어플 캐시 삭제)
                        * */
                        exceptionClass == null -> R.string.NullPointerException
                        exceptionClass == "java.io.FileNotFoundException" -> if (isImport) R.string.FileNotFoundException1 else R.string.FileNotFoundException2
                        !isInternetAvailable(context) -> R.string.UnknownHostException
                        else -> if (isImport) R.string.FileNotFoundException1 else R.string.FileNotFoundException2
                    }
                )

                return false
            }

            override fun onResourceReady(
                resource: Drawable?, model: Any?, target: Target<Drawable>?,
                dataSource: DataSource?, isFirstResource: Boolean
            ): Boolean {
                success = true

                return false
            }
        }

        private fun isInternetAvailable(context: Context): Boolean {
            var result = false

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            /* VERSION_CODES.Q부터 ConnectivityManager.activeNetworkInfo 기능이 Deprecated 되었습니다.
            *  따라서 VERSION_CODES.M을 기준으로 네트워크 연결 여부를 두 가지 방법으로 확인합니다.
            * */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                connectivityManager.run {
                    @Suppress("DEPRECATION")
                    activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }

                    }
                }
            }

            return result
        }
    }
}