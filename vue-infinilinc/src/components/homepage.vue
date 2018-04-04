<template>
  <v-container fluid py-0 px-0>
    <v-layout row wrap>
      <v-flex xs12  v-if="!authenticated">
        <v-jumbotron color="grey lighten-1">
          <v-container fill-height>
            <v-layout class="text-xs-center" align-center>
              <v-flex>
                <h3 class="display-3">Welcome to the site</h3>
                <span class="subheading">Lorem ipsum dolor sit amet, pri veniam forensibus id. Vis maluisset molestiae id, ad semper lobortis cum. At impetus detraxit incorrupte usu, repudiare assueverit ex eum, ne nam essent vocent admodum.</span>
                <v-divider class="my-3"></v-divider>
                <div class="title mb-3">Check out our newest features!</div>
                <v-btn large color="primary" class="mx-0" to="signup">Register</v-btn>
              </v-flex>
            </v-layout>
          </v-container>
        </v-jumbotron>
      </v-flex>
      <v-flex xs12 v-if="authenticated">
        <v-container v-if="links === undefined || links.length < 1">
          <v-btn @click="onClicked"></v-btn>
          <span>You do not have any links yet!</span>
        </v-container>
        <v-container v-else fluid>
          <v-layout row wrap>
            <v-flex xs12 v-for="(link, i) in links" :key="i">
              <infinilinc :link="link"><</infinilinc>
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
    computed: {
      authenticated () {
        return this.$store.getters.user !== null
      },
      links () {
        return this.$store.getters.links
      }
    },
    methods: {
      onClicked () {
        console.log('clicked')
        this.$store.dispatch('createLink', {originalUser: 'Wy6kgxj5LCdomvfVL4v6bc6oxXB3', otherUser: 'ofcESrYMhMMImfb3397qgK13nXf1'})
      }
    },
    components: {
      'infinilinc': link
    }
  }
</script>