package com.nexoranews.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nexoranews.app.BuildConfig
import com.nexoranews.app.R
import com.nexoranews.app.data.local.PreferencesManager
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: PreferencesManager
    private lateinit var repository: NewsRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        prefsManager = PreferencesManager(context)
        repository = NewsRepository(context)

        setupUI()
    }

    private fun setupUI() {
        val context = requireContext()

        // Notification Switch
        binding.switchNotifications.isChecked = prefsManager.isNotificationsEnabled
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isNotificationsEnabled = isChecked
            val msg = if (isChecked) "Notificações ativadas" else "Notificações silenciadas"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        // Cache Size & Clear Action
        updateCacheSize()
        binding.cardClearCache.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_cache_title)
                .setMessage("Deseja limpar os dados armazenados em cache local? As notícias serão recarregadas automaticamente quando estiver online.")
                .setPositiveButton("Limpar") { _, _ ->
                    repository.clearCache()
                    updateCacheSize()
                    Toast.makeText(context, R.string.settings_cache_cleared, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // App Version
        val versionName = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: Exception) {
            "1.0.0"
        }
        binding.tvAppVersion.text = getString(R.string.settings_version_format, versionName)
    }

    private fun updateCacheSize() {
        val size = repository.getCacheSize()
        binding.tvCacheSizeSubtitle.text = "$size armazenados neste dispositivo"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
