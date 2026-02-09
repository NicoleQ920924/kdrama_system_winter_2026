<!-- Login Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { ref, defineEmits, onUnmounted } from 'vue'
    import { userStore } from '@/store'
    import { loginUser } from '@/services/userService'

    const emit = defineEmits(['close', 'switch'])

    const showPassword = ref(false)
    const errorMsg = ref('')
    const loading = ref(false)
    const activeTab = ref('user') // user or admin

    const userId = ref('')
    const userPwd = ref('')
    const adminId = ref('')
    const adminPwd = ref('')

    onUnmounted(() => {
        errorMsg.value = ''
        showPassword.value = false
        activeTab.value = 'user'
        loading.value = false

        userId.value = ''
        userPwd.value = ''
        adminId.value = ''
        adminPwd.value = ''
    })

    function goToForgotPwdModal() {
        emit('switch', 'forgot')
    }

    function goToRegisterModal() {
        emit('switch', 'register')
    }

    function switchTab(tab) {
        activeTab.value = tab
        errorMsg.value = ''

        // Reset Inputs
        if (activeTab.value === 'user') {
            userId.value = ''
            userPwd.value = ''
        } else {
            adminId.value = ''
            adminPwd.value = ''
        }
    }

    async function submitLogin(type) {
        errorMsg.value = ''
        loading.value = true

        try {
            let username = ''
            let password = ''
            
            if (type === 'user') {
                if (!userId.value.trim()) {
                    errorMsg.value = '請輸入帳號'
                    loading.value = false
                    return
                }
                username = userId.value
                password = userPwd.value || userId.value // Use username as default password if empty for regular users
            } else {
                if (!adminId.value.trim()) {
                    errorMsg.value = '請輸入帳號'
                    loading.value = false
                    return
                }
                if (!adminPwd.value.trim()) {
                    errorMsg.value = '請輸入密碼'
                    loading.value = false
                    return
                }
                username = adminId.value
                password = adminPwd.value
            }

            // Login only (no auto-registration)
            try {
                const response = await loginUser(username, password)
                if (response && response.data) {
                    userStore.setCurrentUser(response.data)
                    emit('switch', 'loginSuccess')
                    return
                }
                errorMsg.value = '登入失敗，請重試'
            } catch (err) {
                if (err.response && err.response.status === 401) {
                    errorMsg.value = '帳號或密碼錯誤'
                } else if (err.response && err.response.status === 404) {
                    errorMsg.value = '帳號不存在'
                } else {
                    errorMsg.value = '登入失敗，請稍後重試'
                }
            }
        } catch (err) {
            console.error('Login error:', err)
            errorMsg.value = '登入失敗，請稍後重試'
        } finally {
            loading.value = false
        }
    }
</script>

<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="loginModalLabel" style="display:block;">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="loginModalLabel">登入</h3>
                        <button type="button" class="btn-close" @click="emit('close')" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <!-- Nav Tabs: By ChatGPT -->
                        <ul class="nav nav-tabs justify-content-center mb-3" id="loginTab" role="tablist">
                            <li class="nav-item" role="presentation">
                                <button @click="switchTab('user')" :class="['nav-link', { active: activeTab === 'user' }]" id="user-tab" data-bs-toggle="tab" data-bs-target="#user-login" type="button" role="tab">一般用戶登入</button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button @click="switchTab('admin')" :class="['nav-link', { active: activeTab === 'admin' }]" id="admin-tab" data-bs-toggle="tab" data-bs-target="#admin-login" type="button" role="tab">管理員登入</button>
                            </li>
                        </ul>

                        <!-- Tab Panes: Controlled by v-if and v-else -->
                        <transition name="fade" mode="out-in">
                            <template v-if="activeTab === 'user'">
                                <div class="tab-pane active">
                                    <form class="form" method="post" action="#">
                                    <p class="modal-text-p">
                                        <input v-model="userId" type="text" class="form-control modal-text-field" name="loginId" placeholder="帳號" required :disabled="loading" />
                                    </p>
                                    <p class="modal-text-p">
                                        <input v-model="userPwd" :type="showPassword ? 'text' : 'password'" class="form-control modal-text-field" name="loginPwd" placeholder="密碼 (可選)" :disabled="loading" />
                                        <font-awesome-icon class="text-field-eye-icon" :icon="showPassword ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword = !showPassword" />
                                    </p>
                                    <span class="error-msg">{{ errorMsg }}</span>
                                    <div class="text-center">
                                        <button @click.prevent="submitLogin('user')" type="button" class="btn modal-btn shadow-none" :disabled="loading">
                                            {{ loading ? '登入中...' : '一般用戶登入' }}
                                        </button>
                                    </div>
                                    </form>
                                </div>
                            </template>

                            <template v-else>
                                <div class="tab-pane active">
                                    <form class="form" method="post" action="#">
                                    <p class="modal-text-p">
                                        <input v-model="adminId" type="text" class="form-control modal-text-field" name="loginId" placeholder="帳號" required :disabled="loading" />
                                    </p>
                                    <p class="modal-text-p">
                                        <input v-model="adminPwd" :type="showPassword ? 'text' : 'password'" class="form-control modal-text-field" name="loginPwd" placeholder="密碼 (必填)" required :disabled="loading" />
                                        <font-awesome-icon class="text-field-eye-icon" :icon="showPassword ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword = !showPassword" />
                                    </p>
                                    <span class="error-msg">{{ errorMsg }}</span>
                                    <div class="text-center">
                                        <button @click.prevent="submitLogin('admin')" type="button" class="btn modal-btn shadow-none" :disabled="loading">
                                            {{ loading ? '登入中...' : '管理員登入' }}
                                        </button>
                                    </div>
                                    </form>
                                </div>
                            </template>
                        </transition>
                        <p class="text-center">
                            <a @click="goToForgotPwdModal" href="#">忘記密碼?</a>
                            <span class="vl"></span>
                            <a @click="goToRegisterModal" href="#" id="registerLink">點我註冊</a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>