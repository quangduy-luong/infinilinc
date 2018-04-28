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
              <v-card-media :src="this.user.imageUrl" height="300px" contain>
                <v-layout column class="media">
                  <v-card-title>
                    <v-spacer></v-spacer>
                    <v-btn icon class="mr-3" @click="onPickFile">
                      <v-icon>edit</v-icon>
                    </v-btn>
                    <input
                      type="file"
                      style="display: none" 
                      ref="fileInput" 
                      accept="image/*"
                      @change="onFilePicked">
                  </v-card-title>
                  <v-spacer></v-spacer>
                  <v-card-title class="white--text pl-5 pt-5">
                    <div class="display-1 pl-5 pt-5"></div>
                  </v-card-title>
                </v-layout>
              </v-card-media>
              <v-divider></v-divider>
              <v-list two-line>
                <v-list-tile @click.stop="dialogUsername = true">
                  <v-list-tile-action>
                    <v-icon color="indigo" class="pl-2">perm_identity</v-icon>
                  </v-list-tile-action>
                  <v-list-tile-content>
                    <v-list-tile-title class="pl-4">{{ this.user.username }}</v-list-tile-title>
                    <v-list-tile-sub-title></v-list-tile-sub-title>
                  </v-list-tile-content>
                  <v-list-tile-action>
                    <v-icon class="pr-2">edit</v-icon>
                  </v-list-tile-action>
                </v-list-tile>
                <v-divider></v-divider>
                <v-list-tile @click.stop="dialogEmail = true">
                  <v-list-tile-action>
                    <v-icon color="indigo" class="pl-2">mail_outline</v-icon>
                  </v-list-tile-action>
                  <v-list-tile-content>
                    <v-list-tile-title class="pl-4">{{ this.user.email }}</v-list-tile-title>
                    <v-list-tile-sub-title></v-list-tile-sub-title>
                  </v-list-tile-content>
                  <v-list-tile-action>
                    <v-icon class="pr-2">edit</v-icon>
                  </v-list-tile-action>
                </v-list-tile>
                <v-divider></v-divider>
                <v-list-tile @click.stop="dialogPassword = true">
                  <v-list-tile-action>
                    <v-icon color="indigo" class="pl-2">lock_outline</v-icon>
                  </v-list-tile-action>
                  <v-list-tile-content>
                    <v-list-tile-title class="pl-4">Password</v-list-tile-title>
                    <v-list-tile-sub-title></v-list-tile-sub-title>
                  </v-list-tile-content>
                  <v-list-tile-action>
                    <v-icon class="pr-2">edit</v-icon>
                  </v-list-tile-action>
                </v-list-tile>
              </v-list>
            </v-card>

            <v-dialog v-model="dialogUsername" fullscreen :overlay="false">
              <v-card>
                <v-toolbar dark color="primary">
                  <v-btn icon @click.native="dialogUsername = false" dark>
                    <v-icon>chevron_left</v-icon>
                  </v-btn>
                  <v-toolbar-title>Change username</v-toolbar-title>
                  <v-spacer></v-spacer>
                  <v-toolbar-items>
                    <v-btn dark flat @click.native="saveUsername">Save</v-btn>
                  </v-toolbar-items>
                </v-toolbar>
                <v-container>
                  <v-layout>
                    <v-flex xs12 sm6 offset-sm3>
                      <v-card>
                        <v-card-text>
                          <v-form v-model="valid" ref="form">
                            <v-text-field
                            label="New username"
                            v-model="username"
                            :rules="usernameRules"
                            :counter="18"
                            required
                            ></v-text-field>
                          </v-form>
                        </v-card-text>
                      </v-card>
                    </v-flex>
                  </v-layout>
                </v-container>
              </v-card>
            </v-dialog>

            <v-dialog v-model="dialogEmail" fullscreen :overlay="false">
              <v-card>
                <v-toolbar dark color="primary">
                  <v-btn icon @click.native="dialogEmail = false" dark>
                    <v-icon>chevron_left</v-icon>
                  </v-btn>
                  <v-toolbar-title>Change email</v-toolbar-title>
                  <v-spacer></v-spacer>
                  <v-toolbar-items>
                    <v-btn dark flat @click.native="saveEmail">Save</v-btn>
                  </v-toolbar-items>
                </v-toolbar>
                <v-container>
                  <v-layout>
                    <v-flex xs12 sm6 offset-sm3>
                      <v-card>
                        <v-card-text>
                          <v-form v-model="valid" ref="form2">
                            <v-text-field
                            label="New email"
                            v-model="email"
                            :rules="emailRules"
                            required
                            ></v-text-field>
                            <v-text-field
                            label="Confirm email"
                            v-model="email2"
                            :rules="emailRules.concat([compareEmails])"
                            required
                            ></v-text-field>
                          </v-form>
                        </v-card-text>
                      </v-card>
                    </v-flex>
                  </v-layout>
                </v-container>
              </v-card>
            </v-dialog>

            <v-dialog v-model="dialogPassword" fullscreen :overlay="false">
              <v-card>
                <v-toolbar dark color="primary">
                  <v-btn icon @click.native="dialogPassword = false" dark>
                    <v-icon>chevron_left</v-icon>
                  </v-btn>
                  <v-toolbar-title>Change password</v-toolbar-title>
                  <v-spacer></v-spacer>
                  <v-toolbar-items>
                    <v-btn dark flat @click.native="savePassword">Save</v-btn>
                  </v-toolbar-items>
                </v-toolbar>
                <v-container>
                  <v-layout>
                    <v-flex xs12 sm6 offset-sm3>
                      <v-card>
                        <v-card-text>
                          <v-form v-model="valid" ref="form3">
                            <v-text-field
                            label="New password"
                            v-model="password"
                            :append-icon="passwordVisible ? 'visibility' : 'visibility_off'"
                            :append-icon-cb="() => (passwordVisible = !passwordVisible)"
                            :type="passwordVisible ? 'password' : 'text'"
                            :rules="passwordRules"
                            required
                            ></v-text-field>
                            <v-text-field
                            label="Confirm password"
                            v-model="password2"
                            :append-icon="password2Visible ? 'visibility' : 'visibility_off'"
                            :append-icon-cb="() => (password2Visible = !password2Visible)"
                            :type="password2Visible ? 'password' : 'text'"
                            :rules="passwordRules.concat([comparePasswords])"
                            required
                            ></v-text-field>
                          </v-form>
                        </v-card-text>
                      </v-card>
                    </v-flex>
                  </v-layout>
                </v-container>
              </v-card>
            </v-dialog>
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
      dialogUsername: false,
      dialogEmail: false,
      dialogPassword: false,
      username: '',
      email: '',
      email2: '',
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
      ],
      image: null,
      imageUrl: ''
    }),
    computed: {
      compareEmails () {
        return this.email !== this.email2 ? 'Emails do not match!' : true
      },
      comparePasswords () {
        return this.password !== this.password2 ? 'Passwords do not match!' : true
      },
      user () {
        return this.$store.getters.user
      },
      loading () {
        return this.$store.getters.loading
      }
    },
    methods: {
      saveUsername () {
        if (this.$refs.form.validate()) {
          this.dialogUsername = false
          this.$store.dispatch('updateUsername', { username: this.username })
        }
      },
      saveEmail () {
        if (this.$refs.form2.validate()) {
          this.dialogEmail = false
          this.$store.dispatch('updateEmail', { email: this.email })
        }
      },
      savePassword () {
        if (this.$refs.form3.validate()) {
          this.dialogPassword = false
          this.$store.dispatch('updatePassword', { password: this.password })
        }
      },
      onPickFile () {
        this.$refs.fileInput.click()
      },
      onFilePicked (event) {
        const files = event.target.files
        let filename = files[0].name
        if (filename.lastIndexOf('.') <= 0) {
          return alert('Please upload a valid image file.')
        }
        const fileReader = new FileReader()
        fileReader.addEventListener('load', () => {
          this.imageUrl = fileReader.result
        })
        fileReader.readAsDataURL(files[0])
        this.image = files[0]
        this.$store.dispatch('updatePhoto', { image: this.image })
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