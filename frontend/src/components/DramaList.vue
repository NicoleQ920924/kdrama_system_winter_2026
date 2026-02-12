<!-- Imported in DramaPage.vue -->
<script setup>
  import Spinner from './Spinner.vue'
  import { defineEmits, onMounted, ref, computed } from 'vue'

  import { findDramas, deleteSelectedDrama } from '@/services/dramaService';
  import { userStore } from '@/store'

  const emit = defineEmits(['select-drama', 'reset-drama'])

  const dramas = ref([])
  const loading = ref(true)
  const sortBy = ref('name')
   const selectedGenre = ref('全選 All')

  const user = computed(() => userStore.getCurrentUser())

  onMounted(() =>
    loadDramas()
  )

  const genres = [
    { key: "all", name: "全選 All"},
    { key: "action-and-adventure", name: "動作冒險 Action & Adventure"},
    { key: "animation", name: "動畫 Animation"},
    { key: "comedy", name: "喜劇 Comedy"},
    { key: "crime", name: "犯罪 Crime"},
    { key: "documentary", name: "紀錄片 Documentary"},
    { key: "drama", name: "劇情 Drama"},
    { key: "family", name: "家庭 Family"},
    { key: "history", name: "歷史 History"},
    { key: "horror", name: "恐怖 Horror"},
    { key: "music", name: "音樂 Music"},
    { key: "mystery", name: "懸疑 Mystery"},
    { key: "romance", name: "愛情 Romance"},
    { key: "scifi-and-fantasy", name: "奇幻&科幻 Sci-Fi & Fantasy"},
    { key: "soap", name: "肥皂劇 Soap"},
    { key: "thriller", name: "驚悚 Thriller"},
    { key: "war-and-politics", name: "戰爭政治 War & Politics"}
  ]

  const adminNotLoggedIn = computed(() => {
    return !user.value || user.value.role === 'USER';
  });

  const sortItems = () => {
      if (sortBy.value === 'name') {
        dramas.value.sort((a, b) => a.chineseName.localeCompare(b.chineseName, 'zh-Hant-TW-u-co-zhuyin'))
      } else if (sortBy.value === 'release-year') {
        dramas.value.sort((b, a) => a.releaseYear.localeCompare(b.releaseYear))
      }
      else if (sortBy.value === 'status') {
        dramas.value.sort((a, b) => a.status.localeCompare(b.status, 'zh-Hant-TW-u-co-zhuyin'))
      }
  }

  const groupedDramas = computed(() => {
    const groups = []; 
    const data = filteredDramas.value

    for (let i = 0; i < data.length; i += 4) {
      groups.push(data.slice(i, i + 4));
    }
    return groups;
  })

  const filteredDramas = computed(() => {
    const selectedGenreChineseName = selectedGenre.value.split(" ")[0];
    if (selectedGenreChineseName === '全選') {
      return dramas.value;
    }
    return dramas.value.filter(drama => drama.genres.includes(selectedGenreChineseName));
  })

  function loadDramas() {
    loading.value = true;
    findDramas()
      .then(res => {
        dramas.value = res.data;
      })
      .catch(err => {
        console.error('載入韓劇失敗:', err);
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function startDeleteDrama(dramaId) {
    loading.value = true;
    deleteSelectedDrama(dramaId)
      .then(res => {
          console.log(res.status);
          if (res.status === 200) {
            alert(`刪除成功！`)
            emit('reset-drama')
            loadDramas()
          }
        })
      .catch(err => {
        if (err.response && err.response.status === 403 || err.response && err.response.status === 401) {
            alert(`您沒有權限刪除此韓劇，請以管理員登入後重試!`)
        } else {
            console.error('刪除韓劇失敗:', err);
            alert(`刪除韓劇時發生錯誤，請稍後重試!`)
        }
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function goToDramaPage (dramaId) {
    emit('select-drama', dramaId)
  }
</script>
<template>
  <div>
    <h2>韓劇列表</h2>
    <h4>請自行用Ctrl+F搜尋</h4>
    <div class="controls-section mb-4">
      <div class="row">
          <div class="col-md-6">
              <label for="selectedGenre" class="form-label">篩選韓劇：</label>
              <select v-model="selectedGenre" id="selectedGenre" class="form-select">
                  <ul v-for="genre in genres" :key="genre.key">
                    <option :value="genre.name">{{ genre.name }}</option>
                  </ul>
              </select>
          </div>
          <div class="col-md-6">
            <label for="sortBy" class="form-label">排序方式：</label>
            <select v-model="sortBy" @change="sortItems" id="sortBy" class="form-select">
              <option value="name">劇名 (按注音排序)</option>
              <option value="release-year">年度 (由新至舊)</option>
              <option value="status">狀態</option>
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
        <table v-else-if="filteredDramas.length > 0 && user != null && user.role === 'ADMIN'" key="drama-admin-table" class="table table-striped table-bordered drama-admin-table">
          <thead>
            <tr>
              <td width="20%">海報照片</td>
              <td>劇名</td>
              <td>年度</td>
              <td>狀態</td>
              <td>操作連結</td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="drama in filteredDramas" :key="drama.dramaId">
              <td><img height="80px" :src="drama.mainPosterUrl" alt="無圖"></td>
              <td>
                <h5>{{ drama.chineseName }}</h5>
                <h5>{{ drama.koreanName }}</h5>
                <h5>{{ drama.englishName }}</h5>
              </td>
              <td><h5>{{ drama.releaseYear }}</h5></td>
              <td><h5>{{ drama.status }}</h5></td>
              <td>
                <h5><a href="#" @click.prevent="goToDramaPage(drama.dramaId)">韓劇專頁</a></h5>
                <h5><router-link :to="{ name: 'UpdateDramaPage', query: { id: drama.dramaId } }" class="drama-router-link">點我編輯</router-link></h5>
                <h5><a href="#" @click.prevent="startDeleteDrama(drama.dramaId)">點我刪除</a></h5>
              </td>
            </tr>
          </tbody>
        </table>

        <table v-else-if="filteredDramas.length > 0 && adminNotLoggedIn == true" key="drama-user-table" class="table table-borderless text-center drama-user-table">
            <tr v-for="(row, rowIndex) in groupedDramas" :key="rowIndex" class="text-center">
                <td v-for="drama in row" :key="drama.dramaId">
                    <img height="160px" :src="drama.mainPosterUrl" alt="No Image">
                    <h5 class="drama-name"><router-link :to="{ name: 'DramaPage', query: { id: drama.dramaId } }" replace class="drama-user-router-link">{{ drama.chineseName }} ({{ drama.releaseYear }})</router-link></h5>
                </td>
            </tr>
        </table>

        <!-- No Data Found -->
        <p v-else key="nodata">⚠目前無法載入韓劇列表</p>

        <div v-if="!loading" class="text-center bottom-text">
              <router-link class="input-router-link" v-if="user != null && user.role === 'ADMIN'" :to="{ name: 'ImportDramaPage', query: {} }" replace>點我新增韓劇資料</router-link>
        </div>
        <div v-if="!loading" class="text-center bottom-text">
              <router-link class="input-router-link" :to="{ name: 'AiImportDramaPage', query: {} }" replace>點我用 AI 依條件搜尋韓劇</router-link>
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
.drama-admin-table {
  text-align:center;
  border:1px solid $autumn-dark-brown;
  margin:3% 5%;
  width:90%;
  height:90%;
}
.drama-admin-table thead td {
  text-align:center;
  background-color:$autumn-light-orange;
  opacity:0.6;
  font-weight:bold;
}
.drama-admin-table tbody tr {
  height:80px;
}
.drama-admin-table tbody td {
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
a, .drama-router-link {
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
.drama-user-table {
    padding:10px;
    width:98%;
    margin-top:2%;
}
.drama-user-table td {
    height:200px;
    width:25%;
}
.drama-name {
    margin-top:5px;
    color:$autumn-dark-orange;
}
.drama-user-router-link:link, .drama-user-router-link:visited {
    font-weight:bold;
    text-decoration:none;
    color:$autumn-dark-orange;
}
.drama-user-router-link:hover {
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