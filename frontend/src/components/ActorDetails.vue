<!-- Imported in ActorPage.vue -->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { findSelectedActor } from '@/services/actorService'
import Spinner from './Spinner.vue'

const props = defineProps({
  selectedActorId: String
})

const emit = defineEmits(['reset-actor'])

const actor = ref({})
const loading = ref(true)

const groupedDramas = computed(() => {
  const groups = [];
  const dramas = actor.value.dramas || [];
  for (let i = 0; i < dramas.length; i += 4) {
    groups.push(dramas.slice(i, i + 4));
  }
  return groups;
})

const groupedMovies = computed(() => {
  const groups = [];
  const movies = actor.value.movies || [];
  for (let i = 0; i < movies.length; i += 4) {
    groups.push(movies.slice(i, i + 4));
  }
  return groups;
})

function loadActor() {
  loading.value = true;
  findSelectedActor(props.selectedActorId)
    .then(res => {
      actor.value = res.data;
    })
    .catch(err => console.error(err))
    .finally(() => loading.value = false)
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

const displayData = (data) => {
  if (data === null || data === undefined || data === '') return '無資料';
  if (Array.isArray(data) && data.length === 0) return '無資料';
  return data;
}

function backToActorList() {
    emit('reset-actor')
}

onMounted(() => loadActor())
</script>


<template>
    <div>
        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="loading" key="loading">
                <Spinner />
            </div>

            <!-- Finish Loading -->
            <div v-else-if="Object.keys(actor).length > 0" class="actor-details">
                <table key="actor-table" class="table table-borderless actor-table">
                    <tr>
                        <!-- Left: Profile Picture; Right: Other Information -->
                        <td>
                            <img height="420px" :src="actor.profilePicUrl">
                        </td>
                        <td>
                            <h4><span>韓國{{ actor.actorGender }}演員</span></h4>
                            <h3>{{ actor.chineseName }}</h3>
                            <h3><a :href="actor.namuWikiPageUrl" class="namu-wiki-link">{{ actor.koreanName }}</a></h3>
                            <h3>{{ actor.englishName }}</h3>
                            <h4>出生年月日：{{ formatDate(actor.birthday) }}</h4>
                        </td>
                    </tr>
                </table>
                <h3 class="actor-biography-table-caption text-center">{{ actor.chineseName }} 的介紹</h3>
                <hr class="text-center">
                <table key="actor-biography-table" class="table table-borderless text-center actor-biography-table">
                    <tr class="text-center">
                        <td>
                            <h6 :class="actor.biography == null || actor.biography == '' ? 'text-center' : 'text-start'">{{ displayData(actor.biography) }}</h6>
                        </td>
                    </tr>
                </table>
                <h3 class="actor-works-table-caption text-center">{{ actor.chineseName }} 出演的韓劇 (以TMDB為準)</h3>
                <hr class="text-center">
                <table key="actor-works-table" class="table table-borderless text-center actor-works-table">
                    <tr v-for="(row, rowIndex) in groupedDramas" :key="rowIndex" class="text-center">
                        <td v-for="drama in row" :key="drama.dramaId">
                            <img height="160px" :src="drama.mainPosterUrl" alt="No Image">
                            <h5 class="work-name"><router-link :to="{ name: 'DramaPage', query: { id: drama.dramaId } }" replace class="work-router-link">{{ drama.chineseName }}</router-link></h5>
                        </td>
                    </tr>
                </table>
                <h3 class="actor-works-table-caption text-center">{{ actor.chineseName }} 出演的韓影 (以TMDB為準)</h3>
                <hr class="text-center">
                <table key="actor-works-table" class="table table-borderless text-center actor-works-table">
                    <tr v-for="(row, rowIndex) in groupedMovies" :key="rowIndex" class="text-center">
                        <td v-for="movie in row" :key="movie.movieId">
                            <img height="160px" :src="movie.mainPosterUrl" alt="無圖">
                            <h5 class="work-name"><router-link :to="{ name: 'MoviePage', query: { id: movie.movieId } }" class="work-router-link">{{ movie.chineseName }}</router-link></h5>
                        </td>
                    </tr>
                </table>
                <h5 class="text-center"><router-link :to="{ name: 'UpdateActorPage', query: { id: actor.actorId } }" class="actor-router-link text-center">點我編輯此專頁</router-link></h5>
                <div class="text-center">
                    <button class="btn back-btn text-center" @click="backToActorList">返回演員列表</button>
                </div>
            </div>

            <!-- No Data Found -->
            <div v-else key="nodata">⚠目前沒有演員資料
                <p>⚠目前沒有演員資料</p>
            </div>

        </transition>
    </div>
</template>

<style lang="scss" scoped>
.actor-table {
    text-align:center;
    margin:3% 5%;
    width:90%;
    height:90%;
}
.actor-works-table {
    padding:10px;
    width:100%;
    margin-top:2%;
}
.actor-biography-table {
    padding:20px;
    width:100%;
    margin-top:2%;
}
.actor-works-table td {
    height:200px;
    width:25%;
}
.actor-biography-table td h6 {
    padding:5px;
    margin:2% 5%;
    color:$autumn-dark-yellow;
}
span {
    background-color:$autumn-light-orange;
    color:$autumn-dark-brown;
}
h3, h4 {
    text-align:start;
    color:$autumn-text-dark;
    padding:5px 0px;
    margin-left:20px;
    font-weight:normal;
}
.actor-works-table-caption {
    text-align:center;
    color:$autumn-dark-brown;
    margin-top:5px;
}
.work-name {
    margin-top:5px;
    color:$autumn-dark-orange;
}
p {
    margin-top:50px;
    color:$autumn-red;
    font-size:larger;
    text-align:center;
}
hr {
    color:$autumn-dark-orange;
    width:auto;
}
.back-btn {
    background-color:$autumn-light-orange;
    color:$autumn-light;
    margin:15px 0px;

    &:hover,
    &:active,
    &:focus {
        background-color:$autumn-light-orange;
        color:$autumn-light;
    }
}
.work-router-link:link, .work-router-link:visited {
    font-weight:bold;
    text-decoration:none;
    color:$autumn-dark-orange;
}
.work-router-link:hover {
    font-weight:bold;
    text-decoration:underline;
    color:$autumn-dark-orange;
}
.actor-router-link {
  color:$autumn-dark-orange;
  padding-top:5px;
}
.namu-wiki-link {
    text-align:start;
    color:$autumn-text-dark;
    padding:5px 0px;
    font-weight:normal;
    text-decoration:underline;
    
    &:hover,
    &:active,
    &:focus {
        color:$autumn-text-dark;
        text-decoration:underline;
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