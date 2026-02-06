<!-- Watchlist Page - Dedicated page for viewing watchlist -->
<script setup>
    import { ref, onMounted } from 'vue'
    import { userStore } from '@/store'
    import { removeMovieFromWatchlist, removeDramaFromWatchlist } from '@/services/watchlistService'
    import Spinner from '@/components/Spinner.vue'

    const user = ref(null)
    const isLoading = ref(false)
    const watchedDramas = ref([])
    const watchedMovies = ref([])
    const activeTab = ref('dramas')
    const displayNameMode = ref('chineseName')
    const sortBy = ref('name')

    onMounted(() => {
        user.value = userStore.getCurrentUser()
        if (user.value) {
            loadWatchlist()
        }
    })

    const loadWatchlist = async () => {
        isLoading.value = true
        try {
            if (user.value) {
                const response = await getUserWithWatchlist(user.value.userId)
                watchedDramas.value = response.data.watchedDramas || []
                watchedMovies.value = response.data.watchedMovies || []
                sortItems()
            }
        } catch (error) {
            console.error('Error loading watchlist:', error)
        } finally {
            isLoading.value = false
        }
    }

    const getUserWithWatchlist = async (userId) => {
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

    const sortItems = () => {
        if (sortBy.value === 'name') {
            watchedDramas.value.sort((a, b) => getDisplayName(a).localeCompare(getDisplayName(b)))
            watchedMovies.value.sort((a, b) => getDisplayName(a).localeCompare(getDisplayName(b)))
        } else if (sortBy.value === 'date') {
            watchedDramas.value.sort((a, b) => new Date(b.airDate) - new Date(a.airDate))
            watchedMovies.value.sort((a, b) => new Date(b.releaseDate) - new Date(a.releaseDate))
        }
    }

    const removeDrama = async (dramaId) => {
        if (confirm('確認要移除此韓劇?')) {
            try {
                await removeDramaFromWatchlist(user.value.userId, dramaId)
                watchedDramas.value = watchedDramas.value.filter(d => d.dramaId !== dramaId)
            } catch (error) {
                console.error('Error removing drama from watchlist:', error)
                alert('移除失敗，請稍後重試')
            }
        }
    }

    const removeMovie = async (movieId) => {
        if (confirm('確認要移除此韓影?')) {
            try {
                await removeMovieFromWatchlist(user.value.userId, movieId)
                watchedMovies.value = watchedMovies.value.filter(m => m.movieId !== movieId)
            } catch (error) {
                console.error('Error removing movie from watchlist:', error)
                alert('移除失敗，請稍後重試')
            }
        }
    }

    const switchTab = (tab) => {
        activeTab.value = tab
    }
</script>

<template>
    <div class="watchlist-page container mt-5">
        <div v-if="!user" class="alert alert-warning text-center">
            <p>請先登入以查看您的追蹤清單</p>
        </div>

        <template v-else>
            <h2 class="page-title mb-4">我的追蹤清單</h2>

            <!-- Controls -->
            <div class="controls-section mb-4">
                <div class="row">
                    <div class="col-md-6">
                        <label for="displayMode" class="form-label">顯示名稱:</label>
                        <select v-model="displayNameMode" id="displayMode" class="form-select">
                            <option value="chineseName">繁體中文名</option>
                            <option value="koreanName">韓文名</option>
                            <option value="englishName">英文名</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="sortBy" class="form-label">排序方式:</label>
                        <select v-model="sortBy" @change="sortItems" id="sortBy" class="form-select">
                            <option value="name">名稱</option>
                            <option value="date">日期</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- Tabs -->
            <ul class="nav nav-tabs mb-4">
                <li class="nav-item">
                    <button 
                        @click="switchTab('dramas')" 
                        :class="['nav-link', { active: activeTab === 'dramas' }]"
                    >
                        韓劇 ({{ watchedDramas.length }})
                    </button>
                </li>
                <li class="nav-item">
                    <button 
                        @click="switchTab('movies')" 
                        :class="['nav-link', { active: activeTab === 'movies' }]"
                    >
                        韓影 ({{ watchedMovies.length }})
                    </button>
                </li>
            </ul>

            <!-- Loading State -->
            <div v-if="isLoading" class="text-center">
                <Spinner />
            </div>

            <!-- Dramas Tab -->
            <template v-else-if="activeTab === 'dramas'">
                <div v-if="watchedDramas.length > 0" class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light">
                            <tr>
                                <th>劇名</th>
                                <th>狀態</th>
                                <th>製作國家</th>
                                <th>播出日期</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="drama in watchedDramas" :key="drama.dramaId">
                                <td>
                                    <router-link :to="{ name: 'DramaDetails', params: { id: drama.dramaId } }">
                                        {{ getDisplayName(drama) }}
                                    </router-link>
                                </td>
                                <td>
                                    <span :class="['badge', getStatusBadgeClass(drama.status)]">
                                        {{ drama.status }}
                                    </span>
                                </td>
                                <td>{{ drama.country }}</td>
                                <td>{{ drama.airDate }}</td>
                                <td>
                                    <button 
                                        @click="removeDrama(drama.dramaId)" 
                                        class="btn btn-sm btn-danger"
                                    >
                                        移除
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div v-else class="alert alert-info">
                    您還沒有追蹤任何韓劇。
                    <router-link :to="{ name: 'DramaPage' }">瀏覽韓劇</router-link>
                </div>
            </template>

            <!-- Movies Tab -->
            <template v-else-if="activeTab === 'movies'">
                <div v-if="watchedMovies.length > 0" class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light">
                            <tr>
                                <th>影名</th>
                                <th>製作國家</th>
                                <th>上映日期</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="movie in watchedMovies" :key="movie.movieId">
                                <td>
                                    <router-link :to="{ name: 'MovieDetails', params: { id: movie.movieId } }">
                                        {{ getDisplayName(movie) }}
                                    </router-link>
                                </td>
                                <td>{{ movie.country }}</td>
                                <td>{{ movie.releaseDate }}</td>
                                <td>
                                    <button 
                                        @click="removeMovie(movie.movieId)" 
                                        class="btn btn-sm btn-danger"
                                    >
                                        移除
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div v-else class="alert alert-info">
                    您還沒有追蹤任何韓影。
                    <router-link :to="{ name: 'MoviePage' }">瀏覽韓影</router-link>
                </div>
            </template>
        </template>
    </div>
</template>

<script>
const getStatusBadgeClass = (status) => {
    const classes = {
        'AIRING': 'bg-success',
        'FINISHED': 'bg-secondary',
        'ON_HIATUS': 'bg-warning',
        'CANCELLED': 'bg-danger',
        'NOT_AIRED': 'bg-light text-dark'
    }
    return classes[status] || 'bg-secondary'
}
</script>

<style lang="scss" scoped>
    .watchlist-page {
        min-height: 70vh;
        padding: 20px 0;

        .page-title {
            color: #4a3728;
            border-bottom: 3px solid #c5a882;
            padding-bottom: 15px;
        }

        .controls-section {
            background: #f9f7f4;
            padding: 15px;
            border-radius: 8px;
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

            .btn-danger {
                background-color: #a34847;
                border-color: #a34847;

                &:hover {
                    background-color: #8b3a3e;
                }
            }
        }

        .nav-tabs {
            border-bottom: 2px solid #c5a882;

            .nav-link {
                color: #8b5a3c;
                border: none;

                &.active {
                    background-color: transparent;
                    border-bottom: 3px solid #8b5a3c;
                    color: #4a3728;
                    font-weight: bold;
                }

                &:hover {
                    color: #5c3d27;
                }
            }
        }

        .alert {
            a {
                color: #8b5a3c;
            }
        }
    }
</style>
