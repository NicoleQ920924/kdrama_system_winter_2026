<!-- Imported in MoviePage.vue -->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { findSelectedMovie } from '@/services/movieService'
import { importActor } from '@/services/actorService'
import Spinner from './Spinner.vue'

const props = defineProps({
  selectedMovieId: String
})

const emit = defineEmits(['reset-movie'])

const movie = ref({})
const loading = ref(true)

const loadingActors = ref([]) // for handleActorClick()

function loadMovie() {
  loading.value = true;
  findSelectedMovie(props.selectedMovieId)
    .then(res => {
      movie.value = res.data;
    })
    .catch(err => console.error(err))
    .finally(() => loading.value = false
    )
}

const groupedActors = computed(() => {
  const groups = [];
  const actors = movie.value.leadActors || [];
  for (let i = 0; i < actors.length; i += 5) {
    groups.push(actors.slice(i, i + 5));
  }
  return groups;
})

const directorNamesExist = computed(() => {
  const directorNames = movie.value.directorNames;
  if (directorNames.length > 0) {
    return true;
  }
  else {
    return false;
  }
})

const scriptwriterNamesExist = computed(() => {
  const scriptwriterNames = movie.value.scriptwriterNames;
  if (scriptwriterNames.length > 0) {
    return true;
  }
  else {
    return false;
  }
})

const displayData = (data) => {
  if (data === null || data === undefined || data === '') return '無資料';
  if (Array.isArray(data) && data.length === 0) return '無資料';
  return data;
}

function getAgeRestrictClass(age) {
    if (age == 12) {
        return "age-12"
    }
    else if (age == 15) {
        return "age-15"
    }
    else if (age == 19) {
        return "age-19"
    }
    else if (age == 0) {
        return "age-all"
    }
    else {
        return "age-others"
    }
}

function getPlatformClass(platform) {
    if (platform == "Netflix") {
        return "netflix"
    }
    else if (platform == "Disney Plus") {
        return "disney-plus"
    }
    else if (platform == "Amazon Prime Video") {
        return "prime-video"
    }
    else if (platform == "中華電信Hami Video") {
        return "hami-video"
    }
    else if (platform == "friDay影音") {
        return "friday-video"
    }
    else if (platform == "MyVideo") {
        return "myvideo"
    }
    else if (platform == "LINE TV") {
        return "line-tv"
    }
    else if (platform == "Apple TV") {
        return "apple-tv"
    }
    else if (platform == "HBO Max") {
        return "hbo-max"
    }
    else if (platform == "Catchplay") {
        return "catch-play"
    }
    else {
        return "other-platforms"
    }
}

