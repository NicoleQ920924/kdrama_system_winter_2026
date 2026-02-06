<!-- Navbar -->
<template>
    <div id="menu">
        <ul>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'Home'}" replace>首頁</router-link></li>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'NewReleasesPage' }" replace>新上架</router-link></li>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'DramaPage', query: {} }" replace>熱門韓劇</router-link></li>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'MoviePage', query: {} }" replace>精選韓影</router-link></li>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'ActorPage', query: {} }" replace>韓國演員一覽</router-link></li>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'PlatformInfoPage', query: {} }" replace>影音平台整理</router-link></li>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'ContactUsPage', query: {} }" replace>聯絡我們</router-link></li>
            <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'ExternalLinksPage', query: {} }" replace>外部連結</router-link></li>
            
            <!-- Show watchlist and profile links when logged in -->
            <template v-if="isLoggedIn && currentUser">
                <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'WatchlistPage' }" replace>追蹤清單</router-link></li>
                <li><router-link class="normal-dark-text nav-router-link" :to="{ name: 'UserProfilePage' }" replace>{{ currentUser.displayName }}</router-link></li>
            </template>
            
            <li><button class="btn nav-btn-light" @click="handleBtn1Click">{{ navBtnText1 }}</button></li>
            <li><button class="btn nav-btn-dark" @click="handleBtn2Click">{{ navBtnText2 }}</button></li>
        </ul>
    </div>
</template>

<script setup>
    import { computed, defineProps, defineEmits, onMounted } from 'vue'
    import { userStore } from '@/store'

    const emit = defineEmits(['open-modal', 'logout'])

    const { isLoggedIn } = defineProps({
        isLoggedIn: Boolean
    })

    const currentUser = computed(() => userStore.getCurrentUser())

    const btnTexts = {
        login: "登入",
        register: "註冊",
        logout: "登出",
        member: "密碼重設"
    }

    const navBtnText1 = computed(() => isLoggedIn ? btnTexts.logout : btnTexts.login)
    const navBtnText2 = computed(() => isLoggedIn ? btnTexts.member : btnTexts.register)

    function handleBtn1Click() {
        if (!isLoggedIn) {
            // To login
            emit('open-modal', 'login') // Tell the parent component to open modal
        } else {
            // To logout
            userStore.clearUser()
            emit('logout')
        }
    }

    function handleBtn2Click() {
        if (!isLoggedIn) {
            // To register
            emit('open-modal', 'register') // Tell the parent component to open modal
        } else {
            // To password reset
            emit('open-modal', 'beforeReset')
        }
    }

    onMounted(() => {
        userStore.initializeStore()
    })
</script>

<style lang="scss" scoped>
    #menu {
        padding:20px 20px 20px 0px;
        vertical-align:middle;
    }
    #menu ul {
        float:right;
        list-style:none;
        margin:0px;
    }
    #menu ul li {
        float:left;
        display:block;
        line-height:80px;
        margin:0px 10px;
        font-size:16px;
    }
    .nav-router-link:link, .nav-router-link:visited {
        font-weight:bold;
        color:$autumn-dark-brown;
        text-decoration:none;
        margin:0px 5px;
    }
    .nav-router-link:hover {
        font-weight:bold;
        color:$autumn-dark-brown;
        text-decoration:underline;
        margin:0px 5px;
    }
    .normal-dark-text {
        color:$autumn-text-dark;
        font-weight:normal;
    }
    .nav-btn-light {
        background-color:$autumn-light-orange;
        color:$autumn-text-dark;

        &:hover,
        &:active,
        &:focus {
            background-color: $autumn-light-orange;
            color: $autumn-text-dark;
        }

        margin-bottom:4px;
    }
    .nav-btn-dark {
        background-color:$autumn-dark-brown;
        color:$autumn-text-light;

        &:hover,
        &:active,
        &:focus {
            background-color:$autumn-dark-brown;
            color:$autumn-text-light;
        }

        margin-bottom:4px;
    }
</style>