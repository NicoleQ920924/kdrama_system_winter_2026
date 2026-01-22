*** 本README.md檔主要為此系統設計專案的使用說明。
*** This README.md file contains the introduction, as well as a guide of how to use this system.

# 專案介紹 / System Introduction
1. 這個專案是韓國影視管理系統，專為台灣人打造，並實現多影音平台整合。
2. 本專案的前端使用Vue.js，後端使用Spring Boot + MySQL資料庫。
3. 此專案解決台灣韓劇和韓影迷需要反覆在網路上搜尋「韓劇、韓影、韓國演員，以及在台灣有播放的平台」之困擾。
4. 本專案中，使用者可以透過新增演員，直接一鍵透過TMDB API爬取該演員的作品之資料，包含有播出該作品的台灣平台等實用資訊。
5. 使用者也可以前往韓劇或韓影直接新增出演演員的資訊。
6. 使用者也可以新增單獨的韓劇和韓影，或更新及刪除資料。
7. 系統執行中的情況下，台灣本土平台資訊在每天的3:00、6:00、9:00、...、21:00、24:00更新，以確保為最新資訊。
8. 設計者建議在使用此系統中同時聆聽韓劇OST，視覺和聽覺雙重享受。
9. 本專案是學習專案，為設計者的備審資料之一，僅利用一個暑假完成，若有設計不足之處，如未如期完成的登入、註冊、追蹤清單等功能，敬請見諒。

# 專案結構 / System Structure
kdrama_system_summer_2025/   <-- 專案根目錄
├── backend/                 <-- Spring Boot 原始碼
│   ├── src/main/java/...    <-- Java 程式碼
│   ├── src/main/resources/... <-- application.properties.example、其他資源檔
│   ├── pom.xml              <-- Maven 配置
│   └── ...                  <-- 其他 Spring Boot 專案檔案
│
├── frontend/                <-- Vue.js 原始碼
│   ├── src/                 <-- Vue 組件、頁面
│   ├── public/              <-- 靜態資源
│   ├── package.json
│   └── ...                  <-- 其他 Vue.js 專案檔案
│
├── .gitignore               <-- 忽略 build、node_modules、資料庫備份等
├── README.md                <-- 專案說明
└── LICENSE (optional)       <-- 如果你想附加開源授權

# 軟體要求 / Software Requirements
後端：Java JDK (17或21)、MySQL Server、Maven
前端：Node.js、支援JavaScript的瀏覽器
爬取資料：TMDB API Key

# 後端、MySQL 資料庫、TMDB API Key 設定方式 / Backend, MySQL and API Key Configuration
1. 在Terminal執行 cp application.properties.example application.properties 以複製.example範例檔並建立          application.properties
2. 開啟 application.properties 並根據註解的提示編輯各欄位，設定自己的MySQL資料庫和帳密，以及TMDB API Key
3. 在Terminal執行cd backend，再執行./mvnw spring-boot:run 或者透過IDE執行Application.java檔案，以執行專案和確定有設定成功

# 後端 Build / Spring Boot Jar
1. 在Terminal執行cd backend，再執行./mvnw clean package
2. 生成的 jar 在 target/ 目錄，例如：backend-x.x.x-SNAPSHOT.jar
3. 透過執行java -jar target/backend-0.0.1-SNAPSHOT.jar來啟動。

# 前端編譯方式 / Frontend
1. 在Terminal執行cd frontend
2. 接著在Terminal執行npm install以安裝依賴
3. 接著在Terminal執行npm run dev
4. 打開瀏覽器訪問Terminal裡的http://localhost:5173

# 前端 Build / Production Build
1. 在Terminal執行cd frontend
2. 接著在Terminal執行npm install以安裝依賴
3. 接著在Terminal執行npm run build
4. 會生成檔案在frontend/dist/路徑，可放到靜態伺服器或整合到 Spring Boot 中。

## TMDB API 資料使用聲明 / Data Source
以下是TMDB API的聲明：
This project uses the TMDB API but is not endorsed or certified by TMDB.
TMDB的官方Logo在網站的footer顯示。

## 資料來源 / References
設計此專案時有參考的網路資源請參考references.md檔案。
For a detailed list of resources used or referenced in this project, please see [References.md](./References.md).

## 著作權 / Copyright

© 2025 Nicole Q. All rights reserved.

本專案程式碼與設計為作者 Nicole Q. 原創，僅供學習與展示用途，未經允許不得用於商業用途。
作者的GitHub專頁為 https://github.com/NicoleQ920924
有問題歡迎分享。