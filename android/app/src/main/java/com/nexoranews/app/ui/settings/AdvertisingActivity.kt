package com.nexoranews.app.ui.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexoranews.app.R
import com.nexoranews.app.data.repository.NewsRepository
import com.nexoranews.app.databinding.ActivityAdvertisingBinding
import kotlinx.coroutines.launch

class AdvertisingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdvertisingBinding
    private lateinit var repository: NewsRepository

    private val adFormats = listOf(
        "Hero Banner Principal de Topo (Foto / Vídeo)",
        "Banner na Barra Lateral & Notícias",
        "Publirreportagem / Artigo Patrocinado",
        "Patrocínio Exclusivo de Categoria",
        "Pacote Completo Multimídia 360°"
    )

    private val adFormatCodes = listOf(
        "hero_banner",
        "sidebar_banner",
        "publirreportagem",
        "patrocinio_categoria",
        "pacote_completo"
    )

    private val budgets = listOf(
        "Campanha Curta (1 a 2 Semanas)",
        "Campanha Mensal (30 Dias)",
        "Pacote Trimestral (Desconto Especial)",
        "Parceria Anual Contínua"
    )

    private val budgetCodes = listOf(
        "1_semana",
        "1_mes",
        "trimestral",
        "anual"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdvertisingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = NewsRepository(this)

        setupDropdowns()
        setupSubmitButton()
    }

    private fun setupDropdowns() {
        binding.actvAdFormat.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                adFormats
            )
        )

        binding.actvBudget.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                budgets
            )
        )
    }

    private fun setupSubmitButton() {
        binding.btnSubmitProposal.setOnClickListener {
            submitProposal()
        }
    }

    private fun submitProposal() {
        val company = binding.etCompany.text?.toString()?.trim().orEmpty()
        val contactName = binding.etContactName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val phone = binding.etPhone.text?.toString()?.trim().orEmpty()
        val message = binding.etMessage.text?.toString()?.trim().orEmpty()

        if (company.isEmpty()) {
            binding.etCompany.error = "Informe o nome da empresa."
            binding.etCompany.requestFocus()
            return
        }

        if (contactName.isEmpty()) {
            binding.etContactName.error = "Informe o nome do responsável."
            binding.etContactName.requestFocus()
            return
        }

        if (email.isEmpty() || !email.contains("@")) {
            binding.etEmail.error = "Informe um email válido."
            binding.etEmail.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Informe o telefone de contacto."
            binding.etPhone.requestFocus()
            return
        }

        val formatIndex = adFormats.indexOf(binding.actvAdFormat.text.toString())
            .coerceAtLeast(0)

        val budgetIndex = budgets.indexOf(binding.actvBudget.text.toString())
            .coerceAtLeast(0)

        val adFormat = adFormatCodes[formatIndex]
        val budget = budgetCodes[budgetIndex]

        binding.btnSubmitProposal.isEnabled = false
        binding.btnSubmitProposal.text = "A registar solicitação..."

        lifecycleScope.launch {
            val result = repository.submitAdProposal(
                company = company,
                contactName = contactName,
                email = email,
                phone = phone,
                adFormat = adFormat,
                budget = budget,
                message = message
            )

            binding.btnSubmitProposal.isEnabled = true
            binding.btnSubmitProposal.text = "Solicitar Mídia Kit & Proposta"

            result.onSuccess {
                Toast.makeText(
                    this@AdvertisingActivity,
                    "Proposta enviada com sucesso! A equipa comercial entrará em contacto.",
                    Toast.LENGTH_LONG
                ).show()

                binding.etCompany.text?.clear()
                binding.etContactName.text?.clear()
                binding.etEmail.text?.clear()
                binding.etPhone.text?.clear()
                binding.etMessage.text?.clear()
            }

            result.onFailure { error ->
                Toast.makeText(
                    this@AdvertisingActivity,
                    "Não foi possível enviar a solicitação: ${error.message ?: "erro de ligação"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
