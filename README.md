## 프로젝트 설정 및 실행 방법
유의사항 : 사전에 Docker와 Docker Compose가 설치되어 있어야 합니다.  

1. 프로젝트를 내려받아 주세요. 
   ```bash
   git clone https://github.com/rustywhite404/myReviewService.git
   cd myReviewService  
   
2. 루트 경로에 `.env` 파일을 생성하여 DB 접속정보 등의 민감한 정보를 설정해주세요.  
설정해야 하는 항목은 아래와 같습니다.  
   ```env
   DB_USERNAME={ROOT USERNAME}
   DB_PASSWORD={ROOT PASSWORD}
   MYSQL_ROOT_PASSWORD={ROOT PASSWORD}

3. **Docker Compose로 MySQL 컨테이너 실행**  
프로젝트의 docker-compose.yml 파일이 위치한 경로에서 아래 명령어를 입력해주세요. 
   ```bash
   docker-compose up --build -d

3. DB 스키마 myReviewService는 컨테이너가 시작될 때 자동으로 생성됩니다.   
 
4. 애플리케이션은 http://localhost:8080 에서 실행됩니다. 

---

