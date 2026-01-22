<!-- Imported in DramaPage.vue -->
<script setup>
  import Spinner from './Spinner.vue'
  import { defineEmits, onMounted, ref } from 'vue'

  import { findDramas, deleteSelectedDrama } from '@/services/dramaService';

  const emit = defineEmits(['select-drama', 'reset-drama'])

  const dramas = ref([])
  const loading = ref(true)

  onMounted(() =>
    loadDramas()
  )

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
        console.error('刪除韓劇失敗:', err);
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

    <transition name="fade" mode="out-in">
      <div>
        <!-- Loading Animation -->
        <div v-if="loading" key="loading">
          <Spinner />
        </div>

        <!-- Finish Loading -->
        <table v-else-if="dramas.length > 0" key="table" class="table table-striped table-bordered">
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
            <tr v-for="drama in dramas" :key="drama.dramaId">
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

        <!-- No Data Found -->
        <p v-else key="nodata">⚠目前無法載入韓劇列表</p>

        <div v-if="!loading" class="text-center bottom-text">
              <router-link class="input-router-link" :to="{ name: 'ImportDramaPage', query: {} }" replace>點我新增韓劇資料</router-link>
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

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>