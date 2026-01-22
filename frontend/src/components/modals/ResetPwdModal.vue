<!-- Reset Password Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { defineEmits, ref, onUnmounted } from 'vue'
    const emit = defineEmits(['switch', 'close'])

    const showPassword1 = ref(false)
    const showPassword2 = ref(false)

    const errorMsg = ref('')
    const inputText = ref('')

    const resetId = ref('')
    const resetPwd = ref('')
    const confirmResetPwd = ref('')
    const redirectUrl = ref('')

    onUnmounted(() => {
        errorMsg.value = ''
        inputText.value = ''
    })

    function validateResetPwd() {
        const pwdPattern = /^[a-zA-Z0-9]{6,12}$/;
        if (!pwdPattern.test(resetPwd.value)) {
            errorMsg.value = "密碼須為6~12字元!";
        } 
        else if (resetPwd.value !== confirmResetPwd.value) {
            errorMsg.value = "您兩次輸入的密碼不一致!";
        } 
        else {
            // redirectUrl.value = window.location.href;
            emit('switch', 'finishReset');
        }
    }
</script>
<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="resetPwdModalLabel" style="display:block;">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="resetPwdModalLabel">重設密碼</h3>
                        <button type="button disabled" class="btn-close" @click="emit('close')" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <h5 class="normal-dark-text text-center">請重新輸入您的密碼</h5>
                        <form class="form" method="post" action="#">
                            <p class="form-group modal-text-p">
                                <input v-model="resetPwd" :type="showPassword1 ? 'text' : 'password'" class="form-control modal-text-field" name="resetPwd" placeholder="請輸入您的密碼 (6~12字元)" required aria-required="true">
                                <font-awesome-icon class="text-field-eye-icon" :icon="showPassword1 ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword1 = !showPassword1" />
                            </p>
                            <p class="form-group modal-text-p">
                                <input v-model="confirmResetPwd" :type="showPassword2 ? 'text' : 'password'" class="form-control modal-text-field" name="confirmResetPwd" placeholder="再次輸入密碼 (6~12字元)" required aria-required="true">
                                <font-awesome-icon class="text-field-eye-icon" :icon="showPassword2 ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword2 = !showPassword2" />
                            </p>
                            <span class="error-msg">{{ errorMsg }}</span>
                            <input type="hidden" :value="resetId" name="resetId">
                            <input type="hidden" :value="redirectUrl" name="redirectUrl">
                            <div class="text-center">
                                <button @click="validateResetPwd" type="button" id="resetSubmit" class="btn modal-btn shadow-none">完成</button>
                            </div>
                        </form> 
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>