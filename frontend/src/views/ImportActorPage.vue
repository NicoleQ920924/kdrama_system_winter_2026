<script setup>
    import { ref } from 'vue'
    import { importActor } from '@/services/actorService'
    import Spinner from '@/components/Spinner.vue'
    import { useRouter } from 'vue-router'

    const loadingActors = ref([])
    const msg = ref('')
    const msgClass = ref('')
    const actorName = ref('')

    const actorId = ref('')

    const router = useRouter()

    async function startImportActor() { // By ChatGPT
        // Return while loading
        if (loadingActors.value.includes(actorName.value)) return;

        // Return if the input is empty
        if (actorName.value == '') return;

        // Add loading status
        loadingActors.value.push(actorName.value)

        try {
            const res = await importActor(actorName.value)
            console.log(res.status)
            if (res.status === 200) {
                msg.value = `${actorName.value} 已成功加入資料庫！`
                msgClass.value = 'success-msg text-center'
                actorId.value = res.data.actorId
            }
        } catch (err) {
            console.error(err)
            if (err.response && err.response.status === 409) {
                msg.value = `${actorName.value} 已存在資料庫！`
                msgClass.value = 'error-msg text-center'
            } else {
                msg.value = `加入 ${actorName.value} 時發生錯誤`
                msgClass.value = 'error-msg text-center'
            }
        } finally {
            const index = loadingActors.value.indexOf(actorName.value)
            if (index !== -1) loadingActors.value.splice(index, 1)
        }
    }

    function backToActorList() {
        router.push({ name: 'ActorPage', query: {} })
    }
</script>

<template>
    <div>
        <h2>新增演員</h2>

        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="loadingActors.length > 0" key="loading">
                <Spinner />
            </div>

            <!-- Not Loading -->
            <div v-else>
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <input v-model="actorName" class="form-control form-text-field" name="actorName" placeholder="請輸入演員的正確中文譯名" required aria-required="true">
                    </p>
                    <div class="text-center">
                        <button @click="startImportActor" type="button" class="btn form-btn shadow-none" :disabled="loadingActors.length > 0">確定</button>
                    </div>
                </form>
                <div :class="msgClass">{{ msg }}</div>
                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
                    <router-link class="btn back-btn text-center" :to="{ name: 'ActorPage', query: { id: actorId } }">點我看 {{ actorName }} 的專頁</router-link>
                </div>
                <div v-if="msgClass == 'success-msg text-center'" class="text-center">
                    <router-link class="btn back-btn text-center" :to="{ name: 'UpdateActorPage', query: { id: actorId } }">點我編輯 {{ actorName }} 的資料</router-link>
                </div>
                <div v-if="msgClass == 'success-msg text-center' || msgClass == 'error-msg text-center'" class="text-center">
                    <button class="btn back-btn text-center" @click="backToActorList">返回演員列表</button>
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
        background-color:none;
        border:none;
        color:$autumn-dark-brown;
        margin:5px 0px;

        &:hover,
        &:active,
        &:focus {
            color:$autumn-dark-brown;
        }
    }
</style>