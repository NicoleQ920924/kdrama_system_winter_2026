<script setup>
    import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
    import { findSelectedActor, updateSelectedActorViaApi, updateSelectedActorAllInfo } from '@/services/actorService'
    import Spinner from '@/components/Spinner.vue'
    import { useRoute, useRouter } from 'vue-router'

    const loading = ref(false)
    const updatingActors = ref([])
    const msg = ref('')
    const msgClass = ref('')

    const route = useRoute()
    const router = useRouter()
    const selectedActorId = computed(() => route.query.id ?? '')

    const biographyBefore = ref('')
    const namuWikiPageUrlBefore = ref('')

    const displayData = (data) => {
        if (data === null || data === undefined || data === '') return '無資料';
        if (Array.isArray(data) && data.length === 0) return '無資料';
        return data;
    }

    const genderOptions = [
        { value: "MALE", label: "男" },
        { value: "FEMALE", label: "女" }
    ];

    const birthdayDisplay = computed({ // by ChatGPT
        get: () => formatDate(updatedActor.birthday),  // 顯示用格式化
        set: (val) => {
            updatedActor.birthday = parseDateChineseStr(val);
        }
    });

    const biographyDisplay = computed({
        get: () => displayData(updatedActor.biography),
        set: (val) => {
            updatedActor.biography = (val === '無資料') ? '' : val;
        }
    });

    const lastUpdatedByApiDisplay = computed({
        get: () => formatDateTime(updatedActor.lastUpdatedByApi),
        set: (val) => {
            updatedActor.lastUpdatedByApi = parseDateTimeChineseStr(val);
        }
    });

    const updatedActor = reactive({
        actorId: null,            
        tmdbId: null,
        chineseName: '',
        englishName: '',
        koreanName: '',
        profilePicUrl: '',
        actorGender: '', // Refer to genderOptions
        birthday: '',              
        biography: '',
        dramas: [],                
        movies: [],
        namuWikiPageUrl: '',                
        manuallyEdited: false,
        lastUpdatedByApi: null    
    });

    async function startUpdateActorViaApi(actorId, includesExistingWork) {
        // Return while loading
        if (updatingActors.value.includes(actorId)) return;

        // Add loading status
        updatingActors.value.push(actorId)

        try {
            const res = await updateSelectedActorViaApi(actorId, includesExistingWork)
            console.log(res.status)
            if (res.status === 200) {
                loadActor()
                msg.value = `${updatedActor.chineseName} 的API資料已更新完畢！`
                msgClass.value = 'success-msg text-center'
            }
        } catch (err) {
            console.error(err)
            msg.value = `更新 ${updatedActor.chineseName} 的API資料時發生錯誤`
            msgClass.value = 'error-msg text-center'
        } finally {
            // Remove loading status
            const index = updatingActors.value.indexOf(actorId)
            if (index !== -1) updatingActors.value.splice(index, 1)
        }
    }

    async function startUpdateActorAllInfo(actorId, updatedActor) {
        // Return while loading
        if (updatingActors.value.includes(actorId)) return;

        // Add loading status
        updatingActors.value.push(actorId)

        try {
            if (updatedActor.manuallyEdited == false && 
                (biographyBefore.value != updatedActor.biography || 
                namuWikiPageUrlBefore.value != updatedActor.namuWikiPageUrl)) {
                updatedActor.manuallyEdited = true
            }
            const res = await updateSelectedActorAllInfo(actorId, updatedActor)
            console.log(res.status)
            if (res.status === 200) {
                msg.value = `${updatedActor.chineseName} 的資料已更新完畢！`
                msgClass.value = 'success-msg text-center'
            }
        } catch (err) {
            console.error(err)
            msg.value = `更新 ${updatedActor.chineseName} 的資料時發生錯誤`
            msgClass.value = 'error-msg text-center'
        } finally {
            // Remove loading status
            const index = updatingActors.value.indexOf(actorId)
            if (index !== -1) updatingActors.value.splice(index, 1)
        }
    }

    function loadActor() {
        loading.value = true;
        findSelectedActor(selectedActorId.value, false) // displayNameMode = false -> value = MALE / FEMALE
        .then(res => {
            const data = res.data
            Object.assign(updatedActor, data)
            biographyBefore.value = updatedActor.biography
            namuWikiPageUrlBefore.value = updatedActor.namuWikiPageUrl
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

    function backToActorList() {
        router.push({ name: 'ActorPage', query: {} })
    }

    onMounted(() => loadActor())
    onUnmounted(() => updatedActor.value = {})
</script>

<template>
    <div>
        <h2>更新演員資料</h2>

        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="updatingActors.length > 0 || loading" key="loading">
                <Spinner />
            </div>

            <!-- Not Loading -->
            <div v-else>
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <label for="actorId" class="form-label">演員的ID</label>
                        <input v-model="updatedActor.actorId" id="actorId" class="form-control form-text-field" name="actorId" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="tmdbId" class="form-label">演員在TMDB上的ID</label>
                        <input v-model="updatedActor.tmdbId" id="tmdbId" class="form-control form-text-field" name="tmdbId" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="chineseName" class="form-label">中文譯名</label>
                        <input v-model="updatedActor.chineseName" id="chineseName" class="form-control form-text-field" name="chineseName" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="englishName" class="form-label">英文譯名</label>
                        <input v-model="updatedActor.englishName" id="englishName" class="form-control form-text-field" name="englishName" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="koreanName" class="form-label">韓文名字</label>
                        <input v-model="updatedActor.koreanName" id="koreanName" class="form-control form-text-field" name="koreanName" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="profilePicUrl" class="form-label">大頭照連結</label>
                        <input v-model="updatedActor.profilePicUrl" id="profilePicUrl" class="form-control form-text-field" name="profilePicUrl" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="actorGender" class="form-label">性別</label>
                        <select id="actorGender" class="form-select gender-select" v-model="updatedActor.actorGender" disabled>
                            <option v-for="opt in genderOptions" :key="opt.value" :value="opt.value">
                            {{ opt.label }}
                            </option>
                        </select>
                        <!-- Keep the original value to submit -->
                        <input type="hidden"  name="actorGender" :value="updatedActor.actorGender" />
                    </p>
                    <p class="form-group form-text-p">
                        <label for="birthday" class="form-label">出生年月日</label>
                        <input v-model="birthdayDisplay" id="birthday" class="form-control form-text-field" name="birthday" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="biography" class="form-label">簡介 (最多200字，可人工更新，之後除非此欄位為空值，否則無法透過API更新)</label>
                        <input v-model="biographyDisplay" id="biography" class="form-control form-text-field" name="biography" required aria-required="true">
                    </p>
                    <div class="form-group form-text-p">
                        <h6>出演韓劇：</h6>
                        <table class="table form-control work-table">
                            <tr>
                                <ul>
                                    <li v-for="drama in updatedActor.dramas" :key="drama.dramaId">
                                        {{ drama.chineseName }}
                                    </li>
                                </ul>
                            </tr>
                        </table>
                        <h6>出演韓影：</h6>
                        <table class="table form-control work-table">
                            <tr>
                                <ul>
                                    <li v-for="movie in updatedActor.movies" :key="movie.Id">
                                        {{ movie.chineseName }}
                                    </li>
                                </ul>
                            </tr>
                        </table>
                    </div>
                    <p class="form-group form-text-p">
                        <label for="namuWikiPageUrl" class="form-label">Namu Wiki連結 (需要人工更新)</label>
                        <input v-model="updatedActor.namuWikiPageUrl" id="namuWikiPageUrl" class="form-control form-text-field" name="namuWikiPageUrl">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="lastUpdatedByApi" class="form-label">API最近更新時間</label>
                        <input v-model="lastUpdatedByApiDisplay" id="lastUpdatedByApi" class="form-control form-text-field" name="lastUpdateByApi" required readonly aria-required="true">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="manuallyEdited" class="form-label">是否有人工更新</label>
                        <input :value="updatedActor.manuallyEdited ? '是' : '否'" id="manuallyEdited" class="form-control form-text-field" name="manuallyEdited" required readonly aria-required="true">
                    </p>
                    <div class="text-center">
                        <button @click="startUpdateActorViaApi(updatedActor.actorId, true)" type="button" class="btn shadow-none form-btn" :disabled="updatingActors.length > 0">更新API資料 (包含現有作品)</button>
                        <button @click="startUpdateActorViaApi(updatedActor.actorId, false)" type="button" class="btn shadow-none form-btn" :disabled="updatingActors.length > 0">更新API資料 (不包含現有作品)</button>
                        <button @click="startUpdateActorAllInfo(updatedActor.actorId, updatedActor)" type="button" class="btn shadow-none form-btn" :disabled="updatingActors.length > 0">更新其他資訊</button>
                    </div>
                </form>
                <div :class="msgClass">{{ msg }}</div>
                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
                    <router-link class="btn back-btn text-center" :to="{ name: 'ActorPage', query: { id: updatedActor.actorId } }">點我看 {{ updatedActor.chineseName }} 的專頁</router-link>
                </div>
                <div v-if="msgClass == 'success-msg text-center' || msgClass == 'error-msg text-center'" class="text-center">
                    <button class="btn back-btn text-center" @click="backToActorList">返回演員列表</button>
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
    .work-table {
        margin:15px 0px 15px 0px;
        border:1px $autumn-light solid;
        width:500px;
    }
    .gender-select {
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