function backToMovieList() {
    emit('reset-movie')
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

async function handleActorClick(actorName) { // By ChatGPT
    // Return while loading
    if (loadingActors.value.includes(actorName)) return;

    // Add loading status
    loadingActors.value.push(actorName)

    try {
        const res = await importActor(actorName)
        console.log(res.status)
        if (res.status === 200) {
            alert(`${actorName} 已成功加入資料庫！`)
        }
    } catch (err) {
        console.error(err)
        if (err.response && err.response.status === 409) {
            alert(`${actorName} 已存在資料庫中`)
        } else {
            alert(`加入 ${actorName} 時發生錯誤`)
        }
    } finally {
        // Remove loading status
        const index = loadingActors.value.indexOf(actorName)
        if (index !== -1) loadingActors.value.splice(index, 1)
    }
}

onMounted(() => loadMovie())
</script>


<template>
    <div>
        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="loading" key="loading">
                <Spinner />
            </div>

            <!-- Finish Loading -->
            <div v-else-if="Object.keys(movie).length > 0" class="movie-details">
                <table key="movie-table" class="table table-borderless movie-table">
                    <tr>
                        <!-- Left: Poster; Right: Other Information -->
                        <td>
                            <img height="480px" :src="movie.mainPosterUrl" alt="無圖">
                            <br>
                            <a v-if="movie.trailerUrl != null && movie.trailerUrl != undefined && movie.trailerUrl != ''" :href="movie.trailerUrl" class="trailer-url">點我觀賞預告片</a>
                        </td>
                        <td>
                            <h3 class="movie-info">{{ movie.chineseName }}</h3> 
                            <h4 class="movie-info"><a :href="movie.namuWikiPageUrl" class="namu-wiki-link">{{ movie.koreanName }}</a></h4>
                            <h4 class="movie-info">{{ movie.englishName }}</h4>
                            <div>
                                <ul>
                                    <li v-for="genre in movie.genres" :key="genre" class="genres">{{ displayData(genre) }}</li>
                                </ul>
                            </div>
                            <br><br>
                            <p class="movie-detailed-info">韓國上映日期：{{ displayData(formatDate(movie.releaseDate)) }} <span :class="getAgeRestrictClass(movie.krAgeRestriction)">{{ movie.krAgeRestriction === 0 ? '普遍級' : (movie.krAgeRestriction || '無年齡資料') }}</span></p>
                            <p class="movie-detailed-info">片長 {{ displayData((movie.totalRuntime)) }} 分鐘</p>
                            <p class="movie-detailed-info">
                                <button class="btn wish-btn">加入追蹤清單</button>
                            </p>
                            <div class="platforms">
                                <h5 class="platform-section-title">台灣播出平台</h5>
                                <ul v-if="Object.keys(movie.movieTwPlatformMap).length > 0">
                                    <li v-for="(value, key) in movie.movieTwPlatformMap" :key="key">
                                    <!-- key = platform_name, value = url -->
                                        <a :class="getPlatformClass(key)" :href="value">{{ key }}</a>
                                    </li>
                                </ul>
                                <h6 v-else class="movie-detailed-info">無資料</h6>
                            </div>
                        </td>
                    </tr>
                </table>
                <h3 class="movie-actors-table-caption text-center">{{ movie.chineseName }} 的主演演員</h3>
                <h4 class="movie-actors-table-caption text-center">點擊演員的連結即可將演員和其作品加入資料庫</h4>
                <hr class="text-center">
                <table key="movie-actors-table" class="table table-borderless text-center movie-actors-table">
                    <tr v-for="(row, rowIndex) in groupedActors" :key="rowIndex" class="text-center">
                        <td v-for="actorName in row" :key="actorName">
                            <button class="actor-name-clickable border-0" @click="handleActorClick(actorName)" :class="{ 'loading': loadingActors.includes(actorName) }" :disabled="loadingActors.includes(actorName)">{{ actorName }}</button>
                            <span v-if="loadingActors.includes(actorName)" class="small-loading-spinner"></span>
                        </td>
                    </tr>
                </table>
                <h3 class="movie-actors-table-caption text-center">{{ movie.chineseName }} 的製作人員</h3>
                <hr class="text-center">
                <table key="movie-actors-table" class="table table-borderless text-center movie-actors-table">
                    <tr v-if="directorNamesExist">
                        <td colspan="5" class="text-center"><h4>導演</h4></td>
                        <td v-for="directorName in movie.directorNames" :key="directorName" class="text-center">
                            <h5 class="actor-name"> {{ directorName }} </h5>
                        </td>
                    </tr>
                    <tr v-else>
                        <td colspan="5" class="text-center"><h4>導演</h4></td>
                        <td colspan="5" class="text-center">
                            <h5 class="actor-name">無資料</h5>
                        </td>
                    </tr>
                    <tr v-if="scriptwriterNamesExist">
                        <td colspan="5" class="text-center"><h4>劇本作家</h4></td>
                        <td v-for="scriptwriterName in movie.scriptwriterNames" :key="scriptwriterName" class="text-center">
                            <h5 class="actor-name"> {{ scriptwriterName }} </h5>
                        </td>
                    </tr>
                    <tr v-else>
                        <td colspan="5" class="text-center"><h4>劇本作家</h4></td>
                        <td colspan="5" class="text-center">
                            <h5 class="actor-name">無資料</h5>
                        </td>
                    </tr>
                </table>
                <h5 class="text-center"><router-link :to="{ name: 'UpdateMoviePage', query: { id: movie.movieId } }" class="movie-router-link text-center">點我編輯此專頁</router-link></h5>
                <div class="text-center">
                    <button class="btn back-btn text-center" @click="backToMovieList">返回韓影列表</button>
                </div>
            </div>

            <!-- No Data Found -->
            <div v-else key="nodata">⚠目前沒有韓影資料
                <p>⚠目前沒有韓影資料</p>
            </div>

        </transition>
    </div>
</template>

<style lang="scss" scoped>
.movie-table {
    text-align:center;
    margin-top:3%;
}
.movie-table td {
    margin-left:10px;
    width:480px;
}
.movie-actors-table {
    padding:10px;
    width:95%;
    margin-top:2%;
}
.movie-actors-table td {
    height:200px;
    width:20%;
}
.current-ep {
    color:$autumn-dark-brown;
}
.movie-info {
    text-align:start;
    color:$autumn-text-dark;
    padding:6px 0px;
    font-weight:normal;
}
.movie-detailed-info {
    color:$autumn-dark-orange;
    padding:5px 0px;
    text-align:start;
    font-weight:normal;
}
.genres {
    color:$autumn-dark-brown;
    text-align:start;
    background-color:$autumn-light-orange;
    border-radius:2px;
    border:1px solid $autumn-dark-brown;
}
.movie-actors-table-caption {
    text-align:center;
    color:$autumn-dark-brown;
    margin-top:8px;
    padding:8px;
}
.actor-name {
    color:$autumn-dark-orange;
    margin-top:5px;
}
.actor-name-clickable, .trailer-url {
    color:$autumn-dark-orange;
    margin-top:5px;
    text-decoration:none;

    &:hover,
    &:active,
    &:focus {
        color:$autumn-dark-orange;
    }
}
.actor-name-clickable:hover, .trailer-url:hover {
    text-decoration:underline;
    font-weight:bolder;
}
.actor-name-clickable.loading {
    opacity: 0.5;
    pointer-events: none;
    transition: opacity 0.3s ease;
}
.small-loading-spinner {
    display: inline-block;
    width: 12px;
    height: 12px;
    border: 2px solid #fff;
    border-top: 2px solid $autumn-dark-orange;
    border-radius: 50%;
    animation: spin 0.6s linear infinite;
    margin-left: 5px;
    vertical-align: middle;
}
@keyframes spin {
    0% { transform: rotate(0deg);}
    100% { transform: rotate(360deg);}
}
.nodata {
    margin-top:50px;
    color:$autumn-red;
    font-size:larger;
    text-align:center;
}
.platforms {
    margin-left:-5px;
}
.platform-section-title {
    text-align:start;
    color:$autumn-text-dark;
}
hr {
    color:$autumn-dark-orange;
    width:auto;
}
ul {
    float:left;
    list-style:none;
    margin-left:-45px;
    margin-bottom:10px;
}
ul li {
    opacity:0.9;
    float:left;
    display:block;
    line-height:40px;
    margin:0px 5px;
    padding:1px 8px;
    font-weight:bolder;
}
.wish-btn {
    background-color:$autumn-dark-brown;
    color:$autumn-text-light;
    padding:5px;
    text-align:start;
    margin-left:0px;

    &:hover,
    &:active,
    &:focus {
        background-color:$autumn-dark-brown;
        color:$autumn-text-light;
    }
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
.movie-router-link {
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

/* Age Restrition Classes */
.age-12 {
    margin-left:5px;
    width:40px;
    height:40px;
    color:$autumn-light-orange;
    border:2px solid $autumn-light-orange;
    border-radius:50%;
    padding:1px;
}
.age-15 {
    margin-left:5px;
    width:40px;
    height:40px;
    color:$autumn-dark-orange;
    border:2px solid $autumn-dark-orange;
    border-radius:50%;
    padding:1px;
}
.age-19 {
    margin-left:5px;
    width:40px;
    height:40px;
    color:$autumn-red;
    border:2px solid $autumn-red;
    border-radius:50%;
    padding:1px;
}
.age-all {
    margin-left:5px;
    width:40px;
    height:40px;
    border:2px solid #1399FF;
    color:#1399FF;
    border-radius:20%;
    padding:1px;
}
.age-others {
    color:$autumn-text-dark;
    padding:1px;
}

/* Platform Classes */
.netflix {
    color:#E50914;

    &:hover,
    &:active,
    &:focus {
        color:#E50914;
    }
}
.disney-plus {
    color:teal;

    &:hover,
    &:active,
    &:focus {
        color:teal;
    }
}
.prime-video {
    color:#1399FF;

    &:hover,
    &:active,
    &:focus {
        color:#1399FF;
    }
}
.apple-tv {
    color:darkgray;

    &:hover,
    &:active,
    &:focus {
        color:darkgray;
    }
}
.hbo-max {
    color:gray;

    &:hover,
    &:active,
    &:focus {
        color:gray;
    }
}
.hami-video {
    color:greenyellow;

    &:hover,
    &:active,
    &:focus {
        color:greenyellow;
    }
}
.friday-video {
    color:aqua;

    &:hover,
    &:active,
    &:focus {
        color:aqua;
    }
}
.myvideo {
    color:orange;

    &:hover,
    &:active,
    &:focus {
        color:orange;
    }
}
.line-tv {
    color:#06C755;

    &:hover,
    &:active,
    &:focus {
        color:#06C755;
    }
}
.catch-play {
    color:$autumn-dark-orange;

    &:hover,
    &:active,
    &:focus {
        color:$autumn-dark-orange;
    }
}
.other-platforms {
    color:$autumn-text-dark;

    &:hover,
    &:active,
    &:focus {
        color:$autumn-text-dark;
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