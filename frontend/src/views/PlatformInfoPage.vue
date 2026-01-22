<!-- The Page to Display via 影音平台整理 on the Navigation Bar -->
<script setup>

  const platforms = [
    { name: "Netflix", country: "美國", url: "https://www.netflix.com", class: "netflix"},
    { name: "Disney Plus", country: "美國", url: "https://www.disneyplus.com", class: "disney-plus"},
    { name: "Amazon Prime Video", country: "美國", url: "https://www.primevideo.com", class: "prime-video"},
    { name: "Apple TV", country: "美國", url: "https://tv.apple.com", class: "apple-tv"},
    { name: "HBO Max", country: "美國", url: "https://www.hbomax.com/tw", class: "hbo-max"},
    { name: "中華電信Hami Video", country: "台灣", url: "https://hamivideo.hinet.net", class: "hami-video"},
    { name: "遠傳friDay影音", country: "台灣", url: "https://video.friday.tw", class: "friday-video"},
    { name: "MyVideo", country: "台灣", url: "https://www.myvideo.net.tw", class: "myvideo"},
    { name: "LINE TV", country: "台灣", url: "https://www.linetv.tw", class: "line-tv"},
    { name: "Catchplay", country: "台灣", url: "https://www.catchplay.com", class: "catch-play"}

  ]
  
</script>
<template>
  <div>
    <h2>影音平台整理</h2>
    <h4>(僅包含本系統有讀取的影音平台)</h4>

    `<table key="table" class="table table-striped table-bordered text-center">
      <thead>
        <tr>
          <td width="60%">平台名稱 (點擊即可前往首頁)</td>
          <td width="40%">國家</td>
        </tr>
      </thead>
      <tbody>
        <tr v-for="platform in platforms" :key="platform.name">
          <td class="text-center"><h5><a :class="platform.class" :href=platform.url>{{ platform.name }}</a></h5></td>
          <td class="text-center"><h5 class="country">{{ platform.country }}</h5></td>
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
  margin:0px 15% 5% 15%;
  width:70%;
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
h5 {
  font-weight:normal;
}

/* Platform Classes */
.netflix {
    color:#E50914;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:#E50914;
    }
}
.disney-plus {
    color:teal;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:teal;
    }
}
.prime-video {
    color:#1399FF;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:#1399FF;
    }
}
.apple-tv {
    color:darkgray;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:darkgray;
    }
}
.hbo-max {
    color:gray;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:gray;
    }
}
.hami-video {
    color:greenyellow;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:greenyellow;
    }
}
.friday-video {
    color:aqua;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:aqua;
    }
}
.myvideo {
    color:orange;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:orange;
    }
}
.line-tv {
    color:#06C755;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:#06C755;
    }
}
.catch-play {
    color:$autumn-dark-orange;
    font-weight:bold;

    &:hover,
    &:active,
    &:focus {
        color:$autumn-dark-orange;
    }
}

</style>