package com.example.adp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adp.ui.theme.ADPTheme
import android.content.res.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ADPTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileCard(Profile("Bae Geon Woo", "Hello there this is 15th testing."))
        Keypad()
    }
}

data class Profile(val name: String, val intro: String)

@Composable
fun ProfileCard(data: Profile) {
    Row(
        modifier = Modifier.padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.rni1),
            contentDescription = "ni1",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = data.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.intro,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun Keypad() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonModifier = Modifier
            .padding(8.dp)
            .size(80.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("1") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("2") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("3") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("4") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("5") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("6") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("7") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("8") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("9") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("*") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("0") }
            Button(onClick = { /*TODO*/ }, modifier = buttonModifier) { Text("#") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { /*TODO*/ }) { Text("전화걸기") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /*TODO*/ }) { Text("키패드") }
            Button(onClick = { /*TODO*/ }) { Text("최근기록") }
            Button(onClick = { /*TODO*/ }) { Text("연락처") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileCardPreview() {
    ADPTheme {
        ProfileCard(Profile("Bae Geon Woo", "Hello there this is 15th testing."))
    }
}

@Preview(showBackground = true)
@Composable
fun KeypadPreview() {
    ADPTheme {
        Keypad()
    }
}
@Preview(showBackground = true, backgroundColor = 0xFF000000) // Preview 배경 검정색으로
@Composable
fun HomeScreenPreview() {
    ADPTheme {
        HomeScreen()
    }
}