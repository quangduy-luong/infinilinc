<template>
  <v-container fluid class="py-0 px-0">
    <section>
      <v-flex xs12 v-if="error">
        <v-layout row>
          <app-alert @dismissed="onDismissed" :text="error.message"></app-alert>
        </v-layout>
      </v-flex>
      <v-container>
        <v-layout>
          <v-flex xs12 sm6 offset-sm3>
            <v-card>
              <v-card-media :src="require('/home/il/infinilinc/vue-infinilinc/src/assets/logo.png')" height="300px">
                  <v-layout column class="media">
                      <v-card-title class="black--text pl-5 pt-5">
                      </v-card-title>
                  </v-layout>
              </v-card-media>
              <v-list>
                <v-list-tile
                v-for="(item, i) in items"
                :key="i"
                :to="item.path">
                  <v-list-tile-action>
                    <v-icon color="indigo">{{ item.icon }}</v-icon>
                  </v-list-tile-action>
                  <v-list-tile-content>
                    <v-list-tile-title class="black--text">{{ item.title }}</v-list-tile-title>
                    <v-list-tile-sub-title class="black--text"></v-list-tile-sub-title>
                  </v-list-tile-content>
                  <v-list-tile-action>
                    <v-icon>chevron_right</v-icon>
                  </v-list-tile-action>
                </v-list-tile>
              </v-list>
            </v-card>
          </v-flex>
        </v-layout>-
      </v-container>
    </section>
  </v-container>
</template>

<script>
  export default {
    data: () => ({
      valid: true,
      password: '',
      password2: '',
      passwordVisible: true,
      password2Visible: true,
      email: '',
      emailRules: [
        (v) => !!v || 'Cannot be empty!',
        (v) => /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(v) || 'Not a valid email address!'
      ],
      passwordRules: [
        (v) => !!v || 'Cannot be empty!',
        (v) => (v && v.length >= 8) || 'Must be at least 8 characters long!'
      ]
    }),
    computed: {
      comparePasswords () {
        return this.password !== this.password2 ? 'Passwords do not match!' : true
      },
      user () {
        return this.$store.getters.user
      },
      error () {
        return this.$store.getters.error
      },
      loading () {
        return this.$store.getters.loading
      },
      items () {
        return this.$store.getters.getAccountList
      }
    },
    methods: {
      save () {
        if (this.$refs.form.validate()) {
          this.$store.dispatch('updateUser', { email: 'jnarbaitz@gmail.com', username: 'fuccboi' })
        }
      },
      clear () {
        this.$refs.form.reset()
      },
      onDismissed () {
        console.log('Alert dismissed!')
        this.$store.dispatch('clearError')
      }
    },
    watch: {
      user (value) {
        if (value !== null && value !== undefined) {
          this.$router.push('/')
        }
      }
    }
  }
</script>