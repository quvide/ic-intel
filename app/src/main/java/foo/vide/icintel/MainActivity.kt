package foo.vide.icintel

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import foo.vide.icintel.ui.Root
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private var adapter: NfcAdapter? = null
    private lateinit var singleTopIntent: Intent
    private lateinit var pendingIntent: PendingIntent
    private val felicaProvider = FelicaDataProviderImpl()
    private val techListsArray = arrayOf(arrayOf<String>(NfcF::class.java.name))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Root(felicaProvider)
        }

        adapter = NfcAdapter.getDefaultAdapter(this)
        singleTopIntent = Intent(this, javaClass).apply { addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) }
        pendingIntent = PendingIntent.getActivity(this, 0, singleTopIntent, PendingIntent.FLAG_MUTABLE)

        lifecycleScope.launch {
            felicaProvider.fromIntent(intent)
        }
    }

    public override fun onPause() {
        super.onPause()
        adapter?.disableForegroundDispatch(this)
    }

    public override fun onResume() {
        super.onResume()
        adapter?.enableForegroundDispatch(this, pendingIntent, null, techListsArray)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            felicaProvider.fromIntent(intent)
        }
    }
}