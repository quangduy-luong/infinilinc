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
              <v-card-text>
                <v-form v-model="valid" ref="form" lazy-validation>
                    <v-text-field
                    label="Name"
                    v-model="name"
                    :rules="nameRules"
                    :counter="18"
                    required
                    ></v-text-field>

                    <v-btn
                    block
                    color="secondary"
                    @click="submit"
                    :disabled="!valid"
                    >
                    submit
                    </v-btn>
                </v-form>
              </v-card-text>
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
      nameRules: [
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
          this.$store.dispatch('updateUser', { username: this.name})
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