<!-- Confirm Password (Before Reset) Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { defineEmits, ref, onUnmounted } from 'vue'
    const emit = defineEmits(['switch', 'close'])

    const showPassword = ref(false)

    const errorMsg = ref('')
    const redirectUrl = ref('')

    onUnmounted(() => {
        errorMsg.value = ''
    })

    function submitRedirectUrlUponConfirmPwd() {
        /* redirectUrl.value = window.location.href;
        if (localStorage.getItem('stayLogin') === 'false') {
            window.onbeforeunload = () => {
                sessionStorage.setItem('justReload', 'true');
                localStorage.setItem('stayLogin', 'true');
            };
        } */
        emit('switch', 'reset');
    }
</script>
<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="confirmPwdModalLabel" style="display:block;">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="confirmPwdModalLabel">確認密碼</h3>
                        <button type="button" class="btn-close" @click="emit('close')" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div id="confirmPwdForm">
                            <h5 class="text-dark text-center">輸入您現在的密碼</h5>
                            <form class="form" method="post" action="#">
                                <input :value="redirectUrl" type="hidden" name="redirectUrl">
                                <input type="hidden" id="confirmId" name="confirmId" value="">
                                <p class="form-group modal-text-p">
                                    <input :type="showPassword ? 'text' : 'password'" class="form-control modal-text-field" id="confirmPwdToReset" name="confirmPwdToReset" placeholder="請輸入您現在的密碼" required aria-required="true">
                                    <font-awesome-icon class="text-field-eye-icon" :icon="showPassword ? 'fa-eye-slash' : 'fa-eye'" @click="showPassword = !showPassword" />
                                </p>
                                <span class="error-msg">{{ errorMsg }}</span>
                                <div class="text-center">
                                    <button @click="submitRedirectUrlUponConfirmPwd" type="button" class="btn modal-btn shadow-none">確定</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>