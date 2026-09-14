package io.edenx.androidpark.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * The XML palette names every colour after its own hex code (_efedff, _1a1a2b,
 * ...), so there is nothing to reuse. Introducing Compose is the cheap moment
 * to give the same values semantic names, without touching a single layout.
 *
 * Each constant notes the @color/ name it mirrors, so the two stay in step
 * until the last XML sample is gone.
 */
internal val Ink = Color(0xFF030318)        // _030318
internal val Surface = Color(0xFF1A1A2B)    // _1a1a2b
internal val OnSurface = Color(0xFFEFEDFF)  // _efedff
internal val Accent = Color(0xFFE04386)     // _e04386
internal val Slate = Color(0xFF2C3639)      // _2c3639
internal val Mist = Color(0xFFCFD0D2)       // _cfd0d2
internal val Grey = Color(0xFFBBBBBE)       // _bbbbbe

internal val Purple500 = Color(0xFF6200EE)  // purple_500
internal val Purple700 = Color(0xFF3700B3)  // purple_700
internal val Teal200 = Color(0xFF03DAC5)    // teal_200
