<!-- The Page to Display via 韓國演員一覽 on the Navigation Bar -->
<script setup>
    import ActorDetails from '@/components/ActorDetails.vue';
    import ActorList from '@/components/ActorList.vue';
    import { computed, watch } from 'vue'
    import { useRoute, useRouter } from 'vue-router'

    const route = useRoute()
    const router = useRouter()

    const selectedActorId = computed(() => route.query.id ?? '')
    const isActorSelected = computed(() => !!route.query.id)

    function goToActor(actorId) {
        router.push({ name: 'ActorPage', query: { id: actorId } })
    }

    function resetActor() {
        router.push({ name: 'ActorPage', query: {} })
    }

    watch(() => route.query.id, (newId) => {
        // newId has value -> ActorDetails
        // newId is empty -> ActorList
    });
</script>

<template>
    <div>
        <ActorDetails v-if="isActorSelected" @reset-actor="resetActor" :selected-actor-id="selectedActorId" />
        <ActorList v-else @select-actor="goToActor" @reset-actor="resetActor" />
    </div>
</template>
