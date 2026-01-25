<!-- The Page to Display via 影音平台整理 on the Navigation Bar -->
<script setup>
  import { computed, onMounted, ref } from 'vue'
  import Spinner from '@/components/Spinner.vue'
  import { findDramas } from '@/services/dramaService'
  import { findMovies } from '@/services/movieService'

  const platforms = [
    // NOTE:
    // Backend stores platform keys from TWOTT caches using `platformName.split("-")[0]`
    // e.g. "friDay影音-drama" -> key saved in DB is "friDay影音"
    { key: "Netflix", name: "Netflix", country: "美國", url: "https://www.netflix.com", class: "netflix"},
    { key: "Disney Plus", name: "Disney Plus", country: "美國", url: "https://www.disneyplus.com", class: "disney-plus"},
    { key: "Amazon Prime Video", name: "Amazon Prime Video", country: "美國", url: "https://www.primevideo.com", class: "prime-video"},
    { key: "Apple TV", name: "Apple TV", country: "美國", url: "https://tv.apple.com", class: "apple-tv"},
    { key: "HBO Max", name: "HBO Max", country: "美國", url: "https://www.hbomax.com/tw", class: "hbo-max"},
    { key: "中華電信Hami Video", name: "中華電信Hami Video", country: "台灣", url: "https://hamivideo.hinet.net", class: "hami-video"},
    // Display name can be "遠傳friDay影音", but the DB key is "friDay影音"
    { key: "friDay影音", name: "遠傳friDay影音", country: "台灣", url: "https://video.friday.tw", class: "friday-video"},
    { key: "MyVideo", name: "MyVideo", country: "台灣", url: "https://www.myvideo.net.tw", class: "myvideo"},
    { key: "LINE TV", name: "LINE TV", country: "台灣", url: "https://www.linetv.tw", class: "line-tv"},
    { key: "Catchplay", name: "Catchplay", country: "台灣", url: "https://www.catchplay.com", class: "catch-play"}

  ]

  const loading = ref(false)
  const dramas = ref([])
  const movies = ref([])
  const errorMsg = ref('')

  function normalizePlatformMap(mapLike) {
    // Backend should return object map, but guard against null/[].
    if (!mapLike) return {}
    if (Array.isArray(mapLike)) return {}
    if (typeof mapLike !== 'object') return {}
    return mapLike
  }

  function getPlatformUrlFromMap(mapLike, platformName) {
    const m = normalizePlatformMap(mapLike)
    return m?.[platformName] ?? ''
  }

  const platformWorks = computed(() => {
    return platforms.map(p => {
      const dramasOnPlatform = (dramas.value ?? []).filter(d => !!getPlatformUrlFromMap(d?.dramaTwPlatformMap, p.key))
      const moviesOnPlatform = (movies.value ?? []).filter(m => !!getPlatformUrlFromMap(m?.movieTwPlatformMap, p.key))
      return {
        platform: p,
        dramas: dramasOnPlatform,
        movies: moviesOnPlatform
      }
    })
  })

  async function loadPlatformWorks() {
    loading.value = true
    errorMsg.value = ''
    try {
      const [dramasRes, moviesRes] = await Promise.all([
        findDramas(false), // displayNameMode=false: keep enum raw values if any
        findMovies()
      ])

      dramas.value = Array.isArray(dramasRes?.data) ? dramasRes.data : []
      movies.value = Array.isArray(moviesRes?.data) ? moviesRes.data : []
    } catch (err) {
      console.error(err)
      errorMsg.value = '⚠目前無法載入平台上架作品（請確認後端已啟動）'
      dramas.value = []
      movies.value = []
    } finally {
      loading.value = false
    }
  }

  onMounted(() => loadPlatformWorks())
  
