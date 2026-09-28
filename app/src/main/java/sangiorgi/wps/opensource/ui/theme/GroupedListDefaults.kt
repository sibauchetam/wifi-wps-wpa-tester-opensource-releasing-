package sangiorgi.wps.opensource.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Shapes for grouped lists (a row group that reads as one continuous surface).
 *
 * The first row keeps the generous top corners of the group, the last row keeps
 * the generous bottom corners, and the rows in between are flat so the group
 * reads as a single object - the grouped-list pattern used throughout the
 * WikiReader reference design. Pair with [androidx.compose.material3.HorizontalDivider]
 * between rows and flush (zero-gap) items.
 */
object GroupedListDefaults {

    /** Nearly flat corner for the inner edges of a group. */
    private val flat = RoundedCornerShape(2.dp).topStart

    /** A standalone row that is also the whole group. */
    val single: CornerBasedShape
        @Composable get() = MaterialTheme.shapes.large

    /** First row of a group. */
    val top: CornerBasedShape
        @Composable get() = RoundedCornerShape(
            topStart = MaterialTheme.shapes.large.topStart,
            topEnd = MaterialTheme.shapes.large.topEnd,
            bottomStart = flat,
            bottomEnd = flat,
        )

    /** Middle row of a group. */
    val middle: CornerBasedShape
        @Composable get() = RoundedCornerShape(
            topStart = flat,
            topEnd = flat,
            bottomStart = flat,
            bottomEnd = flat,
        )

    /** Last row of a group. */
    val bottom: CornerBasedShape
        @Composable get() = RoundedCornerShape(
            topStart = flat,
            topEnd = flat,
            bottomStart = MaterialTheme.shapes.large.bottomStart,
            bottomEnd = MaterialTheme.shapes.large.bottomEnd,
        )
}
