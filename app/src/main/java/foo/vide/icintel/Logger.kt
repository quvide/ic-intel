package foo.vide.icintel

import android.util.Log
import java.lang.Exception

class Logger private constructor(private val tag: String) {
    fun trace(msg: String) = Log.v(tag, msg)
    fun v(msg: String) = Log.v(tag, msg)

    fun debug(msg: String) = Log.d(tag, msg)
    fun d(msg: String) = debug(msg)
    fun d(msg: Any?) = debug(msg.toString())

    fun e(msg: String, tr: Throwable) = Log.e(tag, msg, tr)

    companion object {
        private const val TAG_PREFIX = "IC_"
        fun forTag(tag: String) = Logger("$TAG_PREFIX$tag")
    }
}