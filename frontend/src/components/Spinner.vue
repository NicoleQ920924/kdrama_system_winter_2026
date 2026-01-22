<template>
    <div class="text-center my-4">
        <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
        </div>
        <p>請稍候...</p>
    </div>
</template>

<style lang="scss" scoped>
    .spinner-border {
        color:$autumn-dark-orange;
        font-weight:bold;
    }
    p {
        margin-top:20px;
        color:$autumn-red;
        font-size:larger;
    }
</style>