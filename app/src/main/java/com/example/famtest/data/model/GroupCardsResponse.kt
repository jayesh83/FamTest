package com.example.famtest.data.model

/**
Note: dataclass variable names are already written as serialized name i.e. no need of @Serialize
 **/
data class GroupCardsResponse(
    val card_groups: List<GroupCard>
)

data class GroupCard(
    val name: String,
    val design_type: String,
    val cards: List<Card>,
    val height: Int?,           // in dp, only used for HC9
    val is_scrollable: Boolean  // not used for HC9
) {
    fun designType(): DesignType {
        return when (design_type) {
            "HC3" -> DesignType.BIG_DISPLAY_CARD
            "HC1" -> DesignType.SMALL_DISPLAY_CARD
            "HC5" -> DesignType.IMAGE_CARD
            "HC6" -> DesignType.SMALL_CARD_WITH_ARROW
            "HC9" -> DesignType.DYNAMIC_WIDTH_CARD
            else -> DesignType.SMALL_DISPLAY_CARD // assuming a fallback design type
        }
    }
}

data class Card(
    val name: String,
    val formatted_title: FormattedText?,
    val title: String?,
    val formatted_description: FormattedText?,
    val description: String?,
    val icon: CardImage?,
    val url: String?,
    val bg_image: CardImage?,
    val bg_color: String?,
    val bg_gradient: Gradient?,
    val cta: List<CallToAction>
)

data class CardImage(
    val image_type: String,    // can be one of 'asset' and 'external'
    val asset_type: String?,
    val image_url: String?
)

data class CallToAction(
    val text: String,
    val bg_color: String?,
    val url: String?,
    val text_color: String?
)

data class Gradient(
    val colors: List<String>,
    val angle: Short? = 0
)

data class FormattedText(
    val text: String,
    val entities: List<Entity>
)

data class Entity(
    val text: String,
    val color: String?,
    val url: String?,
    val font_style: String?    // can be one of 'underline' and 'italic'
)

enum class DesignType(name: String) {
    BIG_DISPLAY_CARD("HC3"),
    SMALL_DISPLAY_CARD("HC1"),
    IMAGE_CARD("HC5"),
    SMALL_CARD_WITH_ARROW("HC6"),
    DYNAMIC_WIDTH_CARD("HC9") /* HC9 is a special type of card, its width is not predefined.
                                    Its height is equal to height specified in its parent card group,
                                    and width is dynamic and depends on the size of the bg_image of the card.*/
}
