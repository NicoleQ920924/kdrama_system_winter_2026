<!-- Login Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { ref, defineEmits, onUnmounted } from 'vue'

    const emit = defineEmits(['close', 'switch'])

    const showPassword = ref(false)
    const errorMsg = ref('')
    const redirectUrl = ref('')
    const activeTab = ref('user') // user or admin

    const userId = ref('')
    const userPwd = ref('')
    const adminId = ref('')
    const adminPwd = ref('')

    onUnmounted(() => {
        errorMsg.value = ''
        showPassword.value = false
        activeTab.value = 'user'

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

        // Reset Inputs
        if (activeTab.value === 'user') {
            userId.value = ''
            userPwd.value = ''
        } else {
            adminId.value = ''
            adminPwd.value = ''
        }
    }

    function submitLogin() {
        /* emit('loginSuccess', { 
            redirectUrl: redirectUrl.value,
            loginType: activeTab.value
        }) */
        emit('switch', 'loginSuccess')
        /* if (activeTab.value === 'user') {
            // Normal User Login Logic
        } else {
            // Admin Login Logic
        } */
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
                                    <input type="hidden" :value="redirectUrl" name="redirectUrl" />
                                    <input type="hidden" value="user" name="loginType" />
                                    <p class="modal-text-p">
                                        <input v-model="userId" type="text" class="form-control modal-text-field" name="loginId" placeholder="帳號" required />
                                    </p>
                                    <p class="modal-text-p">
                                        <input v-model="userPwd" :type="showPassword ? 'text' : 'password'" class="form-control modal-text-field" name="loginPwd" placeholder="密碼" required/>
                                        <font-awesome-icon class="text-field-eye-icon" :icon="showPassword ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword = !showPassword" />
                                    </p>
                                    <span class="error-msg">{{ errorMsg }}</span>
                                    <div class="form-check stay-login">
                                        <label class="form-check-label" for="stayLoginUser">保持登入</label>
                                        <input type="checkbox" class="form-check-input" id="stayLoginUser" name="stayLogin" />
                                    </div>
                                    <div class="text-center">
                                        <button @click.prevent="submitLogin('user')" type="button" class="btn modal-btn shadow-none">一般用戶登入</button>
                                    </div>
                                    </form>
                                </div>
                            </template>

                            <template v-else>
                                <div class="tab-pane active">
                                    <form class="form" method="post" action="#">
                                    <input type="hidden" :value="redirectUrl" name="redirectUrl" />
                                    <input type="hidden" value="admin" name="loginType" />
                                    <p class="modal-text-p">
                                        <input v-model="adminId" type="text" class="form-control modal-text-field" name="loginId" placeholder="帳號" required />
                                    </p>
                                    <p class="modal-text-p">
                                        <input v-model="adminPwd" :type="showPassword ? 'text' : 'password'" class="form-control modal-text-field" name="loginPwd" placeholder="密碼" required/>
                                        <font-awesome-icon class="text-field-eye-icon" :icon="showPassword ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword = !showPassword" />
                                    </p>
                                    <span class="error-msg">{{ errorMsg }}</span>
                                    <div class="form-check stay-login">
                                        <label class="form-check-label" for="stayLoginAdmin">保持登入</label>
                                        <input type="checkbox" class="form-check-input" id="stayLoginAdmin" name="stayLogin" />
                                    </div>
                                    <div class="text-center">
                                        <button @click.prevent="submitLogin('admin')" type="button" class="btn modal-btn shadow-none">管理員登入</button>
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