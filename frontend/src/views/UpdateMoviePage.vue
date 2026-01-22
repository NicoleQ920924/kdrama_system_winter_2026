<script setup>
    import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
    import { findSelectedMovie, updateSelectedMovieViaApi, updateSelectedMovieAllInfo } from '@/services/movieService'
    import Spinner from '@/components/Spinner.vue'
    import { useRoute, useRouter } from 'vue-router'

    const loading = ref(false)
    const updatingMovies = ref([])
    const msg = ref('')
    const msgClass = ref('')

    const chineseNameBefore = ref('')
    const englishNameBefore = ref('')
    const koreanNameBefore = ref('')
    const trailerUrlBefore = ref('')
    const namuWikiPageUrlBefore = ref('')

    const route = useRoute()
    const router = useRouter()
    const selectedMovieId = computed(() => route.query.id ?? '')

    const displayData = (data) => {
        if (data === null || data === undefined || data === '') return '無資料';
        if (Array.isArray(data) && data.length === 0) return '無資料';
        return data;
    }

    const krAgeRestrictionDisplay = computed({
        get: () => displayData(updatedMovie.krAgeRestriction),
        set: (val) => {
            val == '無資料' ? '' : updatedMovie.krAgeRestriction;
        }
    });

    const releaseDateDisplay = computed({ // by ChatGPT
        get: () => formatDate(displayData(updatedMovie.releaseDate)),  // 顯示用格式化
        set: (val) => {
            updatedMovie.releaseDate = parseDateChineseStr(val);
        }
    });

    const totalRuntimeDisplay = computed({
        get: () => displayData(updatedMovie.totalRuntime),
        set: (val) => {
            val == '無資料' ? '' : updatedMovie.totalRuntime;
        }
    });

    const lastUpdatedByApiDisplay = computed({
        get: () => formatDateTime(updatedMovie.lastUpdatedByApi),
        set: (val) => {
            updatedMovie.lastUpdatedByApi = parseDateTimeChineseStr(val);
        }
    });

    const updatedMovie = reactive({
        movieId: null,            
        tmdbId: null,
        chineseName: '',
        englishName: '',
        koreanName: '',
        totalRuntime: null,
        krAgeRestriction: null,
        releaseDate: '',            
        genres: [],
        movieTwPlatformMap: [],
        leadActors: [],                
        directorNames: [],
        scriptwriterNames: [],
        mainPosterUrl: '',
        trailerUrl: '',
        introPageUrl: '',
        namuWikiPageUrl: '',
        manuallyEdited: false,
        lastUpdatedByApi: null    
    });

    async function startUpdateMovieViaApi(movieId) {
        // Return while loading
        if (updatingMovies.value.includes(movieId)) return;

        // Add loading status
        updatingMovies.value.push(movieId)

        try {
            const res = await updateSelectedMovieViaApi(movieId)
            console.log(res.status)
            if (res.status === 200) {
                loadMovie()
                msg.value = `${updatedMovie.chineseName} 的API資料已更新完畢！`
                msgClass.value = 'success-msg text-center'
            }
        } catch (err) {
            console.error(err)
            msg.value = `更新 ${updatedMovie.chineseName} 的API資料時發生錯誤`
            msgClass.value = 'error-msg text-center'
        } finally {
            // Remove loading status
            const index = updatingMovies.value.indexOf(movieId)
            if (index !== -1) updatingMovies.value.splice(index, 1)
        }
    }

    async function startUpdateMovieAllInfo(movieId, updatedMovie) {
        // Return while loading
        if (updatingMovies.value.includes(movieId)) return;

        // Add loading status
        updatingMovies.value.push(movieId)

        try {
            if (updatedMovie.manuallyEdited == false && 
                (trailerUrlBefore.value != updatedMovie.trailerUrl ||
                namuWikiPageUrlBefore.value != updatedMovie.namuWikiPageUrl ||
                chineseNameBefore.value != updatedMovie.chineseName ||
                englishNameBefore.value != updatedMovie.englishName ||
                koreanNameBefore.value != updatedMovie.koreanName)) {
                updatedMovie.manuallyEdited = true
            }
            const res = await updateSelectedMovieAllInfo(movieId, updatedMovie)
            console.log(res.status)
            if (res.status === 200) {
                msg.value = `${updatedMovie.chineseName} 的資料已更新完畢！`
                msgClass.value = 'success-msg text-center'
            }
        } catch (err) {
            console.error(err)
            msg.value = `更新 ${updatedMovie.chineseName} 的資料時發生錯誤`
            msgClass.value = 'error-msg text-center'
        } finally {
            // Remove loading status
            const index = updatingMovies.value.indexOf(movieId)
            if (index !== -1) updatingMovies.value.splice(index, 1)
        }
    }

    function loadMovie() {
        loading.value = true;
        findSelectedMovie(selectedMovieId.value)
        .then(res => {
            const data = res.data
            Object.assign(updatedMovie, data)
            chineseNameBefore.value = updatedMovie.chineseName
            englishNameBefore.value = updatedMovie.englishName
            koreanNameBefore.value = updatedMovie.koreanName
            trailerUrlBefore.value = updatedMovie.trailerUrl
            namuWikiPageUrlBefore.value = updatedMovie.namuWikiPageUrl
        })
        .catch(err => console.error(err))
        .finally(() => loading.value = false)
    }

    function formatDate(dateStr) {
        if (dateStr == '無資料') return '無資料';
        var [year, month, day] = dateStr.split('-');
        if (month.charAt(0) == '0') {
            month = month.slice(1)
        }
        if (day.charAt(0) == '0') {
            day = day.slice(1)
        }
        return `${year}年${month}月${day}日`;
    }

    function parseDateChineseStr(chineseStr) { // by ChatGPT
        if (!chineseStr) return '';
        // 假設輸入格式類似 "1995年8月9日"
        const match = chineseStr.match(/(\d{4})年(\d{1,2})月(\d{1,2})日/);
        if (!match) return chineseStr; // fallback

        let [, year, month, day] = match;
        // 補零，確保兩位數
        month = month.padStart(2, '0');
        day = day.padStart(2, '0');

        return `${year}-${month}-${day}`;
    }

    function formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '';
        const d = new Date(dateTimeStr);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1); // 0-based
        const day = String(d.getDate());
        const hour = String(d.getHours());
        const minute = String(d.getMinutes()).padStart(2, '0');
        return `${year}年${month}月${day}日 ${hour}:${minute}`;
    }

    function parseDateTimeChineseStr(chineseStr) {
        if (!chineseStr) return '';
        // 例如 "2025年8月18日 14:23"
        const match = chineseStr.match(/(\d{4})年(\d{1,2})月(\d{1,2})日\s+(\d{1,2}):(\d{2})/);
        if (!match) return chineseStr;

        let [, year, month, day, hour, minute] = match;
        month = month.padStart(2, '0');
        day = day.padStart(2, '0');
        hour = hour.padStart(2, '0');
        minute = minute.padStart(2, '0');

        return `${year}-${month}-${day} ${hour}:${minute}:00`;
    }

    function backToMovieList() {
        router.push({ name: 'MoviePage', query: {} })
    }

    onMounted(() => loadMovie())
    onUnmounted(() => updatedMovie.value = {})
