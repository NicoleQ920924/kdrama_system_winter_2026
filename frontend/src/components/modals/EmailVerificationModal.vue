<!-- Email Verification Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { ref, defineEmits } from 'vue'

    const testEmail = ref('test@evilroot.com')

    const emit = defineEmits (['close', 'switch'])

    function finishRegister(){
        emit('switch', 'finishRegistration')
    }
</script>

<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="registerEmailModalLabel" style="display:block;">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="registerEmailModalLabel">驗證信箱</h3>
                        <button type="button" class="btn-close" @click="finishRegister" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div>
                            <h4 class="normal-dark-text text-center">
                                我們已經寄一封電子郵件到<br>
                                <span id="email1">{{ testEmail }}</span><br>
                                請點選電子郵件中的連結登入
                            </h4>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>