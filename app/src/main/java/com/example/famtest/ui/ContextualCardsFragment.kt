package com.example.famtest.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.toSpanned
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.famtest.R
import com.example.famtest.data.api.FamService
import com.example.famtest.data.model.Card
import com.example.famtest.data.model.DesignType
import com.example.famtest.data.model.FormattedText
import com.example.famtest.data.model.GroupCardsResponse
import com.example.famtest.data.repository.ContextualCardsRepository
import com.example.famtest.state.ContextualCardsVM
import com.example.famtest.state.ContextualCardsVmFactory
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

class ContextualCardsFragment : Fragment() {
    private lateinit var viewModel: ContextualCardsVM
    private val groupCards: MutableLiveData<GroupCardsResponse> = MutableLiveData()
    private lateinit var displayMetrics: DisplayMetrics


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val service: FamService = FamService.create()
        viewModel =
            ViewModelProvider(
                this.viewModelStore,
                ContextualCardsVmFactory(ContextualCardsRepository(service)) // TODO: use di
            ).get(ContextualCardsVM::class.java)

        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayMetrics = resources.displayMetrics
        val linearLayoutContainer = view.findViewById<LinearLayout>(R.id.ll_container)

        viewModel.contextualCards.observe(viewLifecycleOwner) {
            val totalCardGroups = it.card_groups.size
            linearLayoutContainer.removeAllViews()
            for (i in 0 until totalCardGroups) {
                val totalInnerCards = it.card_groups[i].cards.size

                val cardGroup = it.card_groups[i]
                for (j in 0 until totalInnerCards) {
                    when (cardGroup.designType()) {
                        DesignType.BIG_DISPLAY_CARD -> {
                            attachView(linearLayoutContainer, createSampleCard("Card no: ${i + j}"))
                        }
                        DesignType.SMALL_DISPLAY_CARD -> {
                            attachView(linearLayoutContainer, smallDisplayCard(cardGroup.cards[j]))
                        }
                        DesignType.IMAGE_CARD -> {
                            attachView(linearLayoutContainer, createSampleCard("Card no: ${i + j}"))
                        }
                        DesignType.SMALL_CARD_WITH_ARROW -> {
                            attachView(linearLayoutContainer, createSampleCard("Card no: ${i + j}"))
                        }
                        DesignType.DYNAMIC_WIDTH_CARD -> {
                            attachView(linearLayoutContainer, createSampleCard("Card no: ${i + j}"))
                        }
                    }
                }
            }
        }
    }

    private fun attachView(container: ViewGroup, view: View) {
        container.addView(view)
    }

    private fun createText(text: String): TextView {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.setMargins(16, 16, 16, 16)
        }

        return TextView(requireContext()).apply {
            this.layoutParams = params
            this.text = text
            this.setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createSampleCard(text: String): MaterialCardView {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.setMargins(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }

        return MaterialCardView(requireContext()).apply {
            this.addView(createText(text))
            this.radius = 15f
            this.elevation = 8f
            this.maxCardElevation = 8f
            this.isClickable = true
            this.setCardBackgroundColor(Color.LTGRAY)
            this.layoutParams = params
        }
    }

    private fun smallDisplayCard(card: Card): MaterialCardView {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.setMargins(16.dp(), 16.dp(), 16.dp(), 16.dp())
        }

        return MaterialCardView(requireContext()).apply {
            val title = (formattedText(card.formatted_title) ?: card.title) ?: "Title"
            this.addView(createText(title))
            this.radius = 30f
            this.elevation = 10f
            this.maxCardElevation = 12f
            this.isClickable = true
            this.setCardBackgroundColor(Color.WHITE)
            this.layoutParams = params
        }
    }

    private fun formattedText(text: FormattedText?): String? {
        return if (text != null) {
            val entities = text.entities
            val replaceableSpots = findBlankSpot(text.text)
            replaceableSpots.forEachIndexed { i, range ->
                val txt = entities[i].text
                val txtLength = txt.length
                val color = entities[i].color
                val url = entities[i].url
                val fontStyle = entities[i].font_style

                val spans = SpannableString(txt)

                if (color != null) {
                    val txtColor = try {
                        Color.parseColor(color)
                    } catch (e: IllegalArgumentException) {
                        getColor(requireContext(), R.color.black)
                    }
                    spans.setSpan(
                        ForegroundColorSpan(txtColor),
                        0,
                        txtLength,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                if (url != null) {
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            Log.e("Clicked", "yes") // TODO: Redirect to browser
                        }
                    }
                    spans.setSpan(clickableSpan, 0, txtLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                if (fontStyle != null) {
                    if (fontStyle == "italic")
                        spans.setSpan(
                            Typeface.ITALIC,
                            0,
                            txtLength,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    if (fontStyle == "underline")
                        spans.setSpan(
                            UnderlineSpan(),
                            0,
                            txtLength,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                }
                text.text.replaceRange(range, spans.toSpanned())
            }
            text.text
        } else null
    }

    private fun findBlankSpot(str: String): ArrayList<IntRange> {
        val pattern = Regex("""\{{1}\}""")
        val match = pattern.findAll(str)
        val matchIndexes = arrayListOf<IntRange>()
        match.iterator().forEach { result ->
            matchIndexes.add(result.range)
        }
        return matchIndexes
    }

    private fun Int.dp(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            displayMetrics
        ).roundToInt()
    }
}