</script>

<template>
    <div>
        <h2>更新韓影資料</h2>

        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="updatingMovies.length > 0 || loading" key="loading">
                <Spinner />
            </div>

            <!-- Not Loading -->
            <div v-else>
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <label for="movieId" class="form-label">韓影的ID</label>
                        <input v-model="updatedMovie.movieId" id="movieId" class="form-control form-text-field" name="movieId" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="tmdbId" class="form-label">韓影在TMDB上的ID</label>
                        <input v-model="updatedMovie.tmdbId" id="tmdbId" class="form-control form-text-field" name="tmdbId" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="chineseName" class="form-label">中文譯名 (可人工更新，之後除非此欄位為空值，否則無法透過API更新)</label>
                        <input v-model="updatedMovie.chineseName" id="chineseName" class="form-control form-text-field" name="chineseName" required aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="englishName" class="form-label">英文譯名 (可人工更新，之後除非此欄位為空值，否則無法透過API更新)</label>
                        <input v-model="updatedMovie.englishName" id="englishName" class="form-control form-text-field" name="englishName" required aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="koreanName" class="form-label">韓文名字 (可人工更新，之後除非此欄位為空值，否則無法透過API更新)</label>
                        <input v-model="updatedMovie.koreanName" id="koreanName" class="form-control form-text-field" name="koreanName" required aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="totalRuntime" class="form-label">片長 (分鐘)</label>
                        <input v-model="totalRuntimeDisplay" id="totalRuntime" class="form-control form-text-field" name="totalRuntime" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="krAgeRestriction" class="form-label">觀賞年齡限制 (0表示普遍級)</label>
                        <input v-model="krAgeRestrictionDisplay" id="krAgeRestriction" class="form-control form-text-field" name="krAgeRestriction" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="releaseDate" class="form-label">韓國上映日期</label>
                        <input v-model="releaseDateDisplay" id="releaseDate" class="form-control form-text-field" name="releaseDate" required readonly aria-required="true">
                    </p>
                    <div class="form-group form-text-p">
                        <h6>類型：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="updatedMovie.genres.length > 0">
                                    <li v-for="genre in updatedMovie.genres" :key="genre">
                                        {{ genre }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>台灣播出平台 (平台：連結)：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="Object.keys(updatedMovie.movieTwPlatformMap).length">
                                    <li v-for="(value, key) in updatedMovie.movieTwPlatformMap" :key="key">
                                        {{ key }} : {{ value }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>主演演員：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="updatedMovie.leadActors.length > 0">
                                    <li v-for="actor in updatedMovie.leadActors" :key="actor">
                                        {{ actor }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>導演：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="updatedMovie.directorNames.length > 0">
                                    <li v-for="name in updatedMovie.directorNames" :key="name">
                                        {{ name }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>劇本作家：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="updatedMovie.scriptwriterNames.length > 0">
                                    <li v-for="name in updatedMovie.scriptwriterNames" :key="name">
                                        {{ name }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                    </div>
                    <p class="form-group form-text-p">
                        <label for="mainPosterUrl" class="form-label">海報連結</label>
                        <input v-model="updatedMovie.mainPosterUrl" id="mainPosterUrl" class="form-control form-text-field" name="mainPosterUrl" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="trailerUrl" class="form-label">預告片連結 (需要人工更新)</label>
                        <input v-model="updatedMovie.trailerUrl" id="trailerUrl" class="form-control form-text-field" name="trailerUrl">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="introPageUrl" class="form-label">TMDB介紹頁連結</label>
                        <input v-model="updatedMovie.introPageUrl" id="introPageUrl" class="form-control form-text-field" name="introPageUrl" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="namuWikiPageUrl" class="form-label">Namu Wiki連結 (需要人工更新)</label>
                        <input v-model="updatedMovie.namuWikiPageUrl" id="namuWikiPageUrl" class="form-control form-text-field" name="namuWikiPageUrl">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="lastUpdatedByApi" class="form-label">API最近更新時間</label>
                        <input v-model="lastUpdatedByApiDisplay" id="lastUpdatedByApi" class="form-control form-text-field" name="lastUpdateByApi" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="manuallyEdited" class="form-label">是否有人工更新</label>
                        <input :value="updatedMovie.manuallyEdited ? '是' : '否'" id="manuallyEdited" class="form-control form-text-field" name="manuallyEdited" required readonly aria-required="true">
                    </p>
                    <div class="text-center">
                        <button @click="startUpdateMovieViaApi(updatedMovie.movieId)" type="button" class="btn shadow-none form-btn" :disabled="updatingMovies.length > 0">更新API資料</button>
                        <button @click="startUpdateMovieAllInfo(updatedMovie.movieId, updatedMovie)" type="button" class="btn shadow-none form-btn" :disabled="updatingMovies.length > 0">更新其他資訊</button>
                    </div>
                </form>
                <div :class="msgClass">{{ msg }}</div>
                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
                    <router-link class="btn back-btn text-center" :to="{ name: 'MoviePage', query: { id: updatedMovie.movieId } }">點我看 {{ updatedMovie.chineseName }} 的專頁</router-link>
                </div>
                <div v-if="msgClass == 'success-msg text-center' || msgClass == 'error-msg text-center'" class="text-center">
                    <button class="btn back-btn text-center" @click="backToMovieList">返回韓影列表</button>
                </div>
            </div>
        </transition>
    </div>
</template>

<style lang="scss" scoped>
    h2 
    {
        margin:30px 0px;
        color:$autumn-dark-yellow;
        text-align:center;
    }
    .form-text-p
    {
        margin-top:20px;
        margin-left:28%;
    }
    .form-text-field
    {
        margin:15px 0px 15px 0px;
        border:1px $autumn-light solid;
        width:500px;
    }
    .form-btn
    {
        background-color:$autumn-light-orange;
        border-color:$autumn-light;
        width:15%;
        font-size:large;
        margin:20px 10px 30px 10px;
    }
    .form-btn:hover, .form-btn:active, .form-btn:focus
    {
        background-color:$autumn-light-orange;
        text-decoration:none;
        outline:none;
        color:$autumn-dark-brown;
        width:15%;
    }
    h6 {
        font-weight:normal;
        margin:2px 0px;
    }
    .info-table {
        margin:15px 0px 15px 0px;
        border:1px $autumn-light solid;
        width:500px;
    }
    .enum-select {
        width:fit-content;
        margin:15px 0px 15px 0px;
        border:1px $autumn-light solid;
    }
    .success-msg
    {
        font-weight:bolder;
        color:green;
        font-size:large;
        margin:15px 0px;
    }
    .error-msg
    {
        font-weight:bolder;
        color:$autumn-red;
        font-size:large;
        margin:15px 0px;
    }
    .back-btn
    {
        border:none;
        background-color:none;
        color:$autumn-dark-brown;
        margin:5px 0px;

        &:hover,
        &:active,
        &:focus {
            color:$autumn-dark-brown;
        }
    }
</style>