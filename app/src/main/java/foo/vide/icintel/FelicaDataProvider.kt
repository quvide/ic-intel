package foo.vide.icintel

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot.Companion.withMutableSnapshot
import androidx.core.content.IntentCompat
import com.goroya.kotlinfelicalib.FelicaLib
import com.goroya.kotlinfelicalib.command.BlockElement
import com.goroya.kotlinfelicalib.command.PollingCC
import com.goroya.kotlinfelicalib.command.PollingRC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private val logger = Logger.forTag("FelicaDataProvider")

interface FelicaDataProvider {
    val balance: UInt?
    val lastUpdated: Instant?
    val error: String?
}

class FelicaDataProviderImpl : FelicaDataProvider {
    override var balance by mutableStateOf<UInt?>(null)
    override var lastUpdated by mutableStateOf<Instant?>(null)
    override var error by mutableStateOf<String?>(null)

    suspend fun fromIntent(intent: Intent) {
        val tag = intent.let { IntentCompat.getParcelableExtra(it, NfcAdapter.EXTRA_TAG, Tag::class.java) }
        if (tag != null) {
            fetch(tag)
        }
    }

    private suspend fun fetch(tag: Tag) = withContext(Dispatchers.IO) {
        val flib = FelicaLib(tag)
        try {
            val pollingRes: PollingRC = flib.polling(
                systemCode = 0xFFFF,
                requestCode = PollingCC.RequestCode.SystemCodeRequest,
                timeSlot = PollingCC.TimeSlot.MaximumNumberOfSlot1
            )

            val readBalance = flib.readWithoutEncryption(
                idm = pollingRes.idm,
                serviceCodeList = intArrayOf(0x00, 0x8B).reversedArray(),
                blockList = ArrayList<BlockElement>().apply {
                    add(
                        BlockElement(
                            length = BlockElement.Length.BlockListElementOf2Byte,
                            accessMode = BlockElement.AccessMode.ReadOperationOrWriteOperation,
                            serviceCodeListOrder = 0,
                            blockNumber = 0
                        )
                    )
                }
            )

            withMutableSnapshot {
                error = null
                balance = readBalance
                    .blockData[0]
                    .sliceArray(11..12)
                    .run { this[0].toUInt() + this[1].toUInt().shl(8) }

                lastUpdated = Clock.System.now()
            }
        } catch (e: Exception) {
            logger.e("Failed to read:", e)
            withMutableSnapshot {
                lastUpdated = Clock.System.now()
                error = "ERROR"
            }
        }
    }
}