<script setup>
    import { ref } from 'vue'
    import { importDrama } from '@/services/dramaService'
    import Spinner from '@/components/Spinner.vue'
    import { useRouter } from 'vue-router'

    const loadingDramas = ref([])
    const msg = ref('')
    const msgClass = ref('')
    const dramaName = ref('')

    const dramaIds = ref([])

    const router = useRouter()

    async function startImportDrama() { // By ChatGPT
        // Return while loading
        if (loadingDramas.value.includes(dramaName.value)) return;

        // Return if the input is empty
        if (dramaName.value == '') return;

        // Add loading status
        loadingDramas.value.push(dramaName.value)

        try {
            const res = await importDrama(dramaName.value)
            console.log(res.status)
            if (res.status === 200) {
                msg.value = `${dramaName.value} 已成功加入資料庫！`
                msgClass.value = 'success-msg text-center'
                res.data.forEach(item => {
                    dramaIds.value.push(item.dramaId)
                })
            }
        } catch (err) {
            console.error(err)
            if (err.response && err.response.status === 409) {
                msg.value = `${dramaName.value} 已存在資料庫！`
                msgClass.value = 'error-msg text-center'
            } else {
                msg.value = `加入 ${dramaName.value} 時發生錯誤`
                msgClass.value = 'error-msg text-center'
            }
        } finally {
            const index = loadingDramas.value.indexOf(dramaName.value)
            if (index !== -1) loadingDramas.value.splice(index, 1)
        }
    }

    function backToDramaList() {
        router.push({ name: 'DramaPage', query: {} })
    }
</script>

<template>
    <div>
        <h2>新增韓劇</h2>

        <transition name="fade" mode="out-in">
            <!-- Loading Animation -->
            <div v-if="loadingDramas.length > 0" key="loading">
                <Spinner />
            </div>

            <!-- Not Loading -->
            <div v-else>
                <form class="form" method="post">
                    <p class="form-group form-text-p">
                        <input v-model="dramaName" class="form-control form-text-field" name="dramaName" placeholder="請輸入韓劇的正確中文譯名" required aria-required="true">
                    </p>
                    <div class="input-msg text-center">1. 不要打季數 2. 會新增該韓劇所有已播出的季</div>
                    <div class="text-center">
                        <button @click="startImportDrama" type="button" class="btn form-btn shadow-none" :disabled="loadingDramas.length > 0">確定</button>
                    </div>
                </form>
                <div :class="msgClass">{{ msg }}</div>
                <div v-if="msgClass == 'success-msg text-center'">
                    <div v-for="(dramaId, index) in dramaIds" :key="index" class="text-center">
                        <router-link class="btn back-btn text-center" :to="{ name: 'DramaPage', query: { id: dramaId } }">點我看 {{ dramaName }}{{ index + 1 == 1 ? '' : index + 1 }} 的專頁</router-link>
                        <router-link class="btn back-btn text-center" :to="{ name: 'UpdateDramaPage', query: { id: dramaId } }">點我編輯 {{ dramaName }}{{ index + 1 == 1 ? '' : index + 1 }} 的資料</router-link>
                    </div>
                </div>
                <div v-if="msgClass == 'success-msg text-center' || msgClass == 'error-msg text-center'" class="text-center">
                    <button class="btn back-btn text-center" @click="backToDramaList">返回韓劇列表</button>
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
    .input-msg
    {
        font-weight:normal;
        color:$autumn-dark-orange;
        font-size:large;
        margin:25px 0px;
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