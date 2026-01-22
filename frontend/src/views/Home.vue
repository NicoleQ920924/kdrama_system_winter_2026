<template>
    <div class="home">
        <h1 class="text-center">韓國影視管理系統</h1>
        <h3 class="text-center">專為台灣人打造</h3>
        <h3 class="text-center">多影音平台整合</h3>
        <ul class="text-center">
            <li><router-link class="btn border-dark light-btn" to="/k-dramas">查韓劇</router-link></li>
            <li><router-link class="btn dark-btn" to="/k-movies">查韓影</router-link></li>
            <li><router-link class="btn border-dark light-btn" to="/k-actors">查演員</router-link></li>
        </ul>
    </div>
</template>

<style lang="scss" scoped>
h1 {
    color:$autumn-text-dark;
    margin-top:80px;
    margin-bottom:40px;
}
h3 {
    color:$autumn-light-orange;
    padding:5px 0px;
}
ul {
    list-style:none;
    display:flex;
    justify-content:center;
    gap:20px;
    margin-top:45px;
    margin-bottom:45px;
    padding:0px;
}
ul li {
    display: block;
}
router-link {
    text-decoration:none;
}
.light-btn {
    color:$autumn-text-dark;
    background-color:$autumn-light;

    &:hover,
    &:active,
    &:focus {
        color:$autumn-text-dark;
        background-color:$autumn-light;
    }
}
.dark-btn {
    color:$autumn-text-light;
    background-color:$autumn-dark-brown;

    &:hover,
    &:active,
    &:focus {
        color:$autumn-text-light;
        background-color:$autumn-dark-brown;
    }
}
</style>