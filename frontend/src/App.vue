<!-- Main App -->
  <script setup>
  import { ref } from 'vue'
  
  import Navbar from './components/Navbar.vue'
  import ModalController from './components/ModalController.vue';

  // The const to keep track of modal state
  const modalState = ref(null) // 'login', 'loginSuccess', 'register', 'emailVerification', 'finishRegistration',
                                  // 'forgot', 'beforeReset' 'reset', 'finishReset', null
  
  // The const to keep track of login status
  const isLoggedIn = ref(false)

  // Handling modal opening
  function openModal(state) {
      modalState.value = state
  }

  function handleLoginSuccess() {
    isLoggedIn.value = true
  }

  function handleLogout() {
    isLoggedIn.value = false
  }
</script>

<template>
   <div>
    <header>
      <img src="./assets/logo.png" class="header-logo" alt="header logo">
      <Navbar @open-modal="openModal" :is-logged-in="isLoggedIn" @logout="handleLogout" />
    </header>
    <ModalController :modalState="modalState" @update:modalState="modalState = $event" @loggedin="handleLoginSuccess" @logout="handleLogout" />
    <router-view :key="$route.fullPath" />
    <footer>
      <table class="table-borderless table-center footer-table">
        <tr>
          <td width="10%">
            <img class="tmdb-logo" src="https://www.themoviedb.org/assets/2/v4/logos/v2/blue_square_2-d537fb228cf3ded904ef09b136fe3fec72548ebc1fea3fbbd1ad9e36364db38b.svg">
          </td>
          <td width="90%">
            <h5 class="tmdb-copyright-msg">This [website, program, service, application, product] uses TMDB and the TMDB APIs but is not endorsed, certified, or otherwise approved by TMDB.</h5>
          </td>
        </tr>
      </table>
      <div class="text-center">
        <a href="https://www.themoviedb.org">Link to TMDB Home Page</a>
        <a href="https://www.themoviedb.org/documentation/api">Link to TMDB API Official Documentation</a>
      </div>
    </footer>
   </div>
</template>

<style lang="scss" scoped>
  header {
    background-color:$autumn-light;
    height:120px;
    width:100%;
    margin:0px 0px 5px 0px;
  }
  footer {
    background-color:$autumn-light;
    height:120px;
    width:100%;
    padding:5px;
  }
  .header-logo {
    position: absolute;
    top: 10px;
    left: 20px;
    width: 100px;
    height: auto;
  }
  .footer-table {
    margin:0px 30px;
  }
  .tmdb-logo {
    width:50px;
    height:auto;
    text-align:center;
    vertical-align:middle;
    padding-right:-15%;
  }
  .tmdb-copyright-msg {
    color:$autumn-text-dark;
    width:90%;
  }
  a {
    margin:0px 10px;
    color:$autumn-dark-brown;

     &:hover,
     &:active,
     &:focus
     &:visited {
          color:$autumn-dark-brown;
      }
  }
</style>
