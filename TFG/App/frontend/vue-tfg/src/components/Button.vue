<script setup lang="ts">
</script>

<template>
    <button class="glow__button">
        <slot /> 
    </button>

</template>


<style scoped>

    .glow__button {
        box-shadow: 0px 0px 20px 5px #61C7EC;
        transition: all 0.3s ease;
        border-radius: 0.375rem;
        background-color: #61C7EC;
        font-size: 1rem;
        color: white;
        padding: 16px 16px;
        cursor: pointer;
        border: none;
    }

    .glow__button:hover {
        box-shadow: 0px 0px 30px 10px #89d1eb;
        background-color: #89d1eb;
    }

    .glow__button:focus-visible {
        outline: 2px solid #61C7EC;
        outline-offset: 2px;
        box-shadow: none;
    }

    .glow__button:active {
        background-color: #3182ce;
    }

    .glow__button[aria-disabled="true"] {
        cursor: not-allowed;
        opacity: 0.5;
    }



</style>
