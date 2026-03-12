package josealvarez.personal.finance.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols

@Composable
fun MonthSelector(
    month: Int, // 1-based: 1 for January, 12 for December
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isNextEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatSymbols().months[month - 1]
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month"
            )
        }
        
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        IconButton(
            onClick = onNext,
            enabled = isNextEnabled
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Month"
            )
        }
    }
}
