package com.example.famtest

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.toSpanned
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.famtest.data.api.FamService
import com.example.famtest.data.model.*
import com.example.famtest.data.repository.ContextualCardsRepository
import com.example.famtest.state.ContextualCardsVM
import com.example.famtest.state.ContextualCardsVmFactory
import kotlin.math.roundToInt

class CardsComponent(
    private val fragment: Fragment,
    private val context: Context,
    private val rootView: ViewGroup
) :
    LifecycleObserver {
    private lateinit var viewModel: ContextualCardsVM
    private val displayMetrics = fragment.resources.displayMetrics

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        // fetch the data
        val service: FamService = FamService.create()
        viewModel =
            ViewModelProvider(
                fragment.viewModelStore,
                ContextualCardsVmFactory(ContextualCardsRepository(service)) // TODO: use di
            ).get(ContextualCardsVM::class.java)

        viewModel.contextualCards.observe(fragment.viewLifecycleOwner) {
            plotCards(it, context, rootView)
        }
    }

    private fun plotCards(data: GroupCardsResponse, context: Context, rootView: ViewGroup) {
        val totalCardGroups = data.card_groups.size
        rootView.removeAllViews()
        for (i in 0 until totalCardGroups) {
            val cardGroup = data.card_groups[i]
            val designType = cardGroup.designType()

            if (cardGroup.is_scrollable) {
                val scrollView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_dynamic_width_card, rootView, false)
                val horizontalView =
                    scrollView.findViewById<LinearLayout>(R.id.horizontal_ll_container)
                horizontalView.removeAllViews()
                cardGroup.cards.forEachIndexed { index, card ->
                    val cardView =
                        getCard(cardGroup, designType, card, horizontalView)
                    horizontalView.addView(cardView, index)
                }
                rootView.addView(scrollView)
            } else {
                cardGroup.cards.forEachIndexed { index, card ->
                    val cardView =
                        getCard(cardGroup, designType, card, rootView)
                    rootView.addView(cardView, index)
                }
            }
        }
    }

    private fun getCard(
        cardGroup: GroupCard,
        designType: DesignType,
        cardData: Card,
        rootView: ViewGroup
    ): View {
        return when (designType) {
            DesignType.BIG_DISPLAY_CARD -> {
                bigDisplayCard(cardData, rootView)
            }
            DesignType.SMALL_DISPLAY_CARD -> {
                smallDisplayCard(cardData, rootView)
            }
            DesignType.IMAGE_CARD -> {
                imageCard(cardData, rootView)
            }
            DesignType.SMALL_CARD_WITH_ARROW -> {
                smallDisplayCardWithArrow(cardData, rootView)

            }
            DesignType.DYNAMIC_WIDTH_CARD -> {
                dynamicWidthCard(cardGroup, cardData, rootView)
            }
        }
    }

    private fun smallDisplayCard(card: Card, root: ViewGroup): CardView {
        val view =
            fragment.layoutInflater.inflate(
                R.layout.layout_small_display_card,
                root,
                false
            ) as CardView

        if (card.url != null) {
            view.isClickable = true
            view.setOnClickListener { openInBrowser(card.url) }
        }

        if (card.bg_color != null) {
            val backCard = view.findViewById<CardView>(R.id.root_small_display_card)
            backCard.setCardBackgroundColor(parseColor(card.bg_color))
        }

        val title = (formattedText(card.formatted_title) ?: card.title) ?: "Title"
        view.findViewById<TextView>(R.id.tv_small_dip_card_title).text = title

        val description =
            (formattedText(card.formatted_description) ?: card.description) ?: ""

        if (description != "") {
            val iconView = view.findViewById<TextView>(R.id.tv_small_dip_card_description)
            iconView.visibility = View.VISIBLE
            iconView.text = description
        }

        if (card.icon != null) {
            if (card.icon.image_type == "ext" && card.icon.image_url != null) {
                val iconView = view.findViewById<ImageView>(R.id.iv_small_dip_card_icon)
                Glide.with(context)
                    .load(card.icon.image_url)
                    .placeholder(R.color.purple_700)
                    .error(R.color.design_default_color_error)
                    .into(iconView)
            }
            if (card.icon.image_type == "asset" && card.icon.asset_type != null)
                Log.e("YesIcon", "asset type ${card.icon.asset_type}") // TODO: load asset
        }
        return view
    }

    private fun bigDisplayCard(card: Card, root: ViewGroup): CardView {
        val view =
            fragment.layoutInflater.inflate(
                R.layout.layout_big_display_card,
                root,
                false
            ) as CardView
        val rootCardView = view.findViewById<CardView>(R.id.root_big_display_card)

        rootCardView.setOnLongClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.left_to_right))
            true
        }
        view.findViewById<Button>(R.id.button_remind_later).setOnClickListener {
            rootCardView.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.right_to_left
                )
            )
        }
        view.findViewById<Button>(R.id.button_remind_later).setOnClickListener {
            rootCardView.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.right_to_left
                )
            )
        }

        if (card.url != null) {
            view.isClickable = true
            rootCardView.setOnClickListener { openInBrowser(card.url) }
        }

        if (card.bg_color != null) {
            rootCardView.setCardBackgroundColor(parseColor(card.bg_color))
        }

        val title = (formattedText(card.formatted_title) ?: card.title) ?: "Title"
        rootCardView.findViewById<TextView>(R.id.tv_big_dip_card_title).text = title

        val description =
            (formattedText(card.formatted_description) ?: card.description) ?: ""

        if (description != "") {
            val desc = rootCardView.findViewById<TextView>(R.id.tv_big_dip_card_description)
            desc.visibility = View.VISIBLE
            desc.text = description
        }

        if (card.bg_image != null) {
            if (card.bg_image.image_type == "ext" && card.bg_image.image_url != null) {
                val imageView = rootCardView.findViewById<ImageView>(R.id.iv_big_dip_card_icon)
                Glide.with(context)
                    .load(card.bg_image.image_url)
                    .transform(RoundedCorners(10.dp()))
                    .override(100.dp(), 100.dp())
                    .centerCrop()
                    .placeholder(R.color.purple_700)
                    .error(R.color.design_default_color_error)
                    .into(imageView)
            }
        }
        // setup call to action
        val cta = card.cta
        if (cta != null && cta.isNotEmpty()) {
            val action = cta[0]
            val actionButton = rootCardView.findViewById<Button>(R.id.button_big_dip_card_action)
            actionButton.text = action.text
            action.text_color?.also { actionButton.setTextColor(parseColor(it)) }
            action.bg_color?.also { actionButton.setBackgroundColor(parseColor(it)) }
            actionButton.setOnClickListener { openInBrowser(action.url) }
        }

        return view
    }

    private fun smallDisplayCardWithArrow(card: Card, root: ViewGroup): CardView {
        val view =
            fragment.layoutInflater.inflate(
                R.layout.layout_small_display_card_arrow,
                root,
                false
            ) as CardView

        if (card.url != null) {
            view.isClickable = true
            view.setOnClickListener { openInBrowser(card.url) }
        }

        if (card.bg_color != null) {
            val backCard = view.findViewById<CardView>(R.id.root_small_display_card)
            backCard.setCardBackgroundColor(parseColor(card.bg_color))
        }

        val title = (formattedText(card.formatted_title) ?: card.title) ?: "Title"
        view.findViewById<TextView>(R.id.tv_small_dip_card_title).text = title

        val description =
            (formattedText(card.formatted_description) ?: card.description) ?: ""

        if (description != "") {
            val iconView = view.findViewById<TextView>(R.id.tv_small_dip_card_description)
            iconView.visibility = View.VISIBLE
            iconView.text = description
        }

        if (card.icon != null) {
            if (card.icon.image_type == "ext" && card.icon.image_url != null) {
                val iconView = view.findViewById<ImageView>(R.id.iv_small_dip_card_icon)
                Glide.with(context)
                    .load(card.icon.image_url)
                    .placeholder(R.color.purple_700)
                    .error(R.color.design_default_color_error)
                    .into(iconView)
            }
            if (card.icon.image_type == "asset" && card.icon.asset_type != null)
                Log.e("YesIcon", "asset type ${card.icon.asset_type}") // TODO: load asset
        }

        return view
    }

    private fun dynamicWidthCard(cardGroup: GroupCard, card: Card, root: ViewGroup): View {
        val view =
            fragment.layoutInflater.inflate(
                R.layout.layout_card_with_image,
                root,
                false
            )

        view.updateLayoutParams {
            if (cardGroup.height != null)
                this.height = cardGroup.height.dp()
        }

        if (card.url != null) {
            view.isClickable = true
            view.setOnClickListener { openInBrowser(card.url) }
        }

        if (card.bg_color != null) {
            val backCard = view.findViewById<CardView>(R.id.card_image)
            backCard.setCardBackgroundColor(parseColor(card.bg_color))
        }

        if (card.bg_image != null) {
            if (card.bg_image.image_type == "ext" && card.bg_image.image_url != null) {
                val imageView = view.findViewById<ImageView>(R.id.image_view)
                Glide.with(context)
                    .load(card.bg_image.image_url)
                    .transform(RoundedCorners(10.dp()))
                    .placeholder(R.color.purple_700)
                    .error(R.color.design_default_color_error)
                    .into(imageView)
            }
        }

        return view
    }

    private fun imageCard(card: Card, root: ViewGroup): View {
        val view =
            fragment.layoutInflater.inflate(
                R.layout.layout_image_card,
                root,
                false
            )
        val backCard = view.findViewById<CardView>(R.id.card_image)
        if (card.url != null) {
            backCard.isClickable = true
            backCard.setOnClickListener { openInBrowser(card.url) }
        }

        if (card.bg_color != null)
            backCard.setCardBackgroundColor(parseColor(card.bg_color))

        if (card.bg_image != null) {
            if (card.bg_image.image_type == "ext" && card.bg_image.image_url != null) {
                val imageView = view.findViewById<ImageView>(R.id.image_view)
                Glide.with(context)
                    .load(card.bg_image.image_url)
                    .transform(RoundedCorners(10.dp()))
                    .placeholder(R.color.purple_700)
                    .error(R.color.design_default_color_error)
                    .into(imageView)
            }
        }

        return view
    }

    private fun parseColor(colorString: String): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: IllegalArgumentException) {
            ContextCompat.getColor(context, R.color.design_default_color_surface)
        } catch (e: Exception) {
            ContextCompat.getColor(context, R.color.design_default_color_surface)
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
                        ContextCompat.getColor(context, R.color.black)
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
                            openInBrowser(url)
                        }
                    }
                    spans.setSpan(
                        clickableSpan,
                        0,
                        txtLength,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
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
        val pattern = Regex("""\{{1}\}""")    // regex to find " {} " in the string
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

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            fragment.startActivity(intent)
        } catch (e: Exception) {
            toast("Cannot open in browser")
        }
    }

    private fun toast(text: String) {
        Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}
