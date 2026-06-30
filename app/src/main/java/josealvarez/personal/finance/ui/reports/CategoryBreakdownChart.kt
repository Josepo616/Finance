package josealvarez.personal.finance.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import josealvarez.personal.finance.model.CategorySpending

private val ChartColors = listOf(
    Color(0xFF4285F4), // Blue
    Color(0xFFEA4335), // Red
    Color(0xFFFBBC05), // Yellow
    Color(0xFF34A853), // Green
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF607D8B), // Blue Grey
    Color(0xFFE91E63), // Pink
    Color(0xFF3F51B5)  // Indigo
)

@Composable
fun CategoryBreakdownChart(
    spending: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    if (spending.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses this month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Spending by Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                var startAngle = -90f
                spending.forEachIndexed { index, item ->
                    val sweepAngle = item.percentage * 360f
                    drawArc(
                        color = ChartColors[index % ChartColors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 40.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Legend
        spending.forEachIndexed { index, item ->
            CategoryLegendItem(
                category = item.category,
                amount = item.amount,
                percentage = item.percentage,
                color = ChartColors[index % ChartColors.size]
            )
        }
    }
}

@Composable
private fun CategoryLegendItem(
    category: String,
    amount: Double,
    percentage: Float,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%.1f%%".format(percentage * 100),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            text = "$%.2f".format(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
