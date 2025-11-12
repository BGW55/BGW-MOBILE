package com.example.w06

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.w06.ui.theme.ADPTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADPTheme {
                // 기존 Scaffold 대신 MessageLogScreen을 바로 호출합니다.
                // MessageLogScreen 내부에 자체 Scaffold가 포함되어 있습니다.
                MessageLogScreen()
            }
        }
    }
}

// Pager와 Material3의 'Experimental' API 사용을 알림
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MessageLogScreen() {

    // 1. 상태(State) 관리
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope() // 드로어를 열고 닫을 때 사용
    val tabTitles = listOf("전체 로그", "SMS", "알림")
    val pagerState = rememberPagerState { tabTitles.size }

    // 2. ModalNavigationDrawer (DrawerLayout 대체)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 드로어 메뉴 내용
            ModalDrawerSheet {
                Text("메뉴", modifier = Modifier.padding(16.dp))
                Spacer(Modifier.height(10.dp))
                NavigationDrawerItem(
                    label = { Text("설정") },
                    selected = false,
                    onClick = { /* 설정 클릭 시 동작 */ }
                )
                NavigationDrawerItem(
                    label = { Text("정보") },
                    selected = false,
                    onClick = { /* 정보 클릭 시 동작 */ }
                )
            }
        }
    ) {
        // 3. Scaffold (메인 화면 구조)
        Scaffold(
            topBar = {
                // 상단 앱 바
                TopAppBar(
                    title = { Text("메시지 로그 뷰어") },
                    navigationIcon = {
                        // 햄버거 메뉴 아이콘
                        IconButton(onClick = {
                            // 아이콘 클릭 시 드로어 열기
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "메뉴")
                        }
                    }
                )
            }
        ) { paddingValues ->
            // 4. Pager (ViewPager2 대체)
            Column(modifier = Modifier.padding(paddingValues)) {

                // 4-1. 탭 메뉴 (TabLayout 역할)
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                // 탭 클릭 시 해당 페이지로 스크롤
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }

                // 4-2. 페이저 본문 (HorizontalPager)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    // 각 페이지에 표시될 내용
                    // (실제로는 여기에 리스트(LazyColumn)가 들어갑니다)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tabTitles[pageIndex]} 내용",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// 기존 GreetingPreview 대신 새로운 Preview
@Preview(showBackground = true)
@Composable
fun MessageLogScreenPreview() {
    ADPTheme {
        MessageLogScreen()
    }
}