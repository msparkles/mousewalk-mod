package lgbt.mouse.utils

import net.minecraft.util.math.BlockPos

object Cardinal {
    val CARDINALS = listOf(
        BlockPos(0, -1, 0),
        BlockPos(0, 1, 0),
        BlockPos(0, 0, -1),
        BlockPos(0, 0, 1),
        BlockPos(-1, 0, 0),
        BlockPos(1, 0, 0),
    )
    val CORNERS = listOf(
        BlockPos(0, -1, -1),
        BlockPos(0, -1, 1),
        BlockPos(0, 1, -1),
        BlockPos(0, 1, 1),
        BlockPos(-1, 0, -1),
        BlockPos(-1, 0, 1),
        BlockPos(1, 0, -1),
        BlockPos(1, 0, 1),
        BlockPos(-1, -1, 0),
        BlockPos(-1, 1, 0),
        BlockPos(1, -1, 0),
        BlockPos(1, 1, 0),
    )
    val X_CARDINALS = listOf(
        CARDINALS[0],
        CARDINALS[1],
        CARDINALS[2],
        CARDINALS[3],
    )
    val X_CARDINALS_AND_CORNERS = X_CARDINALS + listOf(
        CORNERS[0],
        CORNERS[1],
        CORNERS[2],
        CORNERS[3],
    )
    val Y_CARDINALS = listOf(
        CARDINALS[2],
        CARDINALS[3],
        CARDINALS[4],
        CARDINALS[5],
    )
    val Y_CARDINALS_AND_CORNERS = Y_CARDINALS + listOf(
        CORNERS[4],
        CORNERS[5],
        CORNERS[6],
        CORNERS[7],
    )
    val Z_CARDINALS = listOf(
        CARDINALS[0],
        CARDINALS[1],
        CARDINALS[4],
        CARDINALS[5],
    )
    val Z_CARDINALS_AND_CORNERS = Z_CARDINALS + listOf(
        CORNERS[8],
        CORNERS[9],
        CORNERS[10],
        CORNERS[11],
    )
}