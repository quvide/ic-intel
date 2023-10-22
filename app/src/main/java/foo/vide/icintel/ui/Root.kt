package foo.vide.icintel.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import foo.vide.icintel.FelicaDataProvider
import foo.vide.icintel.ui.theme.ICIntelTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun Root(fetcher: FelicaDataProvider) {
    ICIntelTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box {
                when (val error = fetcher.error) {
                    null -> {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "¥${"%,d".format(fetcher.balance.toInt())}",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }

                    else -> {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = error,
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.Red
                        )
                    }
                }

                fetcher.last_updated?.let {
                    Text(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        text = "${it.toLocalDateTime(TimeZone.currentSystemDefault())}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private class PProvider : PreviewParameterProvider<UInt> {
    override val values = sequenceOf(0u, 10u, 100u, 1000u, 10000u)
}

@Preview
@Composable
private fun Preview(@PreviewParameter(PProvider::class) balance: UInt) {
    Root(object : FelicaDataProvider {
        override val balance = balance
        override val last_updated = Clock.System.now()
        override val error = null
    })
}

@Preview
@Composable
private fun PreviewError() {
    Root(object : FelicaDataProvider {
        override val balance = 0u
        override val last_updated = Clock.System.now()
        override val error = "ERROR"
    })
}

