### GITHUB 사용 설명서

배건우
Copyright from 202511057

***

### 설치 및 최초 설정

- GIT을 명령줄에서 사용하기 위해 Git을 다운로드하여 설치한다.
- https://desktop.github.com/download/
- 다운로드 후 CMD에서 다음을 입력해 버전 정보가 나오면 성공: `git --version`
- CMD를 관리자 권한으로 열지 말 것.
- 본인의 영문 이름으로 설정: `git config --global user.name "Your Name"`
- GitHub에 가입한 이메일로 설정: `git config --global user.email "your.email@example.com"`

***

### 프로젝트 시작하기: 두 가지 시나리오

- 웹사이트 접속 후 회원 가입을 하고, 오른쪽 상단 +를 눌러 새로운 repository를 생성할 수 있다.
- 프로젝트를 시작하는 방법은 크게 두 가지이다.


#### 1) 깃허브에 이미 있는 프로젝트를 내 컴퓨터로 가져올 때 (Clone)

- 각자의 GitHub에서 가져올 프로젝트 페이지로 이동 후 초록색 Code 버튼을 누르고 HTTPS 주소를 복사한다.
- CMD에서 프로젝트를 저장하고 싶은 폴더로 이동: `cd [경로]` (예: `cd C:\MyProjects`)
- 복사한 깃허브 주소로 클론: `git clone [주소]` (예: `git clone https://github.com/user/project.git`)

주의: GitHub 등에서 받아온 프로젝트 폴더 안에서는 절대 `git clone`을 다시 실행하면 안 된다. `git clone`은 저장소 본체(.git)까지 통째로 복사해 오기 때문에 초기화(`git init`)이 이미 완료된 상태이다.

#### 2) 내 컴퓨터에서 새 프로젝트를 만들어 깃허브에 올릴 때 (Init)

- GitHub 웹사이트에서 비어있는 새 repository 생성.
- CMD에서 프로젝트를 저장할 폴더로 이동: `cd [경로]` (예: `cd C:\MyProjects\project`)
- 현재 폴더를 Git 저장소로 초기화(최초 1회): `git init`
- 원격 연결(origin 별칭): `git remote add origin [주소]`
- 연결 확인: `git remote -v`

***

### 일상적인 작업 흐름 (cd, add, commit, push, pull)

- Clone 또는 Init 중 하나로 저장소 설정이 완료되었다면 이후 작업 흐름은 동일하다.


#### 변경 내용 업로드 흐름 (cd → add → commit → push)

- 프로젝트 이동: `cd [경로]` (예: `cd C:\MyProjects`)
- 변경된 모든 파일 선택: `git add .`
- 변경 내용 저장(메시지 작성): `git commit -m "변경 내용"`
- 업로드(브랜치 이름 확인 후 사용):
    - `git push origin main`
    - `git push origin master`
    - `git push -u origin main`
    - `git push -u origin master`

Q. `git push -u origin main/master`가 뭐가 달라요?
A. `-u` 옵션은 `set-upstream`의 줄임말로 현재 로컬 브랜치(main)를 원격 저장소(origin)의 브랜치에 연결해 추적한다. 다음부터는 `git push`나 `git pull`만 입력해도 되게끔 설정한다.

- 간혹 push 전에 pull을 하라는 경우가 발생한다. 브랜치 이름에 맞게 다음 중 하나 사용:
    - `git pull origin main`
    - `git pull origin master`

주의: pull 시 팀원이 수정한 부분과 겹치면 충돌(Conflict)이 발생할 수 있다. 이는 오류가 아니라, 어떤 코드를 남길지 사용자가 결정해야 함을 알려주는 정상적인 과정이다. 충돌을 해결한 뒤 커밋하면 된다.

- 모든 것을 마친 뒤 GitHub에서 새로고침(F5)하여 커밋 내용과 파일을 확인한다.
- Code 탭의 Codespaces를 클릭한 뒤 웹에서 파일들을 수정할 수도 있다.

***

### 기타 오류 해결과 명령어

#### 로컬 커밋 취소하기 (Push 전)

- 방금 한 커밋 자체를 수정: `git commit --amend`
- Soft: 커밋만 취소, 파일 변경 내용은 남김: `git reset --soft HEAD~1`
- Hard: 커밋과 파일 변경 내용 모두 취소(복구 안 됨, 주의): `git reset --hard HEAD~1`
- `HEAD~1`은 마지막 1개 커밋, `HEAD~2`는 2개를 의미.


