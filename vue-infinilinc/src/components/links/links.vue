<template>
  <v-container fluid py-0 px-0>
    <v-layout row wrap>
      <v-flex xs12>
        <v-container v-if="links === null || links === undefined || links.length < 1">
          <span>You do not have any links yet!</span>
        </v-container>
        <v-container v-else fluid>
          <v-layout row wrap>
            <v-flex xs12 v-for="(link, i) in links" :key="i">
              <infinilinc :link="link"></infinilinc>
            </v-flex>
          </v-layout>
        </v-container>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script>
  import link from './link'
  export default {
    computed: {
      authenticated () {
        return this.$store.getters.user !== null
      },
      links () {
        return this.$store.getters.links
      }
    },
    components: {
      'infinilinc': link
    }
  }
</script>