package cc.aoeiuv020.log.level

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.loglevelchanger.R
import kotlinx.android.synthetic.main.activity_log_level_test.*
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity

class LogLevelTestActivity : AppCompatActivity() {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<LogLevelTestActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_level_test)

        LogLevelManager.prepare(this)

        tvLevel.setOnClickListener {
            val tag = etTag.text.toString()
            selector("选择日志级别", LogLevel.values().map { it.name }) { _, i ->
                val level = LogLevel.values()[i]
                LogLevelManager.setLogLevel(tag, level)
            }
        }

    }
}
