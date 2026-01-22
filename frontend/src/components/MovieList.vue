<!-- Imported in MoviePage.vue -->
<script setup>
  import Spinner from './Spinner.vue'
  import { defineEmits, onMounted, ref } from 'vue'

  import { findMovies, deleteSelectedMovie } from '@/services/movieService';

  const emit = defineEmits(['select-movie', 'reset-movie'])

  const movies = ref([])
  const loading = ref(true)

  onMounted(() =>
    loadMovies()
  )

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
        console.error('刪除韓影失敗:', err);
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
    <h4>請自行用Ctrl+F搜尋</h4>

    <transition name="fade" mode="out-in">
      <div>
        <!-- Loading Animation -->
        <div v-if="loading" key="loading">
          <Spinner />
        </div>

        <!-- Finish Loading -->
        <table v-else-if="movies.length > 0" key="table" class="table table-striped table-bordered">
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
            <tr v-for="movie in movies" :key="movie.movieId">
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

        <!-- No Data Found -->
        <p v-else key="nodata">⚠目前無法載入韓影列表</p>

        <div v-if="!loading" class="text-center bottom-text">
              <router-link class="input-router-link" :to="{ name: 'ImportMoviePage', query: {} }" replace>點我新增韓影資料</router-link>
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
  margin:0px 5%;
  width:90%;
  height:90%;
}
thead td {
  text-align:center;
  background-color:$autumn-light-orange;
  opacity:0.6;
  font-weight:bold;
  vertical-align:middle;
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

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>