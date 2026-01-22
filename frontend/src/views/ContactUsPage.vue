<!-- The Page to Display via 聯絡我們 on the Navigation Bar -->
<script setup>

  const links = [
    { name: "設計者的GitHub專頁", info: "設計者的GitHub專頁，點擊連結即可查看設計者的基本資料，並進一步查看本專案的內容", url: "https://github.com/NicoleQ920924"},
    { name: "設計者的部落格", info: "設計者的部落格，有興趣者歡迎點擊連結並閱讀各種貼文", url: "https://nicoleq920924.wordpress.com/"},
  ]
  
</script>
<template>
  <div>
    <h2>聯絡我們</h2>

    `<table key="table" class="table table-striped table-bordered text-center">
      <thead>
        <tr>
          <td>網頁名稱</td>
          <td width="60%">說明</td>
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
h2 {
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