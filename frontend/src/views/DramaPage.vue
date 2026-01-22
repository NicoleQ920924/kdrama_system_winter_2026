<!-- The Page to Display via 熱門韓劇 on the Navigation Bar -->
<script setup>
    import DramaDetails from '@/components/DramaDetails.vue';
    import DramaList from '@/components/DramaList.vue';
    import { computed, watch } from 'vue'
    import { useRoute, useRouter } from 'vue-router'

    const route = useRoute()
    const router = useRouter()

    watch(() => route.query.id, (newId, oldId) => {
        // newId has value -> DramaDetails
        // newId is empty -> DramaList
        console.log('route.query.id changed:', oldId, '->', newId)
    });

    const selectedDramaId = computed(() => route.query.id || '');
    const isDramaSelected = computed(() => !!selectedDramaId.value);

    function goToDrama(dramaId) {
        router.push({ name: 'DramaPage', query: { id: dramaId } })
    }

    function resetDrama() {
        router.push({ name: 'DramaPage', query: {} })
    }
</script>

<template>
    <div>
        <DramaDetails v-if="isDramaSelected" @reset-drama="resetDrama" :selected-drama-id="selectedDramaId" :key="selectedDramaId" />
        <DramaList v-else @select-drama="goToDrama" @reset-drama="resetDrama" />
    </div>
</template>
