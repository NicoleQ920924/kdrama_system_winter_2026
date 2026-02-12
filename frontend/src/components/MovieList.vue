<!-- Imported in MoviePage.vue -->
<script setup>
  import Spinner from './Spinner.vue'
  import { defineEmits, onMounted, ref, computed } from 'vue'

  import { findMovies, deleteSelectedMovie } from '@/services/movieService';
  import { userStore } from '@/store'

  const emit = defineEmits(['select-movie', 'reset-movie'])

  const movies = ref([])
  const loading = ref(true)
  const sortBy = ref('name')
  const selectedGenre = ref('全選 All')

  const user = computed(() => userStore.getCurrentUser())

  onMounted(() =>
    loadMovies()
  )

  const genres = [
    { key: "all", name: "全選 All"},
    { key: "action", name: "動作 Action"},
    { key: "adventure", name: "冒險 Adventure"},
    { key: "animation", name: "動畫 Animation"},
    { key: "comedy", name: "喜劇 Comedy"},
    { key: "crime", name: "犯罪 Crime"},
    { key: "documentary", name: "紀錄片 Documentary"},
    { key: "drama", name: "劇情 Drama"},
    { key: "family", name: "家庭 Family"},
    { key: "fantasy", name: "奇幻 Fantasy"},
    { key: "history", name: "歷史 History"},
    { key: "horror", name: "恐怖 Horror"},
    { key: "music", name: "音樂 Music"},
    { key: "mystery", name: "懸疑 Mystery"},
    { key: "romance", name: "愛情 Romance"},
    { key: "scifi", name: "科幻 Science Fiction"},
    { key: "thriller", name: "驚悚 Thriller"},
    { key: "tvmovie", name: "電視電影 TV Movie"},
    { key: "war", name: "戰爭 War"}
  ]

  const adminNotLoggedIn = computed(() => {
    return !user.value || user.value.role === 'USER';
  });

  const sortItems = () => {
      if (sortBy.value === 'name') {
        movies.value.sort((a, b) => a.chineseName.localeCompare(b.chineseName, 'zh-Hant-TW-u-co-zhuyin'))
      } else if (sortBy.value === 'release-date') {
        movies.value.sort((a, b) => new Date(b.releaseDate) - new Date(a.releaseDate))
      }
      else if (sortBy.value === 'runtime') {
        movies.value.sort((a, b) => b.totalRuntime - a.totalRuntime)
      }
  }

  const groupedMovies = computed(() => {
    const groups = []; 
    const data = filteredMovies.value

    for (let i = 0; i < data.length; i += 4) {
      groups.push(data.slice(i, i + 4));
    }
    return groups;
  })

  const filteredMovies = computed(() => {
    const selectedGenreChineseName = selectedGenre.value.split(" ")[0];
    if (selectedGenreChineseName === '全選') {
      return movies.value;
    }
    return movies.value.filter(movie => movie.genres.includes(selectedGenreChineseName));
  })

  function loadMovies() {
    loading.value = true;
    findMovies()
      .then(res => {
        movies.value = res.data;
      })
      .catch(err => {
        console.error('載入韓影失敗:', err);
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function startDeleteMovie(movieId) {
    loading.value = true;
    deleteSelectedMovie(movieId)
      .then(res => {
          console.log(res.status);
          if (res.status === 200) {
            alert(`刪除成功！`)
            emit('reset-movie')
            loadMovies()
          }
        })
      .catch(err => {
        if (err.response && err.response.status === 403 || err.response && err.response.status === 401) {
            alert(`您沒有權限刪除此韓影，請以管理員登入後重試!`)
        } else {
            console.error('刪除韓影失敗:', err);
            alert(`刪除韓影時發生錯誤，請稍後重試!`)
        }
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function goToMoviePage (movieId) {
    emit('select-movie', movieId)
  }

  function formatDate(dateStr) {
    if (!dateStr) return '';
    var [year, month, day] = dateStr.split('-');
    if (month.charAt(0) == '0') {
        month = month.slice(1)
    }
    if (day.charAt(0) == '0') {
        day = day.slice(1)
    }
    return `${year}年${month}月${day}日`;
  }
</script>
<template>
  <div>
    <h2>韓影列表</h2>
    <h4>可以用Ctrl+F搜尋</h4>
    <div class="controls-section mb-4">
      <div class="row">
          <div class="col-md-6">
              <label for="selectedGenre" class="form-label">篩選韓影：</label>
              <select v-model="selectedGenre" id="selectedGenre" class="form-select">
                  <ul v-for="genre in genres" :key="genre.key">
                    <option :value="genre.name">{{ genre.name }}</option>
                  </ul>
              </select>
          </div>
          <div class="col-md-6">
              <label for="sortBy" class="form-label">排序方式：</label>
              <select v-model="sortBy" @change="sortItems" id="sortBy" class="form-select">
                  <option value="name">片名 (按注音排序)</option>
                  <option value="release-date">上映日期 (由新至舊)</option>
                  <option value="runtime">片長 (由長至短)</option>
              </select>
          </div>
      </div>
    </div>

    <transition name="fade" mode="out-in">
      <div>
        <!-- Loading Animation -->
        <div v-if="loading" key="loading">
          <Spinner />
        </div>

        <!-- Finish Loading -->
        <table v-else-if="filteredMovies.length > 0 && user != null && user.role === 'ADMIN'" key="movie-admin-table" class="table table-striped table-bordered movie-admin-table">
          <thead>
            <tr>
              <td width="20%">海報照片</td>
              <td>片名</td>
              <td>上映日期</td>
              <td>片長 (分鐘)</td>
              <td>操作連結</td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="movie in filteredMovies" :key="movie.movieId">
              <td><img height="80px" :src="movie.mainPosterUrl" alt="無圖"></td>
              <td>
                <h5>{{ movie.chineseName }}</h5>
                <h5>{{ movie.koreanName }}</h5>
                <h5>{{ movie.englishName }}</h5>
              </td>
              <td><h5>{{ movie.releaseDate != null && movie.releaseDate != '' ? formatDate(movie.releaseDate) : '無資料' }}</h5></td>
              <td><h5>{{ movie.totalRuntime != null && movie.totalRuntime != '' && movie.totalRuntime != '0' ? movie.totalRuntime : '無資料' }}</h5></td>
              <td>
                <h5><a href="#" @click.prevent="goToMoviePage(movie.movieId)">韓影專頁</a></h5>
                <h5><router-link :to="{ name: 'UpdateMoviePage', query: { id: movie.movieId } }" class="movie-router-link">點我編輯</router-link></h5>
                <h5><a href="#" @click.prevent="startDeleteMovie(movie.movieId)">點我刪除</a></h5>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else-if="filteredMovies.length > 0 && adminNotLoggedIn == true" key="movie-user-table" class="table table-borderless text-center movie-user-table">
            <tr v-for="(row, rowIndex) in groupedMovies" :key="rowIndex" class="text-center">
                <td v-for="movie in row" :key="movie.movieId">
                    <img height="160px" :src="movie.mainPosterUrl" alt="No Image">
                    <h5 class="movie-name"><router-link :to="{ name: 'MoviePage', query: { id: movie.movieId } }" replace class="movie-user-router-link">{{ movie.chineseName }}</router-link></h5>
                </td>
            </tr>
        </table>

        <!-- No Data Found -->
        <p v-else key="nodata">⚠目前無法載入韓影列表</p>

        <div v-if="!loading" class="text-center bottom-text">
              <router-link class="input-router-link" v-if="user != null && user.role === 'ADMIN'" :to="{ name: 'ImportMoviePage', query: {} }" replace>點我新增韓影資料</router-link>
        </div>
        <div v-if="!loading" class="text-center bottom-text">
              <router-link class="input-router-link" :to="{ name: 'AiImportMoviePage', query: {} }" replace>點我用 AI 依條件搜尋韓影</router-link>
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
.controls-section {
    background: #f9f7f4;
    padding: 15px;
    border-radius: 8px;
}
.movie-admin-table {
  text-align:center;
  border:1px solid $autumn-dark-brown;
  margin:3% 5%;
  width:90%;
  height:90%;
}
.movie-admin-table thead td {
  text-align:center;
  background-color:$autumn-light-orange;
  opacity:0.6;
  font-weight:bold;
  vertical-align:middle;
}
.movie-admin-table tbody tr {
  height:80px;
}
.movie-admin-table tbody td {
  text-align:center;
  vertical-align:middle;
  color:$autumn-text-dark;
  height:80px;
}
h5 {
  font-weight:normal;
}
p {
  margin-top:50px;
  color:$autumn-red;
  font-size:larger;
  text-align:center;
}
a, .movie-router-link {
  color:$autumn-dark-orange;
  padding-top:5px;
}
.bottom-text {
  margin:20px 0px;
}
.input-router-link:link, .input-router-link:visited {
  font-weight:bold;
  font-size:larger;
  color:$autumn-dark-brown;
  text-decoration:none;
  margin:10px 5px;
}
.input-router-link:hover {
  font-weight:bold;
  font-size:larger;
  color:$autumn-dark-brown;
  text-decoration:underline;
  margin:10px 5px;
}
.movie-user-table {
    padding:10px;
    width:98%;
    margin-top:2%;
}
.movie-user-table td {
    height:200px;
    width:25%;
}
.movie-name {
    margin-top:5px;
    color:$autumn-dark-orange;
}
.movie-user-router-link:link, .movie-user-router-link:visited {
    font-weight:bold;
    text-decoration:none;
    color:$autumn-dark-orange;
}
.movie-user-router-link:hover {
    font-weight:bold;
    text-decoration:underline;
    color:$autumn-dark-orange;
}

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>