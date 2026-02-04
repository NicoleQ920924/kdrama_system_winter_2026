<!-- AI Drama Search Result Modal -->
<!-- Used in AiImportDramaPage.vue -->
<!-- Similar to FinishRegistrationModal.vue design -->
<script setup>
    import { defineProps, defineEmits, ref } from 'vue'
    import { useRouter } from 'vue-router'
    import { importDrama } from '@/services/dramaService'

    const router = useRouter()

    const props = defineProps({
        searchResults: {
            type: Array,
            required: true
        }
    })

    const emit = defineEmits(['close'])

    const loadingDramas = ref([]) // for handleDramaClick()
    const addedDramas = ref({}) // maps dramaTitle to dramaId

    const loadedDramaId = ref('')
    const loading = ref(false)

    async function handleDramaClick(dramaTitle) {
        // If already added, navigate to the drama page
        if (addedDramas.value[dramaTitle]) {
            router.push({ name: 'DramaPage', query: { id: addedDramas.value[dramaTitle] } })
            return
        }

        // Return while loading
        if (loadingDramas.value.includes(dramaTitle) || loading.value) return;

        // Add loading status
        loadingDramas.value.push(dramaTitle)

        try {
            // Call the import endpoint with the drama title
            const response = await importDrama(dramaTitle)
            if (response.status === 200) {
                // Store the drama ID so the link becomes clickable
                alert(`成功將 ${dramaTitle} 加入資料庫！`)
                loadedDramaId.value = response.data.dramaId
                addedDramas.value[dramaTitle] = loadedDramaId.value
                // Don't emit 'close' - let user click the transformed link or close with X button
            }
        } catch (error) {
            console.error('Error importing drama:', error)
            if (error.response && error.response.status === 409) {
                // Drama already exists - treat it as added
                alert(`${dramaTitle} 已存在資料庫中，請至韓劇列表頁面查看`)
            } else {
                alert(`加入 ${dramaTitle} 時發生錯誤，請稍後重試`)
            }
        } finally {
            // Remove loading status
            const index = loadingDramas.value.indexOf(dramaTitle)
            if (index !== -1) loadingDramas.value.splice(index, 1)
        }
    }
</script>

<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="aiSearchResultModalLabel" style="display:block;">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="aiSearchResultModalLabel">AI 搜尋結果</h3>
                        <button type="button" class="btn-close" @click="emit('close')" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="ai-result-content">
                            <h4 class="result-intro">根據您填入的資料，AI搜尋的結果是</h4>
                            
                            <ol class="result-list">
                                <li v-for="(drama, index) in searchResults" :key="index" class="result-item">
                                    <!-- Button before drama is added -->
                                    <button 
                                        v-if="!addedDramas[drama.name]"
                                        class="drama-title-btn border-0"
                                        @click="handleDramaClick(drama.name)"
                                        :disabled="loadingDramas.includes(drama.name)"
                                    >
                                        {{ drama.name }}
                                    </button>

                                    <!-- Link after drama is added -->
                                    <router-link 
                                        v-else
                                        :to="{ name: 'DramaPage', query: { id: addedDramas[drama.name] } }"
                                        class="drama-title-link"
                                    >
                                        {{ drama.name }}
                                    </router-link>

                                    <span class="drama-summary">({{ drama.summary }})</span>
                                </li>
                            </ol>

                            <div class="result-footer">
                                <p v-if="addedDramas === null || Object.keys(addedDramas).length === 0">可以點擊連結將此韓劇加入資料庫</p>
                                <p v-else>新增成功或者是資料庫已經有此韓劇時，可以點擊連結前往該韓劇頁面</p>
                                <p>AI有時會出錯，導致劇名無法透過TMDB搜尋，若遇到此狀況，煩請利用其他方法新增至資料庫</p>
                                <p>若想重新搜尋，請關閉此視窗，並再填入一次搜尋表單</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>

<style lang="scss" scoped>
    @import '@/styles/autumn_theme.scss';

    .ai-result-content {
        padding: 10px 0;
    }

    .result-intro {
        text-align: center;
        color: $autumn-dark-brown;
        font-weight: normal;
        margin-bottom: 20px;
    }

    .result-list {
        list-style-position: inside;
        padding-left: 0;
        margin: 0;
        text-align: center;
    }

    .result-item {
        margin-bottom: 15px;
        color: $autumn-text-dark;
        line-height: 1.6;

        .drama-title-btn {
            background: none;
            padding: 0;
            margin: 0;
            color: $autumn-dark-orange;
            text-decoration: underline;
            cursor: pointer;
            font-size: 1em;
            font-weight: 500;

            &:hover:not(:disabled) {
                color: $autumn-dark-brown;
                text-decoration-color: $autumn-dark-brown;
            }

            &:active:not(:disabled) {
                color: $autumn-light-orange;
            }

            &:disabled {
                opacity: 0.6;
                cursor: not-allowed;
            }
        }

        .drama-title-link {
            background: none;
            padding: 0;
            margin: 0;
            color: $autumn-dark-orange;
            text-decoration: underline;
            font-size: 1em;
            font-weight: 500;

            &:hover {
                color: $autumn-dark-brown;
                text-decoration-color: $autumn-dark-brown;
            }

            &:active {
                color: $autumn-light-orange;
            }
        }

        .drama-summary {
            margin-left: 5px;
            color: $autumn-dark-brown;
            font-size: 0.95em;
        }
    }

    .result-footer {
        text-align: center;
        margin-top: 20px;
        padding-top: 15px;
        border-top: 1px solid $autumn-light;
        color: $autumn-dark-orange;
        font-size: 0.95em;
    }

    /* Modal styling */
    .modal-backdrop {
        background-color: rgba(0, 0, 0, 0.5);
    }

    .modal-dialog {
        margin-top: 5%;
    }

    .modal-content {
        border: 1px solid $autumn-light;
        background-color: rgba(255, 250, 245, 0.95);
    }

    .modal-header {
        border-bottom: 1px solid $autumn-light;
        background-color: transparent;

        .modal-title {
            color: $autumn-dark-brown;
            font-weight: 600;
        }

        .btn-close {
            opacity: 0.7;

            &:hover {
                opacity: 1;
            }
        }
    }

    .modal-body {
        padding: 25px;
    }
</style>
