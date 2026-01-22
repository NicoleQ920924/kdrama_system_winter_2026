<!-- The Page to Display via 外部連結 on the Navigation Bar -->
<script setup>

  const links = [
    { name: "MyDramaList", info: "本系統的設計概念有參考此現有的線上平台", url: "https://mydramalist.com/"},
    { name: "TMDB (The Movie Database) API", info: "本系統主要的API資料來源，所有透過TMDB API爬到的資料都歸TMDB擁有 (資料庫中，海報圖片、演員大頭照等只存取URL超連結)", url: "https://developer.themoviedb.org/docs/"},
    { name: "Spring Boot", info: "本系統使用的後端框架", url: "https://spring.io/projects/spring-boot"},
    { name: "Vue.js", info: "本系統使用的前端框架", url: "https://vuejs.org/"},
    { name: "Web Driver Manager", info: "本系統爬台灣影音平台資訊所使用的Chrome Driver 來源", url: "https://github.com/bonigarcia/webdrivermanager"},
    { name: "Axios", info: "本系統所使用的HTTP請求函式庫", url: "https://axios-http.com/docs/intro"},
    { name: "OpenCC", info: "本系統所使用的繁簡轉換函式庫", url: "https://github.com/BYVoid/OpenCC"},
    { name: "Figma", info:"本系統UI的初步設計", url: "https://www.figma.com/design/2QaUITWlMZ5K7iSIpjDvBw/Kdrama-System?node-id=0-1&p=f&t=8fH5c6Fol75TyrFe-0"},
  ]
  
</script>
<template>
  <div>
    <h2>外部連結</h2>
    <h4>(僅擷取部分網頁，詳情請見聯絡我們->GitHub->本專案->references.md)</h4>

    `<table key="table" class="table table-striped table-bordered text-center">
      <thead>
        <tr>
          <td>網頁名稱</td>
          <td width="50%">說明</td>
          <td>連結</td>
        </tr>
      </thead>
      <tbody>
        <tr v-for="link in links" :key="link.name">
          <td class="text-center"><h5 class="link-name">{{ link.name }}</h5></td>
          <td class="text-center"><h5 class="link-description">{{ link.info }}</h5></td>
          <td class="text-center"><h5><a :href="link.url">點我</a></h5></td>
        </tr>
      </tbody>
    </table>

  </div>
</template>

<style lang="scss" scoped>
h2, h4 {
  margin:30px 0px;
  color:$autumn-dark-yellow;
  text-align:center;
}
table {
  text-align:center;
  border:1px solid $autumn-dark-brown;
  margin:0px 10% 5% 10%;
  width:80%;
  height:90%;
}
thead td {
  text-align:center;
  background-color:$autumn-light-orange;
  opacity:0.6;
  font-weight:bold;
}
tbody tr {
  height:80px;
}
tbody td {
  text-align:center;
  vertical-align:middle;
  color:$autumn-text-dark;
  height:80px;
}
.link-name {
    font-weight:bold;
    color:$autumn-dark-brown;
}
// -info is already registered by Bootstrap
.link-description {
    font-weight:normal;
    text-align:start;
    color:$autumn-text-dark;
    padding:5px;
}
a {
    color:$autumn-dark-orange;

    &:hover,
    &:active,
    &:focus {
        color:$autumn-dark-orange;
    }
}

</style>