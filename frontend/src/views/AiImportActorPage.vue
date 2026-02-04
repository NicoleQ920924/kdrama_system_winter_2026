<script setup>
    import { computed, ref } from 'vue'
    import Spinner from '@/components/Spinner.vue'
    import AiActorSearchResultModal from '@/components/modals/AiActorSearchResultModal.vue'
    import { searchActorsByPrompt } from '@/services/aiService'
    import { useRouter } from 'vue-router'

    const router = useRouter()

    const loading = ref(false)
    const msg = ref('')
    const msgClass = ref('')
    const searchResults = ref([])
    const showSearchModal = ref(false)

    const characterName = ref('')
    const workName = ref('')
    const workDescription = ref('')

    const trimmedCharacterName = computed(() => characterName.value.trim())
    const trimmedWorkName = computed(() => workName.value.trim())
    const trimmedWorkDescription = computed(() => workDescription.value.trim())

    const canSubmit = computed(() => {
        return trimmedCharacterName.value !== '' &&
            (trimmedWorkName.value !== '' || trimmedWorkDescription.value !== '')
    })

    const promptToSend = computed(() => {
        // Prefer Drama/Movie name if provided; otherwise use description
        if (trimmedWorkName.value !== '') {
            return `請搜尋三位韓國演員，他/她有演過 ${trimmedWorkName.value} 這部作品裡的 ${trimmedCharacterName.value}`
        }
        return `請搜尋三位韓國演員，他/她有演過有符合 ${trimmedWorkDescription.value} 描述的作品裡的 ${trimmedCharacterName.value}`
    })

    async function startAiImportActor() {
        if (loading.value) return

        msg.value = ''
        msgClass.value = ''
        searchResults.value = []
        showSearchModal.value = false

        if (trimmedCharacterName.value === '') {
            msg.value = '請先填寫「角色名稱」'
            msgClass.value = 'error-msg text-center'
            return
        }
        if (trimmedWorkName.value === '' && trimmedWorkDescription.value === '') {
            msg.value = '「作品名稱」與「作品描述」請至少填寫一個'
            msgClass.value = 'error-msg text-center'
            return
        }

        loading.value = true
        try {
            const res = await searchActorsByPrompt(promptToSend.value)
            if (res.status === 200) {
                const results = res.data?.results ?? []
                if (results.length > 0) {
                    searchResults.value = results
                    showSearchModal.value = true
                    msg.value = 'AI 搜尋完成！請點擊彈出視窗裡的演員名稱加入資料庫'
                    msgClass.value = 'success-msg text-center'
                } else {
                    msg.value = 'AI 未能找到符合條件的韓國演員，請重新搜尋'
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

    function backToActorList() {
        router.push({ name: 'ActorPage', query: {} })
    }

    function closeSearchModal() {
        showSearchModal.value = false
    }
</script>

<template>
    <div>
        <!-- AI Search Result Modal -->
        <AiActorSearchResultModal 
            v-if="showSearchModal"
            :searchResults="searchResults"
            @close="closeSearchModal"
        />

        <h2>AI 搜尋演員（依角色）</h2>

        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="loading" key="loading">
                <Spinner />
            </div>

            <!-- Not Loading -->
            <div v-else key="form">
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <label for="characterName" class="form-label">角色名稱</label>
                        <input
                            v-model="characterName"
                            id="characterName"
                            class="form-control form-text-field"
                            name="characterName"
                            placeholder="例如：成德善 / 金尚宮 / 崔澤"
                            required
                            aria-required="true"
                        >
                    </p>

                    <p class="form-group form-text-p">
                        <label for="workName" class="form-label">韓劇 / 韓影名稱</label>
                        <input
                            v-model="workName"
                            id="workName"
                            class="form-control form-text-field"
                            name="workName"
                            placeholder="例如：請回答1988 / 來自星星的你 / 釜山行"
                        >
                    </p>

                    <p class="form-group form-text-p">
                        <label for="workDescription" class="form-label">韓劇 / 韓影描述（可取代作品名稱）</label>
                        <textarea
                            v-model="workDescription"
                            id="workDescription"
                            class="form-control form-text-field form-textarea"
                            name="workDescription"
                            placeholder="例如：一部以校園霸凌、復仇為主題的韓劇..."
                            rows="4"
                        />
                    </p>

                    <div class="input-msg text-center">
                        送出前請確認：必填「角色名稱」，且「作品名稱 / 作品描述」至少填一個（若兩者都有填，會優先用作品名稱）。
                    </div>

                    <div class="text-center">
                        <button
                            @click="startAiImportActor"
                            type="button"
                            class="btn shadow-none form-btn"
                            :disabled="!canSubmit"
                        >
                            送出
                        </button>
                    </div>
                </form>

                <div :class="msgClass">{{ msg }}</div>

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
    .form-textarea {
        resize: vertical;
        min-height: 120px;
    }
    .form-text-field::placeholder {
        color: #ccc;
        opacity: 0.9;
    }
    .form-textarea::placeholder {
        color: #ccc;
        opacity: 0.9;
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

