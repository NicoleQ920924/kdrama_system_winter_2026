<!-- Finish Registration Modal -->
<!-- The design is based on my part of 2024 Spring Semester - Web Programming Project, link in references.md -->
<script setup>
    import { defineEmits } from 'vue'

    const emit = defineEmits (['close', 'switch'])
</script>
<template>
    <Teleport to="body">
        <div class="modal-backdrop fade show"></div>
        <div class="modal fade show" tabindex="-1" aria-labelledby="finishRegisterModalLabel" style="display:block;">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title text-center" id="finishRegisterModalLabel">註冊成功</h3>
                        <button type="button" class="btn-close" @click="emit('close')" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div>
                            <h4 id="finishRegisterMsg" class="normal-dark-text text-center">
                                註冊成功!<br><br>
                                請點選右上角的登入按鈕登入
                            </h4>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </Teleport>
</template>