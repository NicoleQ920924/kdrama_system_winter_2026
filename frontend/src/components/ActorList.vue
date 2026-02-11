<!-- Imported in ActorPage.vue -->
<script setup>
  import Spinner from './Spinner.vue'
  import { defineEmits, onMounted, ref, computed } from 'vue'
  import { deleteSelectedActor, findActors } from '@/services/actorService';
  import { userStore } from '@/store'

  const emit = defineEmits(['select-actor', 'reset-actor'])

  const actors = ref([])
  const loading = ref(true)
  const sortBy = ref('name')

  const user = computed(() => userStore.getCurrentUser())

  onMounted(() =>
    loadActors()
  )

  const adminNotLoggedIn = computed(() => {
    return !user.value || user.value.role === 'USER';
  });

  const sortItems = () => {
      if (sortBy.value === 'name') {
        actors.value.sort((a, b) => a.chineseName.localeCompare(b.chineseName, 'zh-Hant-TW-u-co-zhuyin'))
      } else if (sortBy.value === 'birthday') {
        actors.value.sort((a, b) => new Date(b.birthday) - new Date(a.birthday))
      }
      else if (sortBy.value === 'gender') {
        actors.value.sort((a, b) => a.actorGender.localeCompare(b.actorGender, 'zh-Hant-TW-u-co-zhuyin'))
      }
  }

  const groupedActors = computed(() => {
    const groups = []; 
    const data = actors.value

    for (let i = 0; i < data.length; i += 4) {
      groups.push(data.slice(i, i + 4));
    }
    return groups;
  })

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
        if (err.response && err.response.status === 403 || err.response && err.response.status === 401) {
            alert(`您沒有權限刪除此演員，請以管理員登入後重試!`)
        } else {
            console.error('刪除演員失敗:', err);
            alert(`刪除演員時發生錯誤，請稍後重試!`)
        }
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
    <h4>可以用Ctrl+F搜尋</h4>
    <div class="text-center controls-section">
        <label for="sortBy" class="form-label">排序方式:</label>
        <select v-model="sortBy" @change="sortItems" id="sortBy" class="form-select">
            <option value="name">中文譯名 (按注音排序)</option>
            <option value="birthday">生日 (由年輕至年長)</option>
            <option value="gender">性別 (男前女後)</option>
        </select>
    </div>

    <transition name="fade" mode="out-in">
      <div>
        <!-- Loading Animation -->
        <div v-if="loading" key="loading">
          <Spinner />
        </div>

        <!-- Finish Loading -->
        <table v-else-if="actors.length > 0 && user != null && user.role === 'ADMIN'" key="actor-admin-table" class="table table-striped table-bordered actor-admin-table">
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

        <table v-else-if="actors.length > 0 && adminNotLoggedIn == true" key="actor-user-table" class="table table-borderless text-center actor-user-table">
            <tr v-for="(row, rowIndex) in groupedActors" :key="rowIndex" class="text-center">
                <td v-for="actor in row" :key="actor.actorId">
                    <img height="160px" :src="actor.profilePicUrl" alt="No Image">
                    <h5 class="actor-name"><router-link :to="{ name: 'ActorPage', query: { id: actor.actorId } }" replace class="actor-user-router-link">{{ actor.chineseName }}</router-link></h5>
                </td>
            </tr>
        </table>

        <!-- No Data Found -->
        <p v-else key="nodata">⚠目前無法載入演員列表</p>

        <div v-if="!loading" class="text-center bottom-text">
            <router-link v-if="user != null && user.role === 'ADMIN'" class="input-router-link" :to="{ name: 'ImportActorPage', query: {} }" replace>點我新增演員資料</router-link>
        </div>
        <div v-if="!loading" class="text-center bottom-text">
            <router-link class="input-router-link" :to="{ name: 'AiImportActorPage', query: {} }" replace>點我用 AI 依角色搜尋演員</router-link>
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
    width:50%;
    text-align:center;
    margin:0% 25%;
}
.actor-admin-table {
  text-align:center;
  border:1px solid $autumn-dark-brown;
  margin:3% 5%;
  width:90%;
  height:90%;
}
.actor-admin-table thead td {
  text-align:center;
  background-color:$autumn-light-orange;
  opacity:0.6;
  font-weight:bold;
}
.actor-admin-table tbody tr {
  height:80px;
}
.actor-admin-table tbody td {
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
.actor-user-table {
    padding:10px;
    width:98%;
    margin-top:2%;
}
.actor-user-table td {
    height:200px;
    width:25%;
}
.actor-name {
    margin-top:5px;
    color:$autumn-dark-orange;
}
.actor-user-router-link:link, .actor-user-router-link:visited {
    font-weight:bold;
    text-decoration:none;
    color:$autumn-dark-orange;
}
.actor-user-router-link:hover {
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