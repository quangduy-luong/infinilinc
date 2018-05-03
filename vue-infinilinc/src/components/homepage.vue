<template>
  <v-container fluid py-0 px-0>
    <v-layout row wrap>
      <v-flex xs12  v-if="!authenticated">
        <v-jumbotron height="93.5vh"
          src="https://i.imgur.com/YlECO0f.png"
          dark
        >
          <v-container fill-height>
            <v-layout align-center>
              <v-flex text-xs-center>
                <h3 class="display-3" style="text-shadow: 1px 1px #333333;">iNFinilinC</h3>
                <v-btn color="primary" large to="/signup">Sign up</v-btn>
              </v-flex>
            </v-layout>
          </v-container>
        </v-jumbotron>
      </v-flex>
      <v-flex xs12 v-if="authenticated">
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
  import link from './links/link'
  export default {
    data () {
      return {
        otherUser: ''
      }
    },
    computed: {
      authenticated () {
        return this.$store.getters.user !== null
      },
      links () {
        return this.$store.getters.links
      }
    },
    methods: {
    },
    components: {
      'infinilinc': link
    }
  }
</script>