#### 특정 커밋을 되돌리는 새 커밋 생성 (Revert)[^1]

- `git revert [커밋해시]` (예: `git revert a1b2c3d`)


#### 파일 상태 되돌리기

- 스테이징된 파일을 다시 내리기: `git restore --staged [파일명]`
예: `git restore --staged config.js`
- 작업 중인 파일 변경사항 버리기(복구 안 됨, 주의): `git restore [파일명]


#### 삭제 복구 (브랜치)

1) 모든 활동 기록 확인: `git reflog`
2) 삭제된 브랜치의 마지막 커밋 해시를 찾는다(reflog에 'checkout: moving from [브랜치] to...' 기록 확인).[^1]
3) 해당 해시로 새 브랜치 생성: `git checkout -b [새브랜치] a1b2c3d`

#### 원격 저장소 문제 해결
- 현재 연결된 원격 주소 확인: `git remote -v`
- 원격 주소 변경(이름은 보통 origin): `git remote set-url origin [새 주소]`


#### 강제 푸시 관련

- reset 등으로 로컬 히스토리를 바꾼 뒤 push 시 "rejected" 오류가 나면 강제로 밀어 올려야 할 수 있다.
- `git push --force`는 팀원의 커밋을 덮어쓸 수 있어 매우 위험하므로 쓰지 말 것. 작성자는 이 명령어 사용 후 되돌리는 일을 겪었다.
- 더 안전한 강제 푸시: `git push --force-with-lease`는 그 사이 팀원이 다른 커밋을 올렸다면 내 push를 거부하여 사고를 막아준다.


#### `git commit -am "메시지"`의 의미

- Git이 이미 추적 중인 파일들을 수정하거나 삭제했을 때, 변경 사항을 한 번에 스테이징하고 커밋까지 하는 단축키 같은 명령어.
- 옵션 결합: `-a`(수정/삭제된 트래킹 파일 자동 스테이징) + `-m`(메시지 인라인 작성).
- 주의: `-a`는 새로 생성된 Untracked 파일을 스테이징하지 않는다.

***

### git rebase 요약

- `git rebase`는 이미 커밋한 히스토리를 깔끔하게 재정렬하는 강력하고 위험한 명령어다. 브랜치의 시작점(Base)을 다시 배치(Re)한다는 뜻.


#### 언제 쓰나?

- 내 브랜치를 최신 버전으로 업데이트할 때 흔히 사용. 예: main에서 feature 브랜치를 만들었는데 그 사이 main이 업데이트된 경우.
- Merge는 'Merge main into feature' 같은 병합 커밋이 남아 히스토리가 지저분해질 수 있다.
- Rebase는 feature의 커밋들을 통째로 들어 최신 main 끝에 다시 쌓아, 불필요한 병합 커밋 없이 히스토리를 한 줄로 정리한다.


#### 사용 예

1) 브랜치 이동: `git checkout feature`[^1]
2) 기준 브랜치로 재배치 실행: `git rebase main`[^1]

#### 절대 주의(황금률)

- rebase는 기존 커밋을 지우고 새로운 커밋으로 복제해 히스토리를 다시 쓴다.
- 절대로 main, master처럼 여러 사람이 함께 쓰는 공용 브랜치에서는 rebase를 사용하면 안 된다.
- rebase는 혼자 쓰는 로컬 브랜치를 main에 합치기 전에 깔끔하게 정리할 용도로만 사용. 이미 깃허브에 올린 커밋을 rebase하면 팀원 전체 저장소가 꼬일 수 있다.

***

### 최후의 보루: git reflog

- `git reflog`는 커밋, 브랜치 이동, reset 등 거의 모든 행동을 기록한다.
- 실수로 `reset --hard`를 하거나 브랜치를 날려도 `git reflog`에서 과거 커밋 해시(hash)를 찾을 수 있다.
- 해시만 있으면 `git checkout`이나 `git reset`으로 대부분 복구할 수 있다.

원하시면 .md 파일로 저장해서 내려받을 수 있게 만들어 드릴게요. 파일명과 줄바꿈 스타일(Unix LF/Windows CRLF)을 알려주면 반영하겠습니다.

<div align="center"></div>

