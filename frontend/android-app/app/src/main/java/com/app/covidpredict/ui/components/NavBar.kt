package com.app.covidpredict.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.covidpredict.R
import com.app.covidpredict.theme.CovidPredictTheme

@Composable
fun NavBar(
    currentRoute: String = "dashboard",
    onNavigate: (String) -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val navItems = listOf(
            Triple("dashboard", R.drawable.beranda, "Beranda"),
            Triple("data", R.drawable.data, "Data"),
            Triple("prediksi", R.drawable.prediksi, "Prediksi"),
            Triple("grafik", R.drawable.grafik, "Grafik")
        )

        navItems.forEach { (route, iconRes, label) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavBarPreview() {
    CovidPredictTheme {
        NavBar()
    }
}
