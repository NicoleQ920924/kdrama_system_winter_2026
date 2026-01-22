<!-- Register Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { defineEmits, ref, onUnmounted } from 'vue'
    import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
    const emit = defineEmits(['switch', 'close'])

    const showPassword1 = ref(false)
    const showPassword2 = ref(false)

    const errorMsg = ref('')
    const redirectUrl = ref('')
    const inputId = ref('')
    const newPwd = ref('')
    const confirmPwd = ref('')

    onUnmounted(() => {
        errorMsg.value = ''
        inputId.value = ''
        newPwd.value = ''
        confirmPwd.value = ''
    })

    function validateRegisterForm() {
        const idPattern = /^[a-zA-Z0-9]{1,}$/;
        const pwdPattern = /^[a-zA-Z0-9]{6,12}$/;

        if (!idPattern.test(inputId.value)) {
            errorMsg.value = "帳號不可為空值!";
        }
        else if (!pwdPattern.test(newPwd.value)) {
            errorMsg.value = "密碼須為6~12字元!";
        } 
        else if (newPwd.value !== confirmPwd.value) {
            errorMsg.value = "您兩次輸入的密碼不一致!";
        } 
        else {
            emit('switch', 'emailVerification');
        }
    }
</script>

<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="registerModalLabel" style="display:block;">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="registerModalLabel">註冊</h3>
                        <button type="button" class="btn-close" @click="emit('close')" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <h5 class="normal-dark-text text-center">輸入您的帳號和密碼來註冊會員</h5>
                        <form class="form" method="post" action="#">
                            <input :value="redirectUrl" type="hidden" name="redirectUrl">
                            <p class="form-group modal-text-p">
                                <input v-model="inputId" type="text" class="form-control modal-text-field" name="inputId" placeholder="請輸入您的帳號" required aria-required="true">
                            </p>
                            <p class="form-group modal-text-p">
                                <input v-model="newPwd" :type="showPassword1 ? 'text' : 'password'" class="form-control modal-text-field" name="newPwd" placeholder="請輸入您的密碼 (6~12字元)" required aria-required="true">
                                <font-awesome-icon class="text-field-eye-icon" :icon="showPassword1 ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword1 = !showPassword1" />
                            </p>
                            <p class="form-group modal-text-p">
                                <input v-model="confirmPwd" :type="showPassword2 ? 'text' : 'password'" class="form-control modal-text-field" name="confirmPwd" placeholder="再次輸入密碼 (6~12字元)" required aria-required="true">
                                <font-awesome-icon class="text-field-eye-icon" :icon="showPassword2 ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword2 = !showPassword2" />
                            </p>
                            <span class="error-msg">{{ errorMsg }}</span>
                            <div class="text-center">
                                <button @click="validateRegisterForm" type="button" class="btn modal-btn shadow-none">註冊</button>
                            </div>
                        </form> 
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>