</script>
<template>
  <div>
    <h2>影音平台整理</h2>

    <table key="table" class="table table-striped table-bordered text-center">
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

    <h4>(每個影音平台分別有上架且有存在資料庫的作品)</h4>

    <transition name="fade" mode="out-in">
      <div>
        <div v-if="loading" key="loading">
          <Spinner />
        </div>

        <div v-else key="works">
          <p v-if="errorMsg" class="error-text text-center">{{ errorMsg }}</p>

          <div v-else class="platform-works-wrap">
            <details v-for="item in platformWorks" :key="item.platform.name" class="platform-details">
              <summary class="platform-summary">
                <span :class="item.platform.class">{{ item.platform.name }}</span>
                <span class="summary-count">
                  （韓劇 {{ item.dramas.length }} 部、韓影 {{ item.movies.length }} 部）
                </span>
              </summary>

              <div class="platform-works-body">
                <div class="works-col">
                  <h5 class="works-title">韓劇</h5>
                  <ul v-if="item.dramas.length" class="works-list">
                    <li v-for="d in item.dramas" :key="`${d.dramaId}-${d.seasonNumber ?? ''}`" class="works-item">
                      <router-link class="works-router-link" :to="{ name: 'DramaPage', query: { id: d.dramaId } }">
                        {{ d.chineseName }}{{ d.seasonNumber && d.seasonNumber > 1 ? ` (第${d.seasonNumber}季)` : '' }}
                      </router-link>
                      <span v-if="getPlatformUrlFromMap(d.dramaTwPlatformMap, item.platform.key)" class="works-sep">・</span>
                      <a
                        v-if="getPlatformUrlFromMap(d.dramaTwPlatformMap, item.platform.key)"
                        class="works-platform-link"
                        :href="getPlatformUrlFromMap(d.dramaTwPlatformMap, item.platform.key)"
                        target="_blank"
                        rel="noopener noreferrer"
                      >播放頁</a>
                    </li>
                  </ul>
                  <div v-else class="no-works">尚無資料</div>
                </div>

                <div class="works-col">
                  <h5 class="works-title">韓影</h5>
                  <ul v-if="item.movies.length" class="works-list">
                    <li v-for="m in item.movies" :key="m.movieId" class="works-item">
                      <router-link class="works-router-link" :to="{ name: 'MoviePage', query: { id: m.movieId } }">
                        {{ m.chineseName }}
                      </router-link>
                      <span v-if="getPlatformUrlFromMap(m.movieTwPlatformMap, item.platform.key)" class="works-sep">・</span>
                      <a
                        v-if="getPlatformUrlFromMap(m.movieTwPlatformMap, item.platform.key)"
                        class="works-platform-link"
                        :href="getPlatformUrlFromMap(m.movieTwPlatformMap, item.platform.key)"
                        target="_blank"
                        rel="noopener noreferrer"
                      >播放頁</a>
                    </li>
                  </ul>
                  <div v-else class="no-works">尚無資料</div>
                </div>
              </div>
            </details>
          </div>
        </div>
      </div>
    </transition>

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

.platform-works-wrap {
  margin: 0px 10% 5% 10%;
}
.platform-details {
  border: 1px solid $autumn-light;
  border-radius: 6px;
  padding: 10px 12px;
  margin: 12px 0px;
  background: rgba(255, 255, 255, 0.45);
}
.platform-summary {
  cursor: pointer;
  font-weight: bold;
  color: $autumn-text-dark;
}
.summary-count {
  font-weight: normal;
  color: $autumn-dark-brown;
  margin-left: 8px;
}
.platform-works-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  margin-top: 10px;
}
.works-title {
  margin: 8px 0px 6px 0px;
  color: $autumn-dark-yellow;
}
.works-list {
  margin: 0px;
  padding-left: 18px;
}
.works-item {
  margin: 6px 0px;
  color: $autumn-text-dark;
}
.works-router-link {
  color: $autumn-dark-orange;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
    color: $autumn-dark-orange;
  }
}
.works-platform-link {
  color: $autumn-dark-brown;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
    color: $autumn-dark-brown;
  }
}
.works-sep {
  margin: 0px 6px;
  color: $autumn-text-dark;
}
.no-works {
  color: $autumn-red;
  margin: 6px 0px 0px 0px;
}
.error-text {
  margin-top: 30px;
  color: $autumn-red;
  font-size: larger;
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

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

</style>