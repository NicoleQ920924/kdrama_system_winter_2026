<script setup>
    import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
    import { findSelectedDramaById, updateSelectedDramaViaApi, updateSelectedDramaViaAiAndForm, updateSelectedDramaViaForm } from '@/services/dramaService'
    import Spinner from '@/components/Spinner.vue'
    import { useRoute, useRouter } from 'vue-router'

    const loading = ref(false)
    const updatingDramas = ref([])
    const msg = ref('')
    const msgClass = ref('')

    const chineseNameBefore = ref('')
    const englishNameBefore = ref('')
    const koreanNameBefore = ref('')
    const trailerUrlBefore = ref('')
    const chineseWikipediaPageUrlBefore = ref('')
    const namuWikiPageUrlBefore = ref('')

    const route = useRoute()
    const router = useRouter()
    const selectedDramaId = computed(() => route.query.id ?? '')

    const displayData = (data) => {
        if (data === null || data === undefined || data === '') return '無資料';
        if (Array.isArray(data) && data.length === 0) return '無資料';
        return data;
    }

    const krAgeRestrictionDisplay = computed({
        get: () => displayData(loadedDrama.krAgeRestriction),
        set: (val) => {
            val == '無資料' ? '' : loadedDrama.krAgeRestriction;
        }
    });

    const releaseYearDisplay = computed({
        get: () => displayData(loadedDrama.releaseYear),
        set: (val) => {
            val == '無資料' ? '' : loadedDrama.releaseYear;
        }
    });

    const statusOptions = [
        { value: "NOT_AIRED", label: "即將播出" },
        { value: "ONGOING", label: "跟播中" },
        { value: "COMPLETED", label: "已完結" }
    ];

    const releaseScheduleOptions = [
        { value: "MON_TUE", label: "月火" },
        { value: "WED_THU", label: "水木" },
        { value: "FRI_SAT", label: "金土" },
        { value: "SAT_SUN", label: "土日" },
        { value: "FULL_RELEASE", label: "公開日全集上架" },
        { value: "UNKNOWN", label: "尚未公布播出日程" },
        { value: "OTHERS", label: "其他" },
    ];

    const lastUpdatedByApiDisplay = computed({
        get: () => formatDateTime(loadedDrama.lastUpdatedByApi),
        set: (val) => {
            loadedDrama.lastUpdatedByApi = parseDateTimeChineseStr(val);
        }
    });

    const loadedDrama = reactive({
        dramaId: null,            
        tmdbId: null,
        seasonNumber: null,
        chineseName: '',
        englishName: '',
        koreanName: '',
        totalNumOfEps: null,
        currentEpNo: null,
        estRuntimePerEp: '',
        krAgeRestriction: null,
        releaseYear: '',
        status: '', // Refer to statusOptions
        krReleaseSchedule: '', // Refer to releaseScheduleOptions             
        genres: [],
        networks: [],
        dramaTwPlatformMap: [],
        leadActors: [],                
        directorNames: [],
        scriptwriterNames: [],
        mainPosterUrl: '',
        trailerUrl: '',
        introPageUrl: '',
        chineseWikipediaPageUrl: '',
        namuWikiPageUrl: '', 
        aiOrManuallyEdited: null,
        lastUpdatedByApi: null    
    });

    async function startUpdateDramaViaApi(dramaId) {
        // Return while loading
        if (updatingDramas.value.includes(dramaId)) return;

        // Add loading status
        updatingDramas.value.push(dramaId)

        try {
            const res = await updateSelectedDramaViaApi(dramaId)
            console.log(res.status)
            if (res.status === 200) {
                loadDrama()
                msg.value = `${loadedDrama.chineseName} 的API資料已更新完畢！`
                msgClass.value = 'success-msg text-center'
            }
        } catch (err) {
            console.error(err)
            msg.value = `更新 ${loadedDrama.chineseName} 的API資料時發生錯誤`
            msgClass.value = 'error-msg text-center'
        } finally {
            // Remove loading status
            const index = updatingDramas.value.indexOf(dramaId)
            if (index !== -1) updatingDramas.value.splice(index, 1)
        }
    }

    async function startUpdateDramaViaAiAndForm(dramaId, loadedDrama) {
        // Return while loading
        if (updatingDramas.value.includes(dramaId)) return;

        // Add loading status
        updatingDramas.value.push(dramaId)

        try {
            if (loadedDrama.aiOrManuallyEdited == false) {
                loadedDrama.aiOrManuallyEdited = true
            }
            const res = await updateSelectedDramaViaAiAndForm(dramaId, loadedDrama)
            console.log(res.status)
            if (res.status === 200) {
                loadDrama()
                msg.value = `${loadedDrama.chineseName} 的資料已更新完畢！`
                msgClass.value = 'success-msg text-center'
            }
        } catch (err) {
            console.error(err)
            msg.value = `更新 ${loadedDrama.chineseName} 的資料時發生錯誤`
            msgClass.value = 'error-msg text-center'
        } finally {
            // Remove loading status
            const index = updatingDramas.value.indexOf(dramaId)
            if (index !== -1) updatingDramas.value.splice(index, 1)
        }
    }

    async function startUpdateDramaViaForm(dramaId, loadedDrama) {
        // Return while loading
        if (updatingDramas.value.includes(dramaId)) return;

        // Add loading status
        updatingDramas.value.push(dramaId)

        try {
            if (loadedDrama.aiOrManuallyEdited == false && 
                (trailerUrlBefore.value != loadedDrama.trailerUrl || 
                chineseWikipediaPageUrlBefore.value != loadedDrama.chineseWikipediaPageUrl ||
                namuWikiPageUrlBefore.value != loadedDrama.namuWikiPageUrl || 
                chineseNameBefore.value != loadedDrama.chineseName ||
                englishNameBefore.value != loadedDrama.englishName ||
                koreanNameBefore.value != loadedDrama.koreanName)) {
                loadedDrama.aiOrManuallyEdited = true
            }
            const res = await updateSelectedDramaViaForm(dramaId, loadedDrama)
            console.log(res.status)
            if (res.status === 200) {
                loadDrama()
                msg.value = `${loadedDrama.chineseName} 的資料已更新完畢！`
                msgClass.value = 'success-msg text-center'
            }
        } catch (err) {
            console.error(err)
            msg.value = `更新 ${loadedDrama.chineseName} 的資料時發生錯誤`
            msgClass.value = 'error-msg text-center'
        } finally {
            // Remove loading status
            const index = updatingDramas.value.indexOf(dramaId)
            if (index !== -1) updatingDramas.value.splice(index, 1)
        }
    }

    function loadDrama() {
        loading.value = true;
        findSelectedDramaById(selectedDramaId.value, false) // displayNameMode = false -> value = MALE / FEMALE
        .then(res => {
            const data = res.data
            Object.assign(loadedDrama, data)
            chineseNameBefore.value = loadedDrama.chineseName
            englishNameBefore.value = loadedDrama.englishName
            koreanNameBefore.value = loadedDrama.koreanName
            trailerUrlBefore.value = loadedDrama.trailerUrl
            chineseWikipediaPageUrlBefore.value = loadedDrama.chineseWikipediaPageUrl
            namuWikiPageUrlBefore.value = loadedDrama.namuWikiPageUrl
        })
        .catch(err => console.error(err))
        .finally(() => loading.value = false)
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

    function backToDramaList() {
        router.push({ name: 'DramaPage', query: {} })
    }

    onMounted(() => loadDrama())
    onUnmounted(() => loadedDrama.value = {})
</script>

<template>
    <div>
        <h2>更新韓劇資料</h2>

        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="updatingDramas.length > 0 || loading" key="loading">
                <Spinner />
            </div>

            <!-- Not Loading -->
            <div v-else>
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <label for="dramaId" class="form-label">韓劇的ID</label>
                        <input v-model="loadedDrama.dramaId" id="dramaId" class="form-control form-text-field" name="dramaId" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="tmdbId" class="form-label">韓劇在TMDB上的ID</label>
                        <input v-model="loadedDrama.tmdbId" id="tmdbId" class="form-control form-text-field" name="tmdbId" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="seasonNumber" class="form-label">季數</label>
                        <input v-model="loadedDrama.seasonNumber" id="seasonNumber" class="form-control form-text-field" name="seasonNumber" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="chineseName" class="form-label">中文譯名 (AI或人工更新)</label>
                        <input v-model="loadedDrama.chineseName" id="chineseName" class="form-control form-text-field" name="chineseName" required aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="englishName" class="form-label">英文譯名 (AI或人工更新)</label>
                        <input v-model="loadedDrama.englishName" id="englishName" class="form-control form-text-field" name="englishName" required aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="koreanName" class="form-label">韓文劇名 (AI或人工更新)</label>
                        <input v-model="loadedDrama.koreanName" id="koreanName" class="form-control form-text-field" name="koreanName" required aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="totalNumOfEps" class="form-label">總集數 (API更新)</label>
                        <input v-model="loadedDrama.totalNumOfEps" id="totalNumOfEps" class="form-control form-text-field" name="totalNumOfEps" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="currentEpNo" class="form-label">最近播畢集數 (API更新)</label>
                        <input v-model="loadedDrama.currentEpNo" id="currentEpNo" class="form-control form-text-field" name="currentEpNo" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="estRuntimePerEp" class="form-label">每集時長 (分鐘) (API更新)</label>
                        <input v-model="loadedDrama.estRuntimePerEp" id="estRuntimePerEp" class="form-control form-text-field" name="estRuntimePerEp" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="krAgeRestriction" class="form-label">觀賞年齡限制 (0表示普遍級) (API更新)</label>
                        <input v-model="krAgeRestrictionDisplay" id="krAgeRestriction" class="form-control form-text-field" name="krAgeRestriction" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="releaseYear" class="form-label">播出年度 (API更新)</label>
                        <input v-model="releaseYearDisplay" id="releaseYear" class="form-control form-text-field" name="releaseYear" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="status" class="form-label">播出狀態 (API更新)</label>
                        <select id="status" class="form-select enum-select" v-model="loadedDrama.status" disabled>
                            <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
                            {{ opt.label }}
                            </option>
                        </select>
                        <!-- Keep the original value to submit -->
                        <input type="hidden"  name="status" :value="loadedDrama.status" />
                    </p>
                    <p class="form-group form-text-p">
                        <label for="krReleaseSchedule" class="form-label">韓國播出日程 (API更新)</label>
                        <select id="krReleaseSchedule" class="form-select enum-select" v-model="loadedDrama.krReleaseSchedule" disabled>
                            <option v-for="opt in releaseScheduleOptions" :key="opt.value" :value="opt.value">
                            {{ opt.label }}
                            </option>
                        </select>
                        <!-- Keep the original value to submit -->
                        <input type="hidden"  name="krReleaseSchedule" :value="loadedDrama.krReleaseSchedule" />
                    </p>
                    <div class="form-group form-text-p">
                        <h6>類型 (API更新)：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="loadedDrama.genres.length > 0">
                                    <li v-for="genre in loadedDrama.genres" :key="genre">
                                        {{ genre }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>韓國原始播出頻道或平台 (API更新)：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="loadedDrama.networks.length > 0">
                                    <li v-for="network in loadedDrama.networks" :key="network">
                                        {{ network }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>台灣播出平台 (平台：連結) (API更新)：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="Object.keys(loadedDrama.dramaTwPlatformMap).length > 0">
                                    <li v-for="(value, key) in loadedDrama.dramaTwPlatformMap" :key="key">
                                        {{ key }} : {{ value }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>主演演員 (API更新)：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="loadedDrama.leadActors.length > 0">
                                    <li v-for="actor in loadedDrama.leadActors" :key="actor">
                                        {{ actor }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>導演 (API更新)：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="loadedDrama.directorNames.length > 0">
                                    <li v-for="name in loadedDrama.directorNames" :key="name">
                                        {{ name }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                        <h6>劇本作家 (API更新)：</h6>
                        <table class="table form-control info-table">
                            <tr>
                                <ul v-if="loadedDrama.scriptwriterNames.length > 0">
                                    <li v-for="name in loadedDrama.scriptwriterNames" :key="name">
                                        {{ name }}
                                    </li>
                                </ul>
                                <span v-else>無資料</span>
                            </tr>
                        </table>
                    </div>
                    <p class="form-group form-text-p">
                        <label for="mainPosterUrl" class="form-label">海報連結 (API更新)</label>
                        <input v-model="loadedDrama.mainPosterUrl" id="mainPosterUrl" class="form-control form-text-field" name="mainPosterUrl" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="trailerUrl" class="form-label">預告片連結 (AI或人工更新)</label>
                        <input v-model="loadedDrama.trailerUrl" id="trailerUrl" class="form-control form-text-field" name="trailerUrl">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="introPageUrl" class="form-label">TMDB介紹頁連結 (API更新)</label>
                        <input v-model="loadedDrama.introPageUrl" id="introPageUrl" class="form-control form-text-field" name="introPageUrl" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="chineseWikipediaPageUrl" class="form-label">中文維基百科連結 (AI或人工更新)</label>
                        <input v-model="loadedDrama.chineseWikipediaPageUrl" id="chineseWikipediaPageUrl" class="form-control form-text-field" name="chineseWikipediaPageUrl">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="namuWikiPageUrl" class="form-label">Namu Wiki連結 (AI或人工更新)</label>
                        <input v-model="loadedDrama.namuWikiPageUrl" id="namuWikiPageUrl" class="form-control form-text-field" name="namuWikiPageUrl">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="lastUpdatedByApi" class="form-label">API最近更新時間</label>
                        <input v-model="lastUpdatedByApiDisplay" id="lastUpdatedByApi" class="form-control form-text-field" name="lastUpdateByApi" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="aiOrManuallyEdited" class="form-label">是否有AI或人工更新</label>
                        <input :value="loadedDrama.aiOrManuallyEdited ? '是' : '否'" id="aiOrManuallyEdited" class="form-control form-text-field" name="aiOrManuallyEdited" required readonly aria-required="true">
                    </p>
                    <div class="text-center">
                        <button @click="startUpdateDramaViaApi(loadedDrama.dramaId)" type="button" class="btn shadow-none form-btn" :disabled="updatingDramas.length > 0">透過TMDB API更新資料</button>
                        <button @click="startUpdateDramaViaAiAndForm(loadedDrama.dramaId, loadedDrama)" type="button" class="btn shadow-none form-btn" :disabled="updatingDramas.length > 0">透過AI和人工更新資料</button>
                        <button @click="startUpdateDramaViaForm(loadedDrama.dramaId, loadedDrama)" type="button" class="btn shadow-none form-btn" :disabled="updatingDramas.length > 0">儲存人工更新資料</button>
                    </div>
                </form>
                <div :class="msgClass">{{ msg }}</div>
                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
                    <router-link class="btn back-btn text-center" :to="{ name: 'DramaPage', query: { id: loadedDrama.dramaId } }">點我看 {{ loadedDrama.chineseName }} 的專頁</router-link>
                </div>
                <div v-if="msgClass == 'success-msg text-center' || msgClass == 'error-msg text-center'" class="text-center">
                    <button class="btn back-btn text-center" @click="backToDramaList">返回韓劇列表</button>
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