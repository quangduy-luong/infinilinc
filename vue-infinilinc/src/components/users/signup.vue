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
                    Sign Up
                  </v-toolbar-title>
                </v-toolbar>
                <v-card-text>
                  <v-container @keyup.enter="submit">
                    <v-form v-model="valid" ref="form">
                      <v-text-field
                      label="Username"
                      v-model="username"
                      :rules="usernameRules"
                      required
                      ></v-text-field>
                      <v-text-field
                      label="E-mail"
                      v-model="email"
                      :rules="emailRules"
                      required
                      ></v-text-field>
                      <v-text-field
                      label="Password"
                      v-model="password"
                      :append-icon="passwordVisible ? 'visibility' : 'visibility_off'"
                      :append-icon-cb="() => (passwordVisible = !passwordVisible)"
                      :type="passwordVisible ? 'password' : 'text'"
                      :rules="passwordRules"
                      required
                      ></v-text-field>
                      <v-text-field
                      label="Confirm Password"
                      v-model="password2"
                      :append-icon="password2Visible ? 'visibility' : 'visibility_off'"
                      :append-icon-cb="() => (password2Visible = !password2Visible)"
                      :type="password2Visible ? 'password' : 'text'"
                      :rules="passwordRules.concat([comparePasswords])"
                      required
                      ></v-text-field>
                      <v-btn
                      block
                      @click="submit"
                      :disabled="!valid || loading" 
                      :loading="loading"
                      >
                      submit
                      </v-btn>
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
    data: () => ({
      valid: true,
      email: '',
      username: '',
      password: '',
      password2: '',
      passwordVisible: true,
      password2Visible: true,
      usernameRules: [
        (v) => !!v || 'Cannot be empty!'
      ],
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
      }
    },
    methods: {
      submit () {
        if (this.$refs.form.validate()) {
          this.$store.dispatch('registerUser', { email: this.email, username: this.username, password: this.password })
        }
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