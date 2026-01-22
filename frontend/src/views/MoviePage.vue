<!-- The Page to Display via 精選韓影 on the Navigation Bar -->
<script setup>
    import MovieDetails from '@/components/MovieDetails.vue';
    import MovieList from '@/components/MovieList.vue';
    import { computed, watch } from 'vue'
    import { useRoute, useRouter } from 'vue-router'

    const route = useRoute()
    const router = useRouter()

    const selectedMovieId = computed(() => route.query.id ?? '')
    const isMovieSelected = computed(() => !!route.query.id)

    function goToMovie(movieId) {
        router.push({ name: 'MoviePage', query: { id: movieId } })
    }

    function resetMovie() {
        router.push({ name: 'MoviePage', query: {} })
    }

    watch(() => route.query.id, (newId) => {
        // newId has value -> MovieDetails
        // newId is empty -> MovieList
    });
</script>

<template>
    <div>
        <MovieDetails v-if="isMovieSelected" @reset-movie="resetMovie" :selected-movie-id="selectedMovieId" />
        <MovieList v-else @select-movie="goToMovie" @reset-movie="resetMovie" />
    </div>
</template>
