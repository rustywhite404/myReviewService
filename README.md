## 프로젝트 설정 및 실행 방법
유의사항 : 사전에 Docker와 Docker Compose가 설치되어 있어야 합니다.  

1. 프로젝트를 내려받아 주세요. 
   ```bash
   git clone https://github.com/rustywhite404/myReviewService.git
   cd myReviewService  
   
2. **Docker Compose로 MySQL 컨테이너 실행**  
프로젝트의 docker-compose.yml 파일이 위치한 경로에서 아래 명령어를 입력해주세요. 
   ```bash
   docker-compose up --build -d

3. DB 스키마 myReviewService는 컨테이너가 시작될 때 자동으로 생성됩니다.   
 
4. 애플리케이션은 http://localhost:8080에서 실행됩니다. 

---

