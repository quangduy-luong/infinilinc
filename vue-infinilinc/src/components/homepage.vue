<template>
<<<<<<< Updated upstream
  <div
    id="e3"
    style="max-width: 900px; margin: auto;"
    class="grey lighten-3"
  >
    <v-card>
      <v-container
        fluid
        style="min-height: 0;"
        grid-list-lg
      >
        <v-layout row wrap>
          <v-flex xs12 sm4>
            <v-card color="blue-grey darken-2" class="white--text">
              <v-card-title primary-title>
                <div class="headline">Unlimited music now</div>
                <div>Listen to your favorite artists and albums whenever and wherever, online and offline.</div>
              </v-card-title>
              <v-card-actions>
                <v-btn flat dark>Listen now</v-btn>
              </v-card-actions>
            </v-card>
          </v-flex>
          <v-flex xs12 sm4>
            <v-card color="cyan darken-2" class="white--text">
              <v-container fluid grid-list-lg>
                <v-layout row>
                  <v-flex xs7>
                    <div>
                      <div class="headline">Supermodel</div>
                      <div>Foster the People</div>
                    </div>
                  </v-flex>
                  <v-flex xs5>
                    <v-card-media
                      src="https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/68dd54ca-60cf-4ef7-898b-26d7cbe48ec7/10-dithering-opt.jpg"
                      height="125px"
                      contain
                    ></v-card-media>
                  </v-flex>
                </v-layout>
              </v-container>
            </v-card>
          </v-flex>
          <v-flex xs12 sm4>
            <v-card color="purple" class="white--text">
              <v-container fluid grid-list-lg>
                <v-layout row>
                  <v-flex xs7>
                    <div>
                      <div class="headline">Halycon Days</div>
                      <div>Ellie Goulding</div>
                    </div>
                  </v-flex>
                  <v-flex xs5>
                    <v-card-media
                      src="https://cloud.netlifyusercontent.com/assets/344dbf88-fdf9-42bb-adb4-46f01eedd629/68dd54ca-60cf-4ef7-898b-26d7cbe48ec7/10-dithering-opt.jpg"
                      height="125px"
                      contain
                    ></v-card-media>
                  </v-flex>
                </v-layout>
              </v-container>
            </v-card>
          </v-flex>
        </v-layout>
      </v-container>
    </v-card>
  </div>
</template>
=======
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
>>>>>>> Stashed changes
