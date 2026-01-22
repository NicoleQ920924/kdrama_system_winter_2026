<!-- Imported in ActorPage.vue -->
<script setup>
  import Spinner from './Spinner.vue'
  import { defineEmits, onMounted, ref } from 'vue'

  import { deleteSelectedActor, findActors } from '@/services/actorService';

  const emit = defineEmits(['select-actor', 'reset-actor'])

  const actors = ref([])
  const loading = ref(true)

  onMounted(() =>
    loadActors()
  )

  function loadActors() {
    loading.value = true;
    findActors()
      .then(res => {
        actors.value = res.data;
      })
      .catch(err => {
        console.error('載入演員失敗:', err);
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function startDeleteActor(actorId) {
    loading.value = true;
    deleteSelectedActor(actorId)
      .then(res => {
          console.log(res.status);
          if (res.status === 200) {
            alert(`刪除成功！`)
            emit('reset-actor')
            loadActors()
          }
        })
      .catch(err => {
        console.error('刪除演員失敗:', err);
      })
      .finally(() => {
        loading.value = false;
      });
  }

  function goToActorPage (actorId) {
    emit('select-actor', actorId)
  }
</script>
<template>
  <div>
    <h2>演員列表</h2>
    <h4>請自行用Ctrl+F搜尋</h4>

    <transition name="fade" mode="out-in">
      <div>
        <!-- Loading Animation -->
        <div v-if="loading" key="loading">
          <Spinner />
        </div>

        <!-- Finish Loading -->
        <table v-else-if="actors.length > 0" key="table" class="table table-striped table-bordered">
          <thead>
            <tr>
              <td width="20%">照片</td>
              <td>中文譯名</td>
              <td>韓文名字</td>
              <td>性別</td>
              <td>操作連結</td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="actor in actors" :key="actor.actorId">
              <td><img height="80px" :src="actor.profilePicUrl"></td>
              <td><h5>{{ actor.chineseName }}</h5></td>
              <td><h5>{{ actor.koreanName }}</h5></td>
              <td><h5>{{ actor.actorGender }}</h5></td>
              <td>
                <h5><a href="#" @click.prevent="goToActorPage(actor.actorId)">演員專頁</a></h5>
                <h5><router-link :to="{ name: 'UpdateActorPage', query: { id: actor.actorId } }" class="actor-router-link">點我編輯</router-link></h5>
                <h5><a href="#" @click.prevent="startDeleteActor(actor.actorId)">點我刪除</a></h5>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- No Data Found -->
        <p v-else key="nodata">⚠目前無法載入演員列表</p>

        <div v-if="!loading" class="text-center bottom-text">
            <router-link class="input-router-link" :to="{ name: 'ImportActorPage', query: {} }" replace>點我新增演員資料</router-link>
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
a, .actor-router-link {
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