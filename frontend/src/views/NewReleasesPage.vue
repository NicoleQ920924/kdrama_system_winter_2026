<!-- The Page to Display via 新上架 on the Navigation Bar -->
<script setup>
  import Spinner from '@/components/Spinner.vue'
  import { onMounted, ref } from 'vue'

  import { findDramas } from '@/services/dramaService';
  import { findMovies } from '@/services/movieService';

  const dramas = ref([])
  const movies = ref([])
  const loading = ref(true)

  const currentYear = new Date().getFullYear().toString();
  const lastYear = (new Date().getFullYear() - 1).toString();

  onMounted(() =>
    loadDramas(),
    loadMovies()
  )

  function loadDramas() {
    loading.value = true;
    findDramas()
      .then(res => {
        dramas.value = res.data.filter(drama => drama.releaseYear === currentYear || drama.releaseYear === lastYear + "-" + currentYear);
      })
      .catch(err => {
        console.error('載入韓劇失敗:', err);
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function loadMovies() {
    loading.value = true;
    findMovies()
      .then(res => {
        movies.value = res.data.filter(movie => getYear(movie.releaseDate) === currentYear);
      })
      .catch(err => {
        console.error('載入韓影失敗:', err);
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function getYear(dateStr) {
    if (!dateStr) return '';
    var [year, month, day] = dateStr.split('-');
    return year;
  }
</script>
<template>
  <div>
    <h2>新上架作品</h2>

    <transition name="fade" mode="out-in">
      <div>
        <!-- Loading Animation -->
        <div v-if="loading" key="loading">
          <Spinner />
        </div>

        <!-- Finish Loading -->
        <h4 v-if="!loading && dramas.length > 0">新上架韓劇</h4>
        <table v-if="!loading && dramas.length > 0" key="table" class="table table-striped table-bordered">
          <thead>
            <tr>
              <td width="40%">海報照片</td>
              <td>作品名稱</td>
              <td>專頁</td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="drama in dramas" :key="drama.dramaId">
              <td><img height="240px" :src="drama.mainPosterUrl" alt="無圖"></td>
              <td>
                <h5>{{ drama.chineseName }}</h5>
                <h5>{{ drama.koreanName }}</h5>
                <h5>{{ drama.englishName }}</h5>
              </td>
              <td>
                <h5><router-link :to="{ name: 'DramaPage', query: { id: drama.dramaId } }" class="work-router-link">點我查看作品專頁</router-link></h5>
              </td>
            </tr>
          </tbody>
        </table>

        <h4 v-if="!loading && movies.length > 0">新上架韓影</h4>
        <table v-if="!loading && movies.length > 0" key="table" class="table table-striped table-bordered">
          <thead>
            <tr>
              <td width="40%">海報照片</td>
              <td>作品名稱</td>
              <td>專頁</td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="movie in movies" :key="movie.movieId">
              <td><img height="240px" :src="movie.mainPosterUrl" alt="無圖"></td>
              <td>
                <h5>{{ movie.chineseName }}</h5>
                <h5>{{ movie.koreanName }}</h5>
                <h5>{{ movie.englishName }}</h5>
              </td>
              <td>
                <h5><router-link :to="{ name: 'MoviePage', query: { id: movie.movieId } }" class="work-router-link">點我查看作品專頁</router-link></h5>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- No Data Found -->
        <p v-if="!loading && dramas.length == 0 && movies.length == 0" key="nodata">⚠目前無法載入作品列表</p>
      </div>
    </transition>
  </div>
</template>

<style lang="scss" scoped>
h2 {
  margin:30px 0px;
  color:$autumn-dark-yellow;
  text-align:center;
}
h4 {
  margin:30px 0px;
  color:$autumn-dark-brown;
  text-align:center;
}
table {
  text-align:center;
  border:1px solid $autumn-dark-brown;
  margin:2% 5%;
  width:90%;
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
p {
  margin-top:50px;
  color:$autumn-red;
  font-size:larger;
  text-align:center;
}
a, .work-router-link {
  color:$autumn-dark-orange;
  padding-top:5px;
}

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>