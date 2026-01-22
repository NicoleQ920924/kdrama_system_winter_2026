<script setup>
    import { ref } from 'vue'
    import { importMovie } from '@/services/movieService'
    import Spinner from '@/components/Spinner.vue'
    import { useRouter } from 'vue-router'

    const loadingMovies = ref([])
    const msg = ref('')
    const msgClass = ref('')
    const movieName = ref('')

    const movieId = ref('')

    const router = useRouter()

    async function startImportMovie() { // By ChatGPT
        // Return while loading
        if (loadingMovies.value.includes(movieName.value)) return;

        // Return if the input is empty
        if (movieName.value == '') return;

        // Add loading status
        loadingMovies.value.push(movieName.value)

        try {
            const res = await importMovie(movieName.value)
            console.log(res.status)
            if (res.status === 200) {
                msg.value = `${movieName.value} 已成功加入資料庫！`
                msgClass.value = 'success-msg text-center'
                movieId.value = res.data.movieId
            }
        } catch (err) {
            console.error(err)
            if (err.response && err.response.status === 409) {
                msg.value = `${movieName.value} 已存在資料庫！`
                msgClass.value = 'error-msg text-center'
            } else {
                msg.value = `加入 ${movieName.value} 時發生錯誤`
                msgClass.value = 'error-msg text-center'
            }
        } finally {
            const index = loadingMovies.value.indexOf(movieName.value)
            if (index !== -1) loadingMovies.value.splice(index, 1)
        }
    }

    function backToMovieList() {
        router.push({ name: 'MoviePage', query: {} })
    }
</script>

<template>
    <div>
        <h2>新增韓影</h2>

        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="loadingMovies.length > 0" key="loading">
                <Spinner />
            </div>

            <!-- Not Loading -->
            <div v-else>
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <input v-model="movieName" class="form-control form-text-field" name="movieName" placeholder="請輸入韓影的正確中文譯名" required aria-required="true">
                    </p>
                    <div class="text-center">
                        <button @click="startImportMovie" type="button" class="btn form-btn shadow-none" :disabled="loadingMovies.length > 0">確定</button>
                    </div>
                </form>
                <div :class="msgClass">{{ msg }}</div>
                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
                    <router-link class="btn back-btn text-center" :to="{ name: 'MoviePage', query: { id: movieId } }">點我看 {{ movieName }} 的專頁</router-link>
                </div>
                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
                    <router-link class="btn back-btn text-center" :to="{ name: 'UpdateMoviePage', query: { id: movieId } }">點我編輯 {{ movieName }} 的資料</router-link>
                </div>
                <div v-if="msgClass == 'success-msg text-center' || msgClass == 'error-msg text-center'" class="text-center">
                    <button class="btn back-btn text-center" @click="backToMovieList">返回韓影列表</button>
                </div>
            </div>
        </transition>
    </div>
</template>

<style lang="scss" scoped>
    h2 
    {
        margin:30px 0px;
        color:$autumn-dark-yellow;
        text-align:center;
    }
    .form-text-p
    {
        height:28px;
        margin-top:20px;
        margin-left:36%;
    }
    .form-text-field
    {
        margin:2px 0px 2px 0px;
        border:1px $autumn-light solid;
        width:300px;
    }
    .form-btn
    {
        background-color:$autumn-light-orange;
        border-color:$autumn-light;
        width:10%;
        font-size:large;
        margin-top:20px;
        margin-bottom:30px;
    }
    .form-btn:hover, .form-btn:active, .form-btn:focus
    {
        background-color:$autumn-light-orange;
        text-decoration:none;
        outline:none;
        color:$autumn-dark-brown;
    }
    .success-msg
    {
        font-weight:bolder;
        color:green;
        font-size:large;
        margin:15px 0px;
    }
    .error-msg
    {
        font-weight:bolder;
        color:$autumn-red;
        font-size:large;
        margin:15px 0px;
    }
    .back-btn
    {
        border:none;
        background-color:none;
        color:$autumn-dark-brown;
        margin:5px 0px;

        &:hover,
        &:active,
        &:focus {
            color:$autumn-dark-brown;
        }
    }
</style>