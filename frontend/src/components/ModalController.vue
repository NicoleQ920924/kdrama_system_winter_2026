<!-- Modal Controller -->
<script setup>
    // Import ref
    import { defineProps, defineEmits } from 'vue'

    // Import Modal .vue files' elements
    import LoginModal from './modals/LoginModal.vue';
    import LoginSuccessModal from './modals/LoginSuccessModal.vue';
    import RegisterModal from './modals/RegisterModal.vue';
    import EmailVerificationModal from './modals/EmailVerificationModal.vue';
    import FinishRegistrationModal from './modals/FinishRegistrationModal.vue';
    import ForgotPwdModal from './modals/ForgotPwdModal.vue';
    import ConfirmPwdBeforeResetModal from './modals/ConfirmPwdBeforeResetModal.vue';
    import ResetPwdModal from './modals/ResetPwdModal.vue';
    import FinishResetPwdModal from './modals/FinishResetPwdModal.vue';

    const props = defineProps({
        modalState: String
    })

    const emit = defineEmits(['update:modalState', 'loggedin', 'logout'])

    // Handling modal closure
    function closeModal(modalName) {
        clearErrors()
        if (modalName == 'loginSuccess') {
            emit('loggedin')
        }
        else if (modalName == 'finishReset') {
            emit('logout')
        }
        emit('update:modalState', null)
    }

    // Clear error messages                                
    const clearErrors = () => {
        const errorMsgs = document.querySelectorAll('.errorMsg')
        errorMsgs.forEach(el => el.innerHTML = '')
    }
</script>

<template>
    <div>
        <LoginModal v-if="modalState === 'login'" @switch="emit('update:modalState', $event)" @close="closeModal('login')" />
        <LoginSuccessModal v-if="modalState === 'loginSuccess'" @switch="emit('update:modalState', $event)" @close="closeModal('loginSuccess')" />
        <RegisterModal v-if="modalState === 'register'" @switch="emit('update:modalState', $event)" @close="closeModal('register')" />
        <EmailVerificationModal v-if="modalState === 'emailVerification'" @switch="emit('update:modalState', $event)" @close="closeModal('emailVerification')" />
        <FinishRegistrationModal v-if="modalState === 'finishRegistration'" @switch="emit('update:modalState', $event)" @close="closeModal('finishRegistration')" />
        <ForgotPwdModal v-if="modalState === 'forgot'" @switch="emit('update:modalState', $event)" @close="closeModal('forgot')" />
        <ConfirmPwdBeforeResetModal v-if="modalState === 'beforeReset'" @switch="emit('update:modalState', $event)" @close="closeModal('beforeReset')" />
        <ResetPwdModal v-if="modalState === 'reset'" @switch="emit('update:modalState', $event)" @close="closeModal('reset')" />
        <FinishResetPwdModal v-if="modalState === 'finishReset'" @switch="emit('update:modalState', $event)" @close="closeModal('finishReset')" />
    </div>
</template>
