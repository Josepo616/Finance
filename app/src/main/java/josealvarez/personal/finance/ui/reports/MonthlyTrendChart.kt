package josealvarez.personal.finance.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import josealvarez.personal.finance.model.MonthlyTrend

@Composable
fun MonthlyTrendChart(
    trend: List<MonthlyTrend>,
    selectedMonth: Int,
    selectedYear: Int,
    modifier: Modifier = Modifier
) {
    if (trend.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val maxExpense = trend.maxOfOrNull { it.totalExpenses }?.coerceAtLeast(1.0) ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Expense Trend (Last 6 Months)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 24.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = (canvasWidth / (trend.size * 2))
                val spacing = canvasWidth / trend.size

                trend.forEachIndexed { index, item ->
                    val isSelected = item.month == selectedMonth && item.year == selectedYear
                    val barHeight = (item.totalExpenses / maxExpense * canvasHeight).toFloat()
                    
                    val x = (index * spacing) + (spacing / 2) - (barWidth / 2)
                    val y = canvasHeight - barHeight

                    // Draw bar
                    drawRoundRect(
                        color = if (isSelected) primaryColor else mutedColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Draw label (month)
                    drawIntoCanvas {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK // Simplified for now, should use theme
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 12.dp.toPx()
                        }
                        it.nativeCanvas.drawText(
                            item.label,
                            x + (barWidth / 2),
                            canvasHeight + 20.dp.toPx(),
                            paint
                        )
                    }

                    // Draw amount label if space allows or just for selected
                    if (isSelected || item.totalExpenses > 0) {
                        drawIntoCanvas {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.DKGRAY
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 10.dp.toPx()
                                isFakeBoldText = isSelected
                            }
                            it.nativeCanvas.drawText(
                                "$%.0f".format(item.totalExpenses),
                                x + (barWidth / 2),
                                y - 8.dp.toPx(),
                                paint
                            )
                        }
                    }
                }
            }
        }
    }
}
