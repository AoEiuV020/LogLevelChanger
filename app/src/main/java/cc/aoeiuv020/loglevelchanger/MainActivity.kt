package cc.aoeiuv020.loglevelchanger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.adb.AdbTestActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAdbTest.setOnClickListener {
            AdbTestActivity.start(this)
        }
    }
}
