package org.betterseqta.betterseqtateachandroid.ui.motion

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier

/** Staggered list entrance via Lazy list item animation API. */
fun LazyItemScope.premiumListItemModifier(): Modifier = Modifier.animateItem()
