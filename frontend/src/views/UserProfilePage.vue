<!-- User Profile Page -->
<script setup>
    import { ref, onMounted, computed } from 'vue'
    import { userStore } from '@/store'
    import Spinner from '@/components/Spinner.vue'

    const user = computed(() => userStore.getCurrentUser())
    const isLoading = ref(false)
    const watchedDramas = ref([])
    const watchedMovies = ref([])
    const displayNameMode = ref('chineseName')

    onMounted(() => {
        if (user.value) {
            loadUserData()
        }
    })

    const loadUserData = async () => {
        isLoading.value = true
        try {
            if (user.value) {
                const response = await getUserWithWatchlist(user.value.userId)
                watchedDramas.value = response.data.watchedDramas || []
                watchedMovies.value = response.data.watchedMovies || []
            }
        } catch (error) {
            console.error('Error loading user data:', error)
        } finally {
            isLoading.value = false
        }
    }

    const getUserWithWatchlist = async (userId) => {
        // This will call the backend to get user with watchlist
        const axios = (await import('axios')).default
        return axios.get(`http://localhost:8080/api/users/${userId}`)
    }

    const getDisplayName = (item) => {
        if (displayNameMode.value === 'chineseName') {
            return item.chineseName || item.name
        } else if (displayNameMode.value === 'koreanName') {
            return item.koreanName || item.name
        }
        return item.englishName || item.name
    }
</script>

<template>
    <div class="user-profile-page container mt-5">
        <div v-if="!user" class="alert alert-warning text-center">
            <p>請先登入以查看您的個人檔案</p>
        </div>

        <template v-else>
            <!-- User Info Section -->
            <div class="user-info-section mb-5">
                <div class="card">
                    <div class="card-body">
                        <h2 class="card-title">{{ user.displayName }}</h2>
                        <p class="card-text">
                            <strong>帳號:</strong> {{ user.username }}
                        </p>
                        <p class="card-text">
                            <strong>身份:</strong> 
                            <span v-if="user.role === 'ADMIN'" class="badge bg-danger">管理員</span>
                            <span v-else class="badge bg-primary">一般用戶</span>
                        </p>
                    </div>
                </div>
            </div>

            <!-- Watchlist Section -->
            <div class="watchlist-section">
                <h3 class="mb-4">我的追蹤清單</h3>

                <!-- Display Mode Toggle -->
                <div class="mb-3">
                    <label for="displayMode" class="form-label">顯示名稱:</label>
                    <select v-model="displayNameMode" id="displayMode" class="form-select w-25">
                        <option value="chineseName">繁體中文名</option>
                        <option value="koreanName">韓文名</option>
                        <option value="englishName">英文名</option>
                    </select>
                </div>

                <!-- Loading State -->
                <div v-if="isLoading" class="text-center">
                    <Spinner />
                </div>

                <!-- Dramas Section -->
                <div v-else-if="watchedDramas.length > 0" class="mb-5">
                    <h4>我的韓劇 ({{ watchedDramas.length }}部)</h4>
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>劇名</th>
                                    <th>狀態</th>
                                    <th>主演演員</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="drama in watchedDramas" :key="drama.dramaId">
                                    <td>
                                        <router-link :to="{ name: 'DramaPage', query: { id: drama.dramaId } }">
                                            {{ getDisplayName(drama) }}
                                        </router-link>
                                    </td>
                                    <td>{{ drama.status }}</td>
                                    <td>{{ drama.leadActors[0] }}, {{ drama.leadActors[1] }}, {{ drama.leadActors[2] }}, {{ drama.leadActors[3] }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div v-else class="alert alert-info">
                    <p>您還沒有追蹤任何韓劇</p>
                </div>

                <!-- Movies Section -->
                <div v-if="watchedMovies.length > 0" class="mb-5">
                    <h4>我的韓影 ({{ watchedMovies.length }}部)</h4>
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>影名</th>
                                    <th>主演演員</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="movie in watchedMovies" :key="movie.movieId">
                                    <td>
                                        <router-link :to="{ name: 'MoviePage', query: { id: movie.movieId } }">
                                            {{ getDisplayName(movie) }}
                                        </router-link>
                                    </td>
                                    <td>{{ movie.leadActors[0] }}, {{ movie.leadActors[1] }}, {{ movie.leadActors[2] }}, {{ movie.leadActors[3] }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div v-else class="alert alert-info">
                    <p>您還沒有追蹤任何韓影</p>
                </div>
            </div>
        </template>
    </div>
</template>

<style lang="scss" scoped>
    .user-profile-page {
        min-height: 70vh;
        padding: 20px 0;

        .user-info-section {
            .card {
                border-top: 6px solid #8b6f47;
            }
        }

        .watchlist-section {
            h3 {
                color: #4a3728;
                border-bottom: 3px solid #c5a882;
                padding-bottom: 10px;
            }

            h4 {
                color: #5c4a35;
                margin-top: 30px;
            }

            table {
                a {
                    color: #8b5a3c;
                    text-decoration: none;

                    &:hover {
                        color: #5c3d27;
                        text-decoration: underline;
                    }
                }
            }
        }
    }
</style>
