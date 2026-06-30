package josealvarez.personal.finance.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import josealvarez.personal.finance.model.BudgetComparison

@Composable
fun BudgetComparisonCard(
    comparisons: List<BudgetComparison>,
    modifier: Modifier = Modifier
) {
    if (comparisons.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Budget Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            comparisons.forEachIndexed { index, item ->
                BudgetProgressItem(item)
                if (index < comparisons.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (comparisons.all { it.limit == 0.0 }) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set your budget limits to track performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressItem(item: BudgetComparison) {
    val progressColor = when {
        item.percentage >= 1.0f -> Color(0xFFEA4335) // Red
        item.percentage >= 0.8f -> Color(0xFFFBBC05) // Yellow
        else -> Color(0xFF34A853) // Green
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.label} Limit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$%.2f / $%.2f".format(item.actual, item.limit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { item.percentage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "%.0f%%".format(item.percentage * 100),
                style = MaterialTheme.typography.labelSmall,
                color = if (item.percentage > 1f) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
