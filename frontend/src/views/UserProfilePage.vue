<!-- User Profile Page -->
<script setup>
    import { computed } from 'vue'
    import { userStore } from '@/store'

    const user = computed(() => userStore.getCurrentUser())
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
                            <strong>帳號：</strong> {{ user.username }}
                        </p>
                        <p class="card-text">
                            <strong>身份： </strong> 
                            <span v-if="user.role === 'ADMIN'" class="badge bg-danger">管理員</span>
                            <span v-else class="badge bg-primary">一般用戶</span>
                        </p>
                    </div>
                </div>
            </div>
            <router-link v-if="user.role === 'USER'" class="normal-dark-text nav-router-link" :to="{ name: 'WatchlistPage' }" replace>點擊查看追蹤清單</router-link>

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
    }
</style>
