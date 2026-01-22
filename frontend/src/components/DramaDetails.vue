<!-- Imported in DramaPage.vue -->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { findSelectedDrama } from '@/services/dramaService'
import { importActor } from '@/services/actorService'
import Spinner from './Spinner.vue'

const props = defineProps({
  selectedDramaId: String
})

const emit = defineEmits(['reset-drama'])

const drama = ref({})
const loading = ref(true)

const loadingActors = ref([]) // for handleActorClick()

function loadDrama() {
  loading.value = true;
  findSelectedDrama(props.selectedDramaId)
    .then(res => {
      drama.value = res.data;
    })
    .catch(err => console.error(err))
    .finally(() => 
        loading.value = false
    )
}

const groupedActors = computed(() => {
  const groups = [];
  const actors = drama.value.leadActors || [];
  for (let i = 0; i < actors.length; i += 5) {
    groups.push(actors.slice(i, i + 5));
  }
  return groups;
})

const directorNamesExist = computed(() => {
  const directorNames = drama.value.directorNames;
  if (directorNames.length > 0) {
    return true;
  }
  else {
    return false;
  }
})

const scriptwriterNamesExist = computed(() => {
  const scriptwriterNames = drama.value.scriptwriterNames;
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

function getStatusClass(status) {
    if (status == "即將播出") {
        return "not-aired"
    }
    else if (status == "跟播中") {
        return "ongoing"
    }
    else { // 已完結
        return "completed"
    }
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

function backToDramaList() {
    emit('reset-drama')
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

onMounted(() => loadDrama())
</script>


<template>
    <div>
        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="loading" key="loading">
                <Spinner />
            </div>

            <!-- Finish Loading -->
            <div v-else-if="Object.keys(drama).length > 0" class="drama-details">
                <table key="drama-table" class="table table-borderless drama-table">
                    <tr>
                        <!-- Left: Poster; Right: Other Information -->
                        <td>
                            <img height="720px" :src="drama.mainPosterUrl" alt="無圖">
                            <br>
                            <a v-if="drama.trailerUrl != null && drama.trailerUrl != undefined && drama.trailerUrl != ''" :href="drama.trailerUrl" class="trailer-url">點我觀賞預告片</a>
                        </td>
                        <td>
                            <h4 :class="getStatusClass(drama.status)">{{ drama.status }}</h4>
                            <h3 class="drama-info">{{ drama.chineseName }}</h3> 
                            <h4 class="drama-info"><a :href="drama.namuWikiPageUrl" class="namu-wiki-link">{{ drama.koreanName }}</a></h4>
                            <h4 class="drama-info">{{ drama.englishName }}</h4>
                            <div>
                                <ul>
                                    <li v-for="genre in drama.genres" :key="genre" class="genres">{{ displayData(genre) }}</li>
                                </ul>
                            </div>
                            <br><br>
                            <p class="drama-detailed-info">{{ displayData(drama.releaseYear) }} <span :class="getAgeRestrictClass(drama.krAgeRestriction)">{{ drama.krAgeRestriction === 0 ? '普遍級' : (drama.krAgeRestriction || '無年齡資料') }}</span></p>
                            <div class="drama-detailed-info">
                                <h5 class="drama-detailed-info">韓國放送：</h5>
                                <ul>
                                    <li v-for="network in drama.networks" :key="network" class="networks">{{ displayData(network) }}</li>
                                    <li>{{ drama.krReleaseSchedule }}</li>
                                </ul>
                            </div>
                            <br><br>
                            <p class="drama-detailed-info">每集 {{ drama.estRuntimePerEp != '0' ? drama.estRuntimePerEp : '[待補]' }} 分鐘，共 {{ drama.totalNumOfEps }} 集<span class="current-ep" v-if="drama.status == '跟播中'"> (目前播到第{{ drama.currentEpNo }}集)</span></p>
                            <p class="drama-detailed-info">
                                <button class="btn wish-btn">加入追蹤清單</button>
                            </p>
                            <div class="platforms">
                                <h5 class="platform-section-title">台灣播出平台 <span class="warning">(台灣本土平台可能會比韓國晚一天上架)</span></h5>
                                <ul v-if="Object.keys(drama.dramaTwPlatformMap).length > 0">
                                    <li v-for="(value, key) in drama.dramaTwPlatformMap" :key="key">
                                    <!-- key = platform_name, value = url -->
                                        <a :class="getPlatformClass(key)" :href="value">{{ key }}</a>
                                    </li>
                                </ul>
                                <h6 v-else class="drama-detailed-info">無資料</h6>
                            </div>
                        </td>
                    </tr>
                </table>
                <h3 class="drama-actors-table-caption text-center">{{ drama.chineseName }} 的主演演員</h3>
                <h4 class="drama-actors-table-caption text-center">點擊演員的連結即可將演員和其作品加入資料庫</h4>
                <hr class="text-center">
                <table key="drama-actors-table" class="table table-borderless text-center drama-actors-table">
                    <tr v-for="(row, rowIndex) in groupedActors" :key="rowIndex" class="text-center">
                        <td v-for="actorName in row" :key="actorName">
                            <button class="actor-name-clickable border-0" @click="handleActorClick(actorName)" :class="{ 'loading': loadingActors.includes(actorName) }" :disabled="loadingActors.includes(actorName)">{{ actorName }}</button>
                            <span v-if="loadingActors.includes(actorName)" class="small-loading-spinner"></span>
                        </td>
                    </tr>
                </table>
                <h3 class="drama-actors-table-caption text-center">{{ drama.chineseName }} 的製作人員</h3>
                <hr class="text-center">
                <table key="drama-actors-table" class="table table-borderless text-center drama-actors-table">
                    <tr v-if="directorNamesExist">
                        <td colspan="5" class="text-center"><h4>導演</h4></td>
                        <td v-for="directorName in drama.directorNames" :key="directorName" class="text-center">
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
                        <td v-for="scriptwriterName in drama.scriptwriterNames" :key="scriptwriterName" class="text-center">
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
                <h5 class="text-center"><router-link :to="{ name: 'UpdateDramaPage', query: { id: drama.dramaId } }" class="drama-router-link text-center">點我編輯此專頁</router-link></h5>
                <div class="text-center">
                    <button class="btn back-btn text-center" @click="backToDramaList">返回韓劇列表</button>
                </div>
            </div>

            <!-- No Data Found -->
            <div v-else key="nodata">⚠目前沒有韓劇資料
                <p class="nodata">⚠目前沒有韓劇資料</p>
            </div>

        </transition>
    </div>
</template>

<style lang="scss" scoped>
.drama-table {
    text-align:center;
    margin-top:3%;
}
.drama-table td {
    margin-left:10px;
    width:480px;
}
.drama-actors-table {
    padding:10px;
    width:95%;
    margin-top:2%;
}
.drama-actors-table td {
    height:200px;
    width:20%;
}
.current-ep {
    color:$autumn-dark-brown;
}
.drama-info {
    text-align:start;
    color:$autumn-text-dark;
    padding:6px 0px;
    font-weight:normal;
}
.drama-detailed-info {
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
    margin-top:-3px;
}
.drama-actors-table-caption {
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
.warning {
    color:$autumn-red;
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
.drama-router-link {
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

/* Status Classes */
.not-aired {
    color:coral;
    background-color:lightpink;
    width:fit-content;
    margin-top:-5%;
}
.ongoing {
    color:#1399FF;
    background-color:lightblue;
    width:fit-content;
    margin-top:-5%;
}
.completed {
    color:#06C755;
    background-color:lightgreen;
    width:fit-content;
    margin-top:-5%;
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