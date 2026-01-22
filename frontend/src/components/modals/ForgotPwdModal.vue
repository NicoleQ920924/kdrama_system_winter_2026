<!-- Forgot Password Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { ref, onUnmounted, defineEmits } from 'vue'

    const errorMsg = ref('')
    const idSent = ref(false)
    const inputId = ref('')
    const testEmail = ref('test@evilroot.com')

    const emit = defineEmits (['close', 'switch'])

    onUnmounted(() => {
        errorMsg.value = ''
        inputId.value = ''
        idSent.value = false
    })

    function validateSentId() {
        const idPattern = /^[a-zA-Z0-9]{1,}$/;
        if (!idPattern.test(inputId.value)) {
            errorMsg.value = "帳號不可為空值!";
        }
        else {
            idSent.value = true;
        }
    }
</script>
<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="forgotPwdModalLabel" style="display:block;">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="forgotPwdModalLabel">忘記密碼</h3>
                        <button type="button" class="btn-close" @click="emit('close')" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div v-if="idSent">
                            <div>
                                <h4 id="forgotPwdPopupMsg1" class="text-dark text-center">
                                    我們已經寄一封電子郵件到<br>
                                    <span id="email2">{{ testEmail }}</span><br>
                                    請點選電子郵件中的連結來重設密碼
                                </h4>
                                <h4 id="forgotPwdPopupMsg2" class="text-dark text-center"></h4>
                            </div>
                        </div>
                        <div v-else id="forgotPwdForm">
                            <h5 class="text-dark text-center">輸入您的帳號</h5>
                            <form class="form" method="post" action="#">
                                <input type="hidden" name="redirectUrl" value="">
                                <p class="form-group modal-text-p">
                                    <input v-model="inputId" type="text" class="form-control modal-text-field" id="forgotId" name="forgotId" placeholder="請輸入您的學號" required aria-required="true">
                                </p>
                                <span class="error-msg"></span>
                                <div class="text-center">
                                    <button type="button" @click="validateSentId" class="btn modal-btn shadow-none">確定</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div> 
        </div>
    </Teleport>
</template>