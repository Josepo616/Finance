package josealvarez.personal.finance.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import josealvarez.personal.finance.R

sealed class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    object Dashboard : NavigationItem("Dashboard", Icons.Default.Home, "dashboard")
    object Budget : NavigationItem("Budget Limits", Icons.Default.AccountBalanceWallet, "budget")
    object Expenses : NavigationItem("Expenses", Icons.Default.Receipt, "expenses")
    object Income : NavigationItem("Income", Icons.Default.AttachMoney, "income")
    object Categories : NavigationItem("Categories", Icons.Default.Category, "categories")
}

@Composable
fun AppDrawer(
    userName: String,
    userEmail: String,
    onLogoutClick: () -> Unit,
    onNavigate: (NavigationItem) -> Unit,
    selectedItem: NavigationItem? = null
) {
    ModalDrawerSheet {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = userEmail, fontSize = 14.sp, color = Color.Gray)
        }
        
        HorizontalDivider()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val items = listOf(
            NavigationItem.Dashboard,
            NavigationItem.Budget,
            NavigationItem.Expenses,
            NavigationItem.Income,
            NavigationItem.Categories
        )
        
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                selected = item == selectedItem,
                onClick = { onNavigate(item) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        HorizontalDivider()
        
        NavigationDrawerItem(
            label = { Text("Logout") },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
            selected = false,
            onClick = onLogoutClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
