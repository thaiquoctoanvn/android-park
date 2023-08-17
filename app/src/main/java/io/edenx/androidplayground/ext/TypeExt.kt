package io.edenx.androidplayground.ext

import android.content.res.Resources

/*
* Why the additional 0.5f?
* When you cast a float to an int, the float rounds. 0.0 -> 0.4 rounds to 0 while 0.5 -> 0.9 rounds to 1.
* This can create inconsistencies in the final ints. To prevent this, adding 0.5 ensures all rounding is to 1.
* Why? say your float is 0.3: 0.3 + 0.5 = 0.8 which rounds UP to 1. Say your float is 0.8: 0.8 + 0.5 = 1.3 which rounds DOWN to 1.
* Now you can be safe in your knowledge of the final rounding
* (SIDE NOTE: we add 0.5 rather than subtract to avoid getting a negative int)
* */
val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()