# BGW-MOBILE: 모바일 앱 실습

<p align="center">
  안녕하세요! <strong>Kotlin</strong>과 <strong>Jetpack Compose</strong>를 활용한<br>
  <strong>모던 안드로이드 앱 개발</strong> 포트폴리오 저장소입니다.
</p>

<br>

<h2 align="center">주요 프로젝트</h2>

<table align="center">
  <tr>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/w03">
        <img src="UIimgs/start.png?raw=true" width="100%" alt="Exynos Intro UI"/>
      </a>
      <br>
      <strong>엑시노스 칩 소개</strong>
      <br>
      <small>간단한 소개 화면</small>
    </td>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/w04">
        <img src="UIimgs/profile+keypad.png?raw=true" width="100%" alt="Dialer App UI"/>
      </a>
      <br>
      <strong>전화 다이얼 앱</strong>
      <br>
      <small>Jetpack Compose 다이얼 UI</small>
    </td>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/w05">
        <img src="UIimgs/stopwatch.png?raw=true" width="100%" alt="Stopwatch App"/>
      </a>
      <br>
      <strong>스톱워치</strong>
      <br>
      <small>시간 및 랩 타임 측정</small>
    </td>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/w05_buble_game">
        <img src="UIimgs/bubble.gif?raw=true" width="100%" alt="Bubble Game"/>
      </a>
      <br>
      <strong>미니 게임</strong>
      <br>
      <small>버블 터치 게임</small>
    </td>
  </tr>
  
  <tr>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/w06">
        <img src="UIimgs/messageloggif.gif?raw=true" width="100%" alt="Message Log UI"/>
      </a>
      <br>
      <strong>메세지 로그</strong>
      <br>
      <small>Compose 기반 메세지 UI</small>
    </td>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/wop-calender-p">
        <img src="UIimgs/캘린더pt1.png?raw=true" width="100%" alt="Calendar Prototype 1"/>
      </a>
      <br>
      <strong>동적 캘린더 (Ver.1)</strong>
      <br>
      <small>기초 캘린더 UI 프로토타입</small>
    </td>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/wop-calender-p">
        <img src="UIimgs/캘린더pt2.png?raw=true" width="100%" alt="Calendar Prototype 2"/>
      </a>
      <br>
      <strong>동적 캘린더 (Ver.2)</strong>
      <br>
      <small>일정 추가 및 상세 보기 로직</small>
    </td>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/wop-calender-p">
        <img src="UIimgs/캘린더pt3.png?raw=true" width="100%" alt="Calendar Prototype 3"/>
      </a>
      <br>
      <strong>동적 캘린더 (Ver.3)</strong>
      <br>
      <small>Room DB, MVVM, Hilt 적용</small>
    </td>
  </tr>

  <tr>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/wop-calender-p">
        <img src="UIimgs\캘린더pt4.png?raw=true" width="100%" alt="Calendar Prototype 4"/>
      </a>
      <br>
      <strong>동적 캘린더 (Ver.4)</strong>
      <br>
      <small>Gemini 1.5,UI redesign</small>
    </td>
    <td align="center" width="25%">
      <a href="https://github.com/BGW55/BGW-MOBILE/tree/main/app/wop-calender-p">
        <img src="UIimgs\캘린더pt5.png?raw=true" width="100%" alt="Calendar Prototype 5"/>
      </a>
      <br>
      <strong>동적 캘린더 (Ver.5)</strong>
      <br>
      <small>UI redesign,working</small>
    </td>
    <td align="center" width="25%">
      <a href="#">
        <img src="https://via.placeholder.com/150x300.png?text=New+Project" width="100%" alt="Project 11"/>
      </a>
      <br>
      <strong>새 프로젝트 11</strong>
      <br>
      <small>프로젝트 설명</small>
    </td>
    <td align="center" width="25%">
      <a href="#">
        <img src="https://via.placeholder.com/150x300.png?text=New+Project" width="100%" alt="Project 12"/>
      </a>
      <br>
      <strong>새 프로젝트 12</strong>
      <br>
      <small>프로젝트 설명</small>
    </td>
  </tr>
</table>

<br>

<h3 align="center">프로젝트 상세 및 학습 내용</h3>

<br>

#### 동적 캘린더 앱 (Agent Calendar Project)
`app/wop-calender-p`
안드로이드의 핵심 아키텍처와 최신 기술을 단계별로 적용하며 고도화한 주력 프로젝트입니다.

* **Ver.1 & 2 (UI & Logic):** `Jetpack Compose`의 `LazyVerticalGrid`를 활용해 달력 뷰를 직접 구현하고, Custom Dialog를 통해 일정 추가/삭제 기능을 개발했습니다.
* **Ver.3 (Architecture):** **MVVM 패턴**을 도입하여 UI와 데이터 로직을 분리했습니다. `Room Database`를 연동하여 앱 종료 후에도 데이터가 유지되도록 했으며, `Hilt`를 사용해 의존성 주입을 처리했습니다.
* **Ver.4 (AI Integration):** **Google Gemini 1.5 flash API**를 활용한 'AI 일정 비서' 기능을 탑재했습니다. 자연어로 일정을 입력하면 AI가 날짜와 시간을 분석해 자동으로 등록해 줍니다.
* **Ver.5 (Optimization):** 전체적인 UI/UX를 다듬고, 불필요한 리컴포지션을 방지하여 앱 성능을 최적화했습니다.

<br>

#### 리듬 게임 (Rhythm Game Project)
`app/wop-rhythmgame`
터치 이벤트 처리와 오디오 싱크를 맞추는 정교한 로직을 구현한 프로젝트입니다.(개발중지)

* **Audio Handling:** `MediaPlayer` 또는 `ExoPlayer`를 활용하여 고음질 음원(`seishunsubliminal.flac`) 재생 및 정지 기능을 구현했습니다.
* **Sync Logic:** 음악의 비트에 맞춰 노트가 떨어지도록 타이밍 알고리즘을 설계하고, 사용자의 터치 정확도(Perfect, Good, Miss)를 판정하는 로직을 개발했습니다.
* **Animation:** `Animatable` API를 사용하여 부드러운 노트 낙하 애니메이션과 타격 이펙트를 구현했습니다.

<br>

#### 안드로이드 기초 실습 (Android Basics)
`app/w03` ~ `app/w06`
매주 다양한 UI 컴포넌트와 상태 관리 기법을 익히기 위해 진행한 미니 프로젝트들입니다.

* **w06 메세지 로그:** `LazyColumn`과 `Card` 컴포넌트를 사용하여 효율적인 채팅 리스트 UI를 구성했습니다.
* **w05 스톱워치 & 미니게임:** `LaunchedEffect`와 `StateFlow`를 사용하여 시간의 흐름을 제어하고, 실시간으로 변하는 UI 상태를 관리했습니다.
* **w04 전화 다이얼:** 복잡한 버튼 그리드 레이아웃을 `Column`과 `Row` 조합으로 깔끔하게 구현했습니다.
* **w03 엑시노스 소개:** `Image` 컴포넌트와 스크롤 뷰를 활용하여 정보 전달을 위한 정적 페이지를 만들었습니다.

<hr>

<h3 align="center">사용 기술</h3> 

<p align="center">
  <strong>Languages</strong><br>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white">
</p>

<p align="center">
  <strong>Android Ecosystem</strong><br>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">
  <img src="https://img.shields.io/badge/Coroutines-3E85C5?style=for-the-badge&logo=kotlin&logoColor=white">
</p>

<br>
