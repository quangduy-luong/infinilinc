<template>
  <v-container fluid class="py-0 px-0">
    <section>
      <v-flex xs12 v-if="error">
        <v-layout row>
          <app-alert @dismissed="onDismissed" :text="error.message"></app-alert>
        </v-layout>
      </v-flex>
      <v-jumbotron height="100vh"
          src="https://i.imgur.com/YlECO0f.png"
        >
      <v-container fill-height>
        <v-layout align-center>
          <v-flex xs12 sm6 offset-sm3>
            <v-card>
              <v-toolbar dark color="primary" flat>
                <v-toolbar-title >
                  Sign In
                </v-toolbar-title>
              </v-toolbar>
              <v-card-text>
                <v-container @keyup.enter="submit">
                  <v-form>
                    <v-text-field
                    label="E-mail"
                    v-model="email"
                    required
                    ></v-text-field>
                    <v-text-field
                    label="Password"
                    v-model="password"
                    type="password"
                    required
                    ></v-text-field>
                    <v-card-actions>
                      <v-btn 
                      block 
                      @click="submit"
                      :disabled="loading" 
                      :loading="loading"
                      >
                      Submit
                      </v-btn>
                    </v-card-actions>
                  </v-form>
                </v-container>
              </v-card-text>
            </v-card>
          </v-flex>
        </v-layout>
      </v-container>
      </v-jumbotron>
    </section>
  </v-container>
</template>

<script>
  export default {
    data () {
      return {
        email: '',
        password: ''
      }
    },
    computed: {
      user () {
        return this.$store.getters.user
      },
      error () {
        return this.$store.getters.error
      },
      loading () {
        return this.$store.getters.loading
      }
    },
    methods: {
      submit () {
        this.$store.dispatch('loginUser', { email: this.email, password: this.password })
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