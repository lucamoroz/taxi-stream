<template>
    <div class="hello">
        <h1>Advanced Internet Computing 2019/20</h1>
        <p>Connection to API server: <span class="status" v-bind:class="{success}">{{ serverStatus }}</span></p>
    </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import axios from "axios";
import config from "../config";

@Component
export default class HelloWorld extends Vue {
    @Prop() private msg!: string;
    private serverStatus: string = "unknown";
    private success: boolean = false;

    constructor() {
      super();
    }

    async created() {
      try {
        const response = await axios.get(`${config.apiUrl}/health`);
        if (response.status === 200) {
          this.serverStatus = "ok :-)";
          this.success = true;
        } else {
          this.serverStatus = `unknown (?)`;
          this.success = false;
        }

      } catch (e) {
        this.serverStatus = `unknown (${e.name}: ${e.message})`;
        this.success = false;
      }

      // eslint-disable-next-line no-console
      console.log("yay created");
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    h3 {
        margin: 40px 0 0;
    }

    ul {
        list-style-type: none;
        padding: 0;
    }

    li {
        display: inline-block;
        margin: 0 10px;
    }

    a {
        color: #42b983;
    }
    span.status {
        font-weight: bold;
        color: red;
    }
    span.status.success {
        color: darkgreen;
    }
</style>
