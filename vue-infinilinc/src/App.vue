<template>
  <v-app>
    <sidebar/>
    <toolbar/>
    <v-content>
      <router-view/>
    </v-content>
  </v-app>
</template>

<script>
  export default {
    mounted () {
      /* eslint-disable */
      try {
        nfc.exists()
        this.$store.commit('setNfc', true)
      } catch (e) {
        console.log('The app cannot detect NFC')
      }
    }
  }
</script>