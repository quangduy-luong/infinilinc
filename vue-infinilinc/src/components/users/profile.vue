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
                <v-container>
                  <v-form v-model="valid" ref="form">
                    <v-layout row>
                        <v-flex xs3>
                            <v-subheader><v-icon class="pt-3" color="indigo">perm_identity</v-icon></v-subheader>
                        </v-flex>
                        <v-flex>
                            <v-text-field
                            name="Username"
                            ref="username"
                            :value="this.user.username"
                            single-line
                            ></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex xs3>
                            <v-subheader><v-icon class="pt-3" color="indigo">email</v-icon></v-subheader>
                        </v-flex>
                        <v-flex>
                            <v-text-field
                            name="Email"
                            ref="email"
                            :value="this.user.email"
                            :rules="emailRules"
                            single-line
                            ></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex>
                        <v-btn block @click="save"
                        :disabled="!valid || loading" :loading="loading"
                        >
                        Save
                        </v-btn>
                        </v-flex>
                    </v-layout>
                    <!--<v-divider></v-divider>
                    <v-layout row>
                        <v-flex xs3>
                            <v-subheader><v-icon color="indigo">lock_outline</v-icon></v-subheader>
                        </v-flex>
                        <v-flex>
                            <v-text-field
                            label="New Password"
                            :rules="passwordRules"
                            single-line
                            ></v-text-field>
                        </v-flex>
                    </v-layout>
                    <v-layout row>
                        <v-flex xs3>
                            <v-subheader></v-subheader>
                        </v-flex>
                        <v-flex>
                            <v-text-field
                            label="Repeat Password"
                            :rules="passwordRules"
                            single-line
                            ></v-text-field>
                        </v-flex>
                    </v-layout>-->
                  </v-form>
                </v-container>
            </v-card>
          </v-flex>
        </v-layout>
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
      }
    },
    methods: {
      save () {
        if (this.$refs.form.validate()) {
          this.$store.dispatch('updateUser', { email: this.$refs.email, username: this.$refs.username })
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