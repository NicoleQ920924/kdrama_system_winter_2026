<script setup>
    import { computed, ref } from 'vue'
    import Spinner from '@/components/Spinner.vue'
    import AiMovieSearchResultModal from '@/components/modals/AiMovieSearchResultModal.vue'
    import { searchMoviesByPrompt } from '@/services/aiService'
    import { useRouter } from 'vue-router'

    const router = useRouter()

    const loading = ref(false)
    const msg = ref('')
    const msgClass = ref('')
    const searchResults = ref([])
    const showSearchModal = ref(false)

    const movieIncludeString = ref('')

    const actorName1 = ref('')
    const actorName2 = ref('')
    const actorName3 = ref('')

    const characterName1 = ref('')
    const characterName2 = ref('')
    const characterName3 = ref('')

    const characterDescription = ref('')
    const partOfPlot = ref('')

    const genre1 = ref('')
    const genre2 = ref('')

    // Refer to PlatformInfoPage.vue (same names)
    const platforms = [
        { name: "Netflix" },
        { name: "Disney Plus" },
        { name: "Amazon Prime Video" },
        { name: "Apple TV" },
        { name: "HBO Max" },
        { name: "Hami Video" },
        { name: "遠傳friDay影音" },
        { name: "MyVideo" },
        { name: "LINE TV" },
        { name: "Catchplay" },
    ]

    const selectedPlatforms = ref([])

    const trimmed = (v) => (v ?? '').trim()
    const pickedActors = computed(() => [actorName1.value, actorName2.value, actorName3.value].map(trimmed).filter(Boolean))
    const pickedCharacters = computed(() => [characterName1.value, characterName2.value, characterName3.value].map(trimmed).filter(Boolean))
    const pickedGenres = computed(() => [genre1.value, genre2.value].map(trimmed).filter(Boolean))
    const pickedPlatforms = computed(() => (selectedPlatforms.value ?? []).map(trimmed).filter(Boolean))

    const hasAnyCoreCriteria = computed(() => {
        return movieIncludeString.value !== '' ||
            pickedActors.value.length > 0 ||
            pickedCharacters.value.length > 0 ||
            trimmed(characterDescription.value) !== '' ||
            trimmed(partOfPlot.value) !== ''
    })

    const canSubmit = computed(() => hasAnyCoreCriteria.value && !loading.value)

    const promptToSend = computed(() => {
        const lines = []
        lines.push('請搜尋一部韓影，並根據以下條件挑選最符合的一部（若不確定請列出 3 個候選）。')

        if (movieIncludeString.value !== '') lines.push(`- 電影名稱包含的字串：${movieIncludeString.value}`)
        if (pickedActors.value.length) lines.push(`- 演員：${pickedActors.value.join('、')}`)
        if (pickedCharacters.value.length) lines.push(`- 角色名稱：${pickedCharacters.value.join('、')}`)
        if (trimmed(characterDescription.value) !== '') lines.push(`- 角色描述：${trimmed(characterDescription.value)}`)
        if (trimmed(partOfPlot.value) !== '') lines.push(`- 劇情片段：${trimmed(partOfPlot.value)}`)
        if (pickedGenres.value.length) lines.push(`- 類型：${pickedGenres.value.join('、')}`)
        if (pickedPlatforms.value.length) lines.push(`- 台灣可觀看平台（越符合越好）：${pickedPlatforms.value.join('、')}`)

       lines.push('請再三透過搜尋確認片名都是台灣官方譯名，且簡介有包含劇情大綱。')
        lines.push('請用繁體中文回覆，並以 JSON 格式提供片名和簡介。')
        return lines.join('\n')
    })

    async function startAiImportMovie() {
        if (loading.value) return

        msg.value = ''
        msgClass.value = ''
        searchResults.value = []
        showSearchModal.value = false

        if (!hasAnyCoreCriteria.value) {
            msg.value = '送出前請至少填寫：片名包含的字串 / 任一位演員 / 任一個角色 / 角色描述 / 劇情片段'
            msgClass.value = 'error-msg text-center'
            return
        }

        loading.value = true
        try {
            const res = await searchMoviesByPrompt(promptToSend.value)
            if (res.status === 200) {
                const results = res.data?.results ?? []
                if (results.length > 0) {
                    searchResults.value = results
                    showSearchModal.value = true
                    msg.value = 'AI 搜尋完成！請點擊彈出視窗裡的片名加入資料庫'
                    msgClass.value = 'success-msg text-center'
                } else {
                    msg.value = 'AI 未能找到符合條件的韓影，請重新搜尋'
                    msgClass.value = 'error-msg text-center'
                }
            }
        } catch (err) {
            console.error(err)
            msg.value = 'AI 搜尋時發生錯誤（請確認後端已啟動，且 AI 設定已完成）'
            msgClass.value = 'error-msg text-center'
        } finally {
            loading.value = false
        }
    }

    function backToMovieList() {
        router.push({ name: 'MoviePage', query: {} })
    }
</script>

<template>
    <div>
        <!-- AI Search Result Modal -->
        <AiMovieSearchResultModal 
            v-if="showSearchModal"
            :searchResults="searchResults"
            @close="closeSearchModal"
        />

        <h2>AI 搜尋韓影（依條件）</h2>

        <transition name="fade" mode="out-in">
            <div v-if="loading" key="loading">
                <Spinner />
            </div>

            <div v-else key="form">
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <label for="movieIncludeString" class="form-label">片名包含的字串</label>
                        <input v-model="movieIncludeString" id="movieIncludeString" class="form-control form-text-field" name="movieIncludeString" placeholder="例如：犯罪, 殺手, 愛情">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="actorName1" class="form-label">演員姓名 1</label>
                        <input v-model="actorName1" id="actorName1" class="form-control form-text-field" name="actorName1" placeholder="例如：宋康昊">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="actorName2" class="form-label">演員姓名 2</label>
                        <input v-model="actorName2" id="actorName2" class="form-control form-text-field" name="actorName2" placeholder="例如：全智賢">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="actorName3" class="form-label">演員姓名 3</label>
                        <input v-model="actorName3" id="actorName3" class="form-control form-text-field" name="actorName3" placeholder="例如：馬東石">
                    </p>

                    <p class="form-group form-text-p">
                        <label for="characterName1" class="form-label">角色名稱 1</label>
                        <input v-model="characterName1" id="characterName1" class="form-control form-text-field" name="characterName1" placeholder="例如：金司機">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="characterName2" class="form-label">角色名稱 2</label>
                        <input v-model="characterName2" id="characterName2" class="form-control form-text-field" name="characterName2" placeholder="例如：未知">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="characterName3" class="form-label">角色名稱 3</label>
                        <input v-model="characterName3" id="characterName3" class="form-control form-text-field" name="characterName3" placeholder="例如：未知">
                    </p>

                    <p class="form-group form-text-p">
                        <label for="characterDescription" class="form-label">角色描述</label>
                        <textarea
                            v-model="characterDescription"
                            id="characterDescription"
                            class="form-control form-text-field form-textarea"
                            name="characterDescription"
                            rows="3"
                            placeholder="例如：男主角是退役軍人，個性沉默但很有正義感..."
                        />
                    </p>

                    <p class="form-group form-text-p">
                        <label for="partOfPlot" class="form-label">劇情片段</label>
                        <textarea
                            v-model="partOfPlot"
                            id="partOfPlot"
                            class="form-control form-text-field form-textarea"
                            name="partOfPlot"
                            rows="4"
                            placeholder="例如：一場災難爆發，主角群在密閉空間求生..."
                        />
                    </p>

                    <p class="form-group form-text-p">
                        <label for="genre1" class="form-label">類型 1</label>
                        <input v-model="genre1" id="genre1" class="form-control form-text-field" name="genre1" placeholder="例如：災難 / 動作 / 犯罪 / 恐怖 / 劇情">
                    </p>
                    <p class="form-group form-text-p">
                        <label for="genre2" class="form-label">類型 2</label>
                        <input v-model="genre2" id="genre2" class="form-control form-text-field" name="genre2" placeholder="可留空">
                    </p>

                    <div class="form-group form-text-p">
                        <h6>可在以下平台觀看（可複選）：</h6>
                        <div class="form-control platform-box">
                            <label v-for="p in platforms" :key="p.name" class="platform-item">
                                <input v-model="selectedPlatforms" type="checkbox" :value="p.name">
                                {{ p.name }}
                            </label>
                        </div>
                    </div>

                    <div class="input-msg text-center">
                        送出前請至少填寫：片名包含的字串 / 任一位演員 / 任一個角色 / 角色描述 / 劇情片段（其他欄位可選填）。
                    </div>

                    <div class="text-center">
                        <button
                            @click="startAiImportMovie"
                            type="button"
                            class="btn shadow-none form-btn"
                            :disabled="!canSubmit"
                        >
                            送出
                        </button>
                    </div>
                </form>

                <div :class="msgClass">{{ msg }}</div>

                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
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
    .form-textarea {
        resize: vertical;
        min-height: 90px;
    }
    .form-text-field::placeholder {
        color: #ccc;
        opacity: 0.9;
    }
    .form-textarea::placeholder {
        color: #ccc;
        opacity: 0.9;
    }
    h6 {
        font-weight:normal;
        margin:2px 0px;
    }
    .platform-box {
        margin:15px 0px 15px 0px;
        border:1px $autumn-light solid;
        width:500px;
        padding: 12px 14px;
        background: rgba(255, 255, 255, 0.5);
    }
    .platform-item {
        display: block;
        margin: 6px 0px;
        font-weight: normal;
        color: $autumn-text-dark;
        input {
            margin-right: 10px;
        }
    }
    .form-btn
    {
        background-color:$autumn-light-orange;
        border-color:$autumn-light;
        width:15%;
        font-size:large;
        margin:20px 10px 30px 10px;
        padding:10px 0px;
    }
    .form-btn:hover, .form-btn:active, .form-btn:focus
    {
        background-color:$autumn-light-orange;
        text-decoration:none;
        outline:none;
        color:$autumn-dark-brown;
        width:15%;
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
    .input-msg
    {
        font-weight:normal;
        color:$autumn-dark-orange;
        font-size:large;
        margin:25px 0px;
    }
    .response-wrap {
        margin: 10px 0px 20px 0px;
    }
    .response-title {
        margin: 10px 0px;
        font-weight: normal;
        color: $autumn-dark-brown;
    }
    .response-pre {
        margin: 0px auto;
        width: 90%;
        max-width: 1100px;
        border: 1px $autumn-light solid;
        padding: 12px 14px;
        white-space: pre-wrap;
        word-break: break-word;
        background: rgba(255, 255, 255, 0.6);
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

    /* Transitions */
    .fade-enter-active, .fade-leave-active {
        transition: opacity 0.4s ease;
    }
    .fade-enter-from, .fade-leave-to {
        opacity: 0;
    }
</style>

