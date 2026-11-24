package com.absensi.jamaah

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.komoui.themes.KomoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private var nfcAdapter: NfcAdapter? = null
    private var nfcDidengarkan = false
    private var nfcAktifState by mutableStateOf(false)

    private val readerFlags = NfcAdapter.FLAG_READER_NFC_A or
        NfcAdapter.FLAG_READER_NFC_B or
        NfcAdapter.FLAG_READER_NFC_F or
        NfcAdapter.FLAG_READER_NFC_V or
        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

    /** Kartu NFC yang terbaca; dikonsumsi oleh layar Absensi NFC / pendaftaran kartu. */
    val nfcUid = kotlinx.coroutines.flow.MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        replay = 0
    )

    fun setNfcEnabled(enabled: Boolean) {
        nfcDidengarkan = enabled
        val adapter = nfcAdapter
        if (enabled && adapter != null && adapter.isEnabled) {
            // Reader mode mengambil alih NFC secara eksklusif, sehingga kartu tidak
            // dilempar ke aplikasi e-wallet / pembayaran bawaan HP.
            adapter.enableReaderMode(
                this,
                { tag ->
                    val uid = formatUidNfc(tag)
                    if (uid.isNotBlank()) nfcUid.tryEmit(uid)
                },
                readerFlags,
                null
            )
        } else {
            adapter?.disableReaderMode(this)
        }
    }

    fun bukaPengaturanNfc() {
        startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("pengaturan", MODE_PRIVATE)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAktifState = nfcAdapter?.isEnabled == true
        NotifikasiHelper.buatChannel(this)
        mintaIzinNotifikasi()
        PengingatMingguan.jadwalkan(this)
        setContent {
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean("mode_gelap", false)) }
            KomoTheme(
                isDarkTheme = isDarkMode,
                komoLightColors = KomoTerang,
                komoDarkColors = KomoGelap,
                materialLightColors = SkemaTerang,
                materialDarkColors = SkemaGelap
            ) {
                LatarGradasiIslami {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        MainAppScreen(
                            viewModel = viewModel,
                            isDarkMode = isDarkMode,
                            onToggleTheme = {
                                isDarkMode = !isDarkMode
                                prefs.edit().putBoolean("mode_gelap", isDarkMode).apply()
                            },
                            nfcUid = nfcUid,
                            onNfcActiveChange = { setNfcEnabled(it) },
                            onBukaPengaturanNfc = { bukaPengaturanNfc() },
                            nfcTersedia = nfcAdapter != null,
                            nfcAktif = nfcAktifState
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAktifState = nfcAdapter?.isEnabled == true
        setNfcEnabled(nfcDidengarkan)
    }

    override fun onPause() {
        setNfcEnabled(false)
        super.onPause()
    }

    private fun mintaIzinNotifikasi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
