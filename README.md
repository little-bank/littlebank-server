# 리틀뱅크(littlebank)
<hr>
부모와 함께하는 아이들의 학습 동기 부여 어플

# 개발환경
<hr>

- SpringBoot 3.3.10
- Java 17
- MySQL

# 작업방식
<hr>
*브랜치 전략 : Git-Flow

**초반 세팅 (최초 1번)**

1. 루트 레포지토리를 자신의 레포지토리로 fork한다.
2. fork한 레포지토리를 자신의 로컬 디렉토리에 clone한다.
3. 루트 레포지토리를 upstream 저장소로 설정한다. 
4. fork한 레포지토리를 origin 저장소로 설정한다.

**반복 작업**

1. 루트 레포지토리에서 이슈를 생성한다. ex) 회원가입 기능 구현
2. 로컬에서 'git fetch upstream' 으로 upstream 저장소의 변동된 사항을 가져온다.
3. upstream의 develop 브랜치로부터 feature 브랜치를 생성한다. ex) feature/#001/회원가입_기능_구현
4. 기능 구현 후 origin 저장소로 push를 하고 루트 레포지토리의 develop 브랜치로 PR(Pull Request)을 요청한다.

**저장소 주인의 역할**

1. 요청 온 PR을 확인 후 develop 브랜치에 merge